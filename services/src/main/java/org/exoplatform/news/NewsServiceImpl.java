package org.exoplatform.news;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.impl.Utils;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.extensions.publication.PublicationManager;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.social.ckeditor.HTMLUploadImageProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;


import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.List;



/**
 * Service managing News and storing them in ECMS
 */
public class NewsServiceImpl implements NewsService {

  public static final String NEWS_NODES_FOLDER = "News";

  public static final String PINNED_NEWS_NODES_FOLDER = "Pinned";

  public static final String APPLICATION_DATA_PATH = "/Application Data";

  private RepositoryService repositoryService;

  private SessionProviderService sessionProviderService;

  private NodeHierarchyCreator nodeHierarchyCreator;

  private DataDistributionType dataDistributionType;

  private SpaceService spaceService;

  private ActivityManager activityManager;

  private IdentityManager identityManager;

  private UploadService uploadService;

  private LinkManager linkManager;

  private HTMLUploadImageProcessor imageProcessor;

  private PublicationService publicationService;

  private PublicationManager publicationManager;

  private WCMPublicationService wCMPublicationService;

  public NewsServiceImpl(RepositoryService repositoryService, SessionProviderService sessionProviderService,
                         NodeHierarchyCreator nodeHierarchyCreator, DataDistributionManager dataDistributionManager,
                         SpaceService spaceService, ActivityManager activityManager, IdentityManager identityManager,
                         UploadService uploadService, HTMLUploadImageProcessor imageProcessor, LinkManager linkManager,
                         PublicationService publicationService,
                         PublicationManager publicationManager,
                         WCMPublicationService wCMPublicationService) {
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.spaceService = spaceService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.uploadService = uploadService;
    this.imageProcessor = imageProcessor;
    this.linkManager = linkManager;
    this.dataDistributionType = dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE);
    this.publicationService = publicationService;
    this.publicationManager = publicationManager;
    this.wCMPublicationService = wCMPublicationService;
  }

  /**
   * Create and publish a News
   * A news is composed of an activity and a CMS node containing the data.
   * If the given News has an id and that a draft already exists with this id, the draft is updated and published.
   * @param news The news to create
   * @throws RepositoryException
   */
  public News createNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    try {
      if(news.getId() == null) {
        news = createNewsDraft(news);
      } else {
        updateNews(news);
      }

      Node newsNode = session.getNodeByUUID(news.getId());
      publicationService.changeState(newsNode, "published",  new HashMap<>());
    } finally {
      if(session != null) {
        session.logout();
      }
    }

    postNewsActivity(news);

    if (news.isPinned()) {
      pinNews(news.getId());
    }

    return news;
  }

  /**
   * Get a news by id
   * @param id Id of the news
   * @return The news with the given id
   * @throws Exception
   */
  public News getNewsById(String id) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);

    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    try {
      Node node = session.getNodeByUUID(id);
      return convertNodeToNews(node);
    } catch (ItemNotFoundException e) {
      return null;
    } finally {
      if(session != null) {
        session.logout();
      }
    }
  }

  /**
   * Get all news
   * @return all news
   * @throws Exception
   */
  public List<News> getNews() throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession((repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName()),
            repositoryService.getCurrentRepository());
    List<News> listNews = new ArrayList<>();

    try {
      StringBuilder sqlQuery  = new StringBuilder("select * from exo:news WHERE publication:currentState = 'published' and jcr:path like '/Groups/spaces/%'");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
      NodeIterator it = query.execute().getNodes();
      while (it.hasNext()) {
        Node iterNode = it.nextNode();
        listNews.add(convertNodeToNews(iterNode));

      }
      return listNews;
    } finally {
      if(session != null)   {
        session.logout();
      }
    }
  }

  /**
   * Update a news
   * If the uploadId of the news is null, the illustration is not updated.
   * If the uploadId of the news is empty, the illustration is removed (if any).
   * @param news The new news
   * @throws Exception
   */
  public void updateNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    try {
      Node newsNode = session.getNodeByUUID(news.getId());
      if(newsNode != null) {
        newsNode.setProperty("exo:title", news.getTitle());
        newsNode.setProperty("exo:summary", news.getSummary());
        newsNode.setProperty("exo:body", imageProcessor.processImages(news.getBody(), newsNode, "images"));
        newsNode.setProperty("exo:dateModified", Calendar.getInstance());

        if(StringUtils.isNotEmpty(news.getUploadId())) {
          attachIllustration(newsNode, news.getUploadId());
        } else if("".equals(news.getUploadId())) {
          removeIllustration(newsNode);
        }

        newsNode.save();

        publicationService.changeState(newsNode, "published",  new HashMap<>());
      }
    } finally {
      if(session != null) {
        session.logout();
      }
    }
  }

  /**
   * Pin a news
   *
   * @param newsId The id of the news to be pinned
   * @throws Exception
   */
  public void pinNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),repositoryService.getCurrentRepository());
    News news = getNewsById(newsId);


    Node newsNode = session.getNodeByUUID(newsId);
    newsNode.setProperty("exo:pinned", true);
    newsNode.save();

    Node pinnedRootNode = getPinnedNewsFolder();

    Calendar newsCreationCalendar = Calendar.getInstance();
    newsCreationCalendar.setTime(news.getCreationDate());
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(pinnedRootNode, getNodeRelativePath(newsCreationCalendar));
    if (newsNode.canAddMixin("exo:privilegeable")) {
      newsNode.addMixin("exo:privilegeable");
    }
    ((ExtendedNode) newsNode).setPermission("*:/platform/users", new String[] { PermissionType.READ });
    linkManager.createLink(newsFolderNode, Utils.EXO_SYMLINK, newsNode, null);

    publicationService.changeState(newsNode, "published",  new HashMap<>());
  }

  public void unpinNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
            repositoryService.getCurrentRepository()
                    .getConfiguration()
                    .getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());
    News news = getNewsById(newsId);
    if (news == null) {
      throw new Exception("Unable to find a news with an id equal to: " + newsId);
    }

    Node newsNode = session.getNodeByUUID(newsId);
    if (newsNode == null) {
      throw new Exception("Unable to find a node with an UUID equal to: " + newsId);
    }
    newsNode.setProperty("exo:pinned", false);
    ((ExtendedNode) newsNode).removePermission("*:/platform/users");
    newsNode.save();

    Node pinnedRootNode = getPinnedNewsFolder();
    if (pinnedRootNode == null) {
      throw new Exception("Unable to find the root pinned folder: /Application Data/News/pinned");
    }
    Calendar newsCreationCalendar = Calendar.getInstance();
    newsCreationCalendar.setTime(news.getCreationDate());
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(pinnedRootNode, getNodeRelativePath(newsCreationCalendar));
    if (newsFolderNode == null) {
      throw new Exception("Unable to find the parent node of the current pinned node");
    }
    Node pinnedNode = newsFolderNode.getNode(newsNode.getName());
    if (pinnedNode == null) {
      throw new Exception("Unable to find the current pinned node");
    }
    pinnedNode.remove();
    newsFolderNode.save();

    publicationService.changeState(newsNode, "published",  new HashMap<>());
  }

  /**
   * Get the root folder for pinned news
   *
   * @return the pinned folder node
   * @throws Exception
   */
  private Node getPinnedNewsFolder() throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
            repositoryService.getCurrentRepository()
                    .getConfiguration()
                    .getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());
    Node applicationDataNode = (Node) session.getItem(APPLICATION_DATA_PATH);
    Node newsRootNode;
    if (!applicationDataNode.hasNode(NEWS_NODES_FOLDER)) {
      newsRootNode = applicationDataNode.addNode(NEWS_NODES_FOLDER, "nt:unstructured");
      applicationDataNode.save();
    } else {
      newsRootNode = applicationDataNode.getNode(NEWS_NODES_FOLDER);
    }
    Node pinnedRootNode;
    if (!newsRootNode.hasNode(PINNED_NEWS_NODES_FOLDER)) {
      pinnedRootNode = newsRootNode.addNode(PINNED_NEWS_NODES_FOLDER, "nt:unstructured");
      newsRootNode.save();
    } else {
      pinnedRootNode = newsRootNode.getNode(PINNED_NEWS_NODES_FOLDER);
    }

    return pinnedRootNode;
  }

  /**
   * Share a news to a list of spaces
   * @param sharedNews Data of the shared news
   * @param spaces List of spaces to share the news with
   */
  public void shareNews(SharedNews sharedNews, List<Space> spaces) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    try {
      Identity poster = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sharedNews.getPoster(), false);
      for (Space space : spaces) {
        // update news node permissions
        Node newsNode = session.getNodeByUUID(sharedNews.getNewsId());
        if (newsNode != null) {
          if (newsNode.canAddMixin("exo:privilegeable")) {
            newsNode.addMixin("exo:privilegeable");
          }
          ((ExtendedNode) newsNode).setPermission("*:" + space.getGroupId(), PermissionType.ALL);
          newsNode.save();
        }

        // create activity
        Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
        ExoSocialActivity activity = new ExoSocialActivityImpl();
        activity.setTitle(sharedNews.getDescription());
        activity.setBody("");
        activity.setType("shared_news");
        activity.setUserId(poster.getId());
        Map<String, String> templateParams = new HashMap<>();
        templateParams.put("newsId", sharedNews.getNewsId());
        templateParams.put("sharedActivityId", sharedNews.getActivityId());
        activity.setTemplateParams(templateParams);
        activityManager.saveActivityNoReturn(spaceIdentity, activity);
      }
    } finally {
      if(session != null) {
        session.logout();
      }
    }
  }

  /**
   * Get news drafts
   * @param  spaceId News space
   * @param  author News drafts author
   * @return The news drafts
   * @throws Exception
   */
  public List<News> getNewsDrafts(String spaceId, String author) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);

    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());
    List<News> newsDrafts = new ArrayList<>();

    try {
      StringBuilder sqlQuery = new StringBuilder("SELECT * FROM exo:news WHERE publication:currentState = 'draft' AND exo:author = '")
              .append(author).append("'").append("AND exo:spaceId='").append(spaceId).append("'");
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
      NodeIterator it = query.execute().getNodes();
      while (it.hasNext()) {
        Node iterNode = it.nextNode();
        newsDrafts.add(convertNodeToNews(iterNode));
      }
      return (newsDrafts);
    } catch (ItemNotFoundException e) {
      return null;
    } finally {
      if(session != null) {
        session.logout();
      }
    }
  }

  /**
   * Delete news
   * @param newsId the news id to delete
   * @throws Exception
   */
  public void deleteNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);

    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    try {
      Node node = session.getNodeByUUID(newsId);
      node.remove();
      session.save();
    } finally {
      if(session != null) {
        session.logout();
      }
    }
  }

  /**
   * Post the news activity in the given space
   * @param news The news to post as an activity
   */
  void postNewsActivity(News news) {
    Identity poster = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor(), false);

    Space space = spaceService.getSpaceById(news.getSpaceId());
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("");
    activity.setBody("");
    activity.setType("news");
    activity.setUserId(poster.getId());
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("newsId", news.getId());
    activity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(spaceIdentity, activity);
  }

  private String getNodeRelativePath(Calendar now) {
    return now.get(Calendar.YEAR) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.DAY_OF_MONTH);
  }

  private void attachIllustration(Node newsNode, String uploadId) throws Exception {
    UploadResource uploadedResource = uploadService.getUploadResource(uploadId);
    if (uploadedResource == null) {
      throw new Exception("Cannot attach uploaded file " + uploadId + ", it may not exist");
    }

    boolean illustrationExists = newsNode.hasNode("illustration");
    Node illustrationNode;
    if(illustrationExists) {
      illustrationNode = newsNode.getNode("illustration");
    } else {
      illustrationNode = newsNode.addNode("illustration", "nt:file");
    }
    illustrationNode.setProperty("exo:title", uploadedResource.getFileName());
    Node resourceNode;
    if(illustrationExists) {
      resourceNode = illustrationNode.getNode("jcr:content");
    } else {
      resourceNode = illustrationNode.addNode("jcr:content", "nt:resource");
    }
    resourceNode.setProperty("jcr:mimeType", uploadedResource.getMimeType());
    Calendar now = Calendar.getInstance();
    resourceNode.setProperty("jcr:lastModified", now);
    resourceNode.setProperty("exo:dateModified", now);
    String fileDiskLocation = uploadedResource.getStoreLocation();
    try(InputStream inputStream = new FileInputStream(fileDiskLocation)) {
      resourceNode.setProperty("jcr:data", inputStream);
      newsNode.save();
    }
  }

  private void removeIllustration(Node newsNode) throws Exception {
    if(newsNode.hasNode("illustration")) {
      newsNode.getNode("illustration").remove();
      newsNode.save();
    }
  }

  private Node getSpaceNewsRootNode(String spaceId, Session session) throws RepositoryException {
    Space space = spaceService.getSpaceById(spaceId);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    String spaceParentPath = groupPath + space.getGroupId();

    Node spaceRootNode = (Node) session.getItem(spaceParentPath);

    Node spaceNewsRootNode;
    if(!spaceRootNode.hasNode(NEWS_NODES_FOLDER)) {
      spaceNewsRootNode = spaceRootNode.addNode(NEWS_NODES_FOLDER, "nt:unstructured");
      if(spaceNewsRootNode.canAddMixin("exo:privilegeable")) {
        spaceNewsRootNode.addMixin("exo:privilegeable");
      }
      ((ExtendedNode) spaceNewsRootNode).setPermission("*:/platform/administrators", PermissionType.ALL);
      ((ExtendedNode) spaceNewsRootNode).setPermission("*:" + space.getGroupId(), PermissionType.ALL);
      spaceRootNode.save();
    } else {
      spaceNewsRootNode = spaceRootNode.getNode(NEWS_NODES_FOLDER);
    }
    return spaceNewsRootNode;
  }

  private News convertNodeToNews(Node node) throws Exception {
    if(node == null) {
      return null;
    }

    News news = new News();

    news.setId(node.getUUID());
    news.setTitle(node.getProperty("exo:title").getString());
    news.setSummary(node.getProperty("exo:summary").getString());
    news.setBody(node.getProperty("exo:body").getString());
    news.setAuthor(node.getProperty("exo:author").getString());
    news.setCreationDate(node.getProperty("exo:dateCreated").getDate().getTime());
    news.setUpdater(node.getProperty("exo:lastModifier").getString());
    news.setUpdateDate(node.getProperty("exo:dateModified").getDate().getTime());
    news.setPinned(node.getProperty("exo:pinned").getBoolean());
    news.setSpaceId(node.getProperty("exo:spaceId").getString());
    news.setPath(getPath(node));

    if(node.hasNode("illustration")) {
      Node illustrationContentNode = node.getNode("illustration").getNode("jcr:content");
      byte[] bytes = IOUtils.toByteArray(illustrationContentNode.getProperty("jcr:data").getStream());
      news.setIllustration(bytes);
      news.setIllustrationUpdateDate(illustrationContentNode.getProperty("exo:dateModified").getDate().getTime());
    }

    Space space = spaceService.getSpaceById(news.getSpaceId());
    if(space != null) {
      String spaceName = space.getDisplayName();
      news.setSpaceDisplayName(spaceName);
      if(StringUtils.isNotEmpty(space.getGroupId())) {
        StringBuilder spaceUrl = new StringBuilder().append("/portal/g/:spaces:").append(space.getGroupId()
                .split("/")[2]).append("/").append(space.getPrettyName());
        news.setSpaceUrl(spaceUrl.toString());
      }
    }

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor(), true);
    if(identity != null && identity.getProfile() != null) {
      news.setAuthorDisplayName(identity.getProfile().getFullName());
    }

    return news;
  }

  private String getPath(Node node) throws Exception {
    String nodePath = null;
    NodeLocation nodeLocation = NodeLocation.getNodeLocationByNode(node);

    if(nodeLocation != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("/")
              .append(nodeLocation.getRepository())
              .append("/")
              .append(nodeLocation.getWorkspace())
              .append(node.getPath());
      nodePath = Text.escapeIllegalJcrChars(sb.toString());
    }

    return nodePath;
  }
  /**
   * Create the exo:news draft node in CMS
   * @param news
   * @return News draft id
   * @throws Exception
   */
  public News createNewsDraft(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    Node spaceNewsRootNode = getSpaceNewsRootNode(news.getSpaceId(), session);

    Calendar creationCalendar = Calendar.getInstance();
    if(news.getCreationDate() != null) {
      creationCalendar.setTime(news.getCreationDate());
    } else {
      news.setCreationDate(creationCalendar.getTime());
    }
    String newsNodeName = !news.getTitle().equals("") ? news.getTitle() : "Untitled";
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(spaceNewsRootNode, getNodeRelativePath(creationCalendar));
    Node newsDraftNode = newsFolderNode.addNode(Utils.cleanName(newsNodeName).trim(), "exo:news");
    newsDraftNode.addMixin("exo:datetime");
    newsDraftNode.setProperty("exo:title", news.getTitle());
    newsDraftNode.setProperty("exo:summary", news.getSummary());
    newsDraftNode.setProperty("exo:body", news.getBody());
    newsDraftNode.setProperty("exo:author", news.getAuthor());
    newsDraftNode.setProperty("exo:dateCreated", creationCalendar);
    Calendar updateCalendar = Calendar.getInstance();
    if(news.getUpdateDate() != null) {
      updateCalendar.setTime(news.getUpdateDate());
    } else {
      news.setUpdateDate(updateCalendar.getTime());
    }
    newsDraftNode.setProperty("exo:dateModified", updateCalendar);
    newsDraftNode.setProperty("exo:pinned", false);
    newsDraftNode.setProperty("exo:spaceId", news.getSpaceId());


    Lifecycle lifecycle = publicationManager.getLifecycle("newsLifecycle");
    String lifecycleName = wCMPublicationService.getWebpagePublicationPlugins()
            .get(lifecycle.getPublicationPlugin())
            .getLifecycleName();
    if (newsDraftNode.canAddMixin("publication:authoring")) {
      newsDraftNode.addMixin("publication:authoring");
      newsDraftNode.setProperty("publication:lastUser", news.getAuthor());
      newsDraftNode.setProperty("publication:lifecycle", lifecycle.getName());
    }
    publicationService.enrollNodeInLifecycle(newsDraftNode, lifecycleName);
    publicationService.changeState(newsDraftNode, "draft",  new HashMap<>());

    newsDraftNode.setProperty("exo:body", imageProcessor.processImages(news.getBody(), newsDraftNode, "images"));
    spaceNewsRootNode.save();

    if(StringUtils.isNotEmpty(news.getUploadId())) {
      attachIllustration(newsDraftNode, news.getUploadId());
    }

    news.setId(newsDraftNode.getUUID());

    return news;
  }
}
