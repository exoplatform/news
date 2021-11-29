/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.storage.jcr;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.jcr.model.VersionNode;
import org.exoplatform.ecm.utils.text.Text;
import org.exoplatform.news.connector.NewsSearchConnector;
import org.exoplatform.news.connector.NewsSearchResult;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.queryBuilder.NewsQueryBuilder;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.news.utils.NewsUtils;
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
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.NodetypeConstant;
import org.exoplatform.services.wcm.extensions.publication.PublicationManager;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.social.common.service.HTMLUploadImageProcessor;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

public class JcrNewsStorage implements NewsStorage {
  
  public static final String       APPLICATION_DATA_PATH            = "/Application Data";
  
  private static final String      HTML_AT_SYMBOL_PATTERN           = "@";

  private static final String      HTML_AT_SYMBOL_ESCAPED_PATTERN   = "&#64;";
  
  private static final String      LAST_PUBLISHER                   = "publication:lastUser";
  
  private static final Log         LOG                             = ExoLogger.getLogger(JcrNewsStorage.class);
  
  private static final Pattern     MENTION_PATTERN                  = Pattern.compile("@([^\\s<]+)|@([^\\s<]+)$");
  
  private static final String EXO_SUMMARY_PROP = "exo:summary";
  
  private static final String EXO_BODY_PROP = "exo:body";
  
  public static final String       MIX_NEWS_MODIFIERS               = "mix:newsModifiers";

  public static final String       MIX_NEWS_MODIFIERS_PROP          = "exo:newsModifiersIds";

  public static final String       NEWS_ACTIVITY_POSTING_MIXIN_TYPE = "mix:newsActivityPosting";

  public static final String       NEWS_ACTIVITY_POSTED_MIXIN_PROP  = "exo:newsActivityPosted";
  
  public static final String       NEWS_DRAFT_VISIBILE_MIXIN_PROP   = "exo:draftVisible";
  
  public static final String       NEWS_DRAFT_VISIBILITY_MIXIN_TYPE = "mix:draftVisibility";
  
  public static final String       NEWS_NODES_FOLDER                = "News";
  
  public static final String       PUBLISHED_NEWS_NODES_FOLDER         = "Pinned";
  
  public static final String[]     SHARE_NEWS_PERMISSIONS           = new String[] { PermissionType.READ };
  
  private ActivityManager          activityManager;
  
  private DataDistributionType     dataDistributionType;
  
  private IdentityManager          identityManager;
  
  private HTMLUploadImageProcessor imageProcessor;
  
  private LinkManager              linkManager;

  private NewsAttachmentsStorage   newsAttachmentsService;
  
  private NodeHierarchyCreator     nodeHierarchyCreator;
  
  private PublicationManager       publicationManager;
  
  private PublicationService       publicationService;
  
  private RepositoryService        repositoryService;
  
  private SessionProviderService   sessionProviderService;

  private SpaceService             spaceService;

  private UploadService            uploadService;
  
  private WCMPublicationService    wCMPublicationService;
  
  private NewsSearchConnector      newsSearchConnector;

  public JcrNewsStorage(RepositoryService repositoryService,
                         SessionProviderService sessionProviderService,
                         NodeHierarchyCreator nodeHierarchyCreator,
                         DataDistributionManager dataDistributionManager,
                         ActivityManager activityManager,
                         SpaceService spaceService,
                         UploadService uploadService,
                         HTMLUploadImageProcessor imageProcessor,
                         PublicationService publicationService,
                         PublicationManager publicationManager,
                         NewsAttachmentsStorage newsAttachmentsService,
                         IdentityManager identityManager,
                         LinkManager linkManager,
                         NewsSearchConnector newsSearchConnector,
                         WCMPublicationService wCMPublicationService) {
    
    this.activityManager = activityManager;
    this.dataDistributionType = dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE);
    this.imageProcessor = imageProcessor;
    this.identityManager = identityManager;
    this.linkManager = linkManager;
    this.newsAttachmentsService = newsAttachmentsService;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.publicationManager = publicationManager;
    this.publicationService = publicationService;
    this.repositoryService = repositoryService;
    this.sessionProviderService = sessionProviderService;
    this.spaceService = spaceService;
    this.uploadService = uploadService;
    this.wCMPublicationService = wCMPublicationService;
    this.newsSearchConnector = newsSearchConnector;
    
  }
  
  /**
   * Create the exo:news draft node in CMS
   * 
   * @param news the news
   * @return News draft id
   * @throws Exception when error
   */
  @Override
  public News createNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node spaceNewsRootNode = getSpaceNewsRootNode(news.getSpaceId(), session);

    Calendar creationCalendar = Calendar.getInstance();
    if (news.getCreationDate() != null) {
      creationCalendar.setTime(news.getCreationDate());
    } 
    else {
      news.setCreationDate(creationCalendar.getTime());
    }
    String newsNodeName = !news.getTitle().equals("") ? news.getTitle() : "Untitled";
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(spaceNewsRootNode, getNodeRelativePath(creationCalendar));
    Node newsDraftNode = newsFolderNode.addNode(Utils.cleanName(newsNodeName).trim(), NewsUtils.EXO_NEWS);
    newsDraftNode.addMixin("exo:datetime");
    newsDraftNode.setProperty(NodetypeConstant.EXO_TITLE, news.getTitle());
    newsDraftNode.setProperty(EXO_SUMMARY_PROP, news.getSummary());
    newsDraftNode.setProperty(EXO_BODY_PROP, news.getBody());
    newsDraftNode.setProperty("exo:author", news.getAuthor());
    newsDraftNode.setProperty("exo:dateCreated", creationCalendar);
    newsDraftNode.setProperty("exo:viewsCount", 0);
    newsDraftNode.setProperty("exo:viewers", "");
    newsDraftNode.setProperty("exo:activities", "");
    Calendar updateCalendar = Calendar.getInstance();
    if (news.getUpdateDate() != null) {
      updateCalendar.setTime(news.getUpdateDate());
    } 
    else {
      news.setUpdateDate(updateCalendar.getTime());
    }
    newsDraftNode.setProperty("exo:dateModified", updateCalendar);
    newsDraftNode.setProperty("exo:pinned", false);
    newsDraftNode.setProperty("exo:archived", false);
    newsDraftNode.setProperty("exo:spaceId", news.getSpaceId());
    if (newsDraftNode.canAddMixin(NEWS_DRAFT_VISIBILITY_MIXIN_TYPE) && !newsDraftNode.hasProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP)) {
      newsDraftNode.addMixin(NEWS_DRAFT_VISIBILITY_MIXIN_TYPE);
    }
    newsDraftNode.setProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP, String.valueOf(news.isDraftVisible()));

    if (newsDraftNode.canAddMixin(NEWS_ACTIVITY_POSTING_MIXIN_TYPE)
        && !newsDraftNode.hasProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP)) {
      newsDraftNode.addMixin(NEWS_ACTIVITY_POSTING_MIXIN_TYPE);
    }
    newsDraftNode.setProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP, String.valueOf(news.isActivityPosted()));

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
    publicationService.changeState(newsDraftNode, PublicationDefaultStates.DRAFT, new HashMap<>());
    newsDraftNode.setProperty(EXO_BODY_PROP, imageProcessor.processImages(news.getBody(), newsDraftNode.getUUID(), "images"));
    spaceNewsRootNode.save();

    if (StringUtils.isNotEmpty(news.getUploadId())) {
      attachIllustration(newsDraftNode, news.getUploadId());
    }
    news.setId(newsDraftNode.getUUID());

    return news;
  }
  
  /**
   * Get all news
   * 
   * @return all news
   * @throws Exception when error
   */
  @Override
  public List<News> getNews(NewsFilter newsFilter) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 (repositoryService.getCurrentRepository()
                                                                   .getConfiguration()
                                                                   .getDefaultWorkspaceName()),
                                                 repositoryService.getCurrentRepository());
    List<News> listNews = new ArrayList<>();
    NewsQueryBuilder queyBuilder = new NewsQueryBuilder();
    try {
      StringBuilder sqlQuery = queyBuilder.buildQuery(newsFilter);
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
      ((QueryImpl) query).setOffset(newsFilter.getOffset());
      ((QueryImpl) query).setLimit(newsFilter.getLimit());

      NodeIterator it = query.execute().getNodes();
      while (it.hasNext()) {
        Node iterNode = it.nextNode();
        News news = getNewsById(iterNode.getUUID(), newsFilter.isDraftNews());
        listNews.add(news);
      }
      return listNews;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
  
  public int getNewsCount(NewsFilter newsFilter) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 (repositoryService.getCurrentRepository()
                                                                   .getConfiguration()
                                                                   .getDefaultWorkspaceName()),
                                                 repositoryService.getCurrentRepository());
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    try {
      StringBuilder sqlQuery = queryBuilder.buildQuery(newsFilter);
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);

      int count = 0;
      NodeIterator it = query.execute().getNodes();
      while (it.hasNext()) {
        it.nextNode();
        count++;
      }
      return count;
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
  
  /**
   * Publish a news
   *
   * @param newsId The id of the news to be published
   * @throws Exception when error
   */
  public void publishNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node newsNode = session.getNodeByUUID(news.getId());

    // Make News node readable by all users
    Node publishedRootNode = getPublishedNewsFolder(session);
    Calendar newsCreationCalendar = Calendar.getInstance();
    newsCreationCalendar.setTime(news.getCreationDate());
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(publishedRootNode, getNodeRelativePath(newsCreationCalendar));
    if (newsNode.canAddMixin("exo:privilegeable")) {
      newsNode.addMixin("exo:privilegeable");
    }
    ((ExtendedNode) newsNode).setPermission("*:/platform/users", SHARE_NEWS_PERMISSIONS);

    newsAttachmentsService.makeAttachmentsPublic(newsNode);

    linkManager.createLink(newsFolderNode, Utils.EXO_SYMLINK, newsNode, null);

    newsNode.setProperty("exo:pinned", true);
    newsNode.save();
  }
  
  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                 .getConfiguration()
                                                 .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    Node node = session.getNodeByUUID(newsId);
    if (node == null) {
      return null;
    }
    if (!editMode && node.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE).getString().equals(PublicationDefaultStates.DRAFT) && node.hasProperty(AuthoringPublicationConstant.LIVE_REVISION_PROP)) {
      String versionNodeUUID = node.getProperty(AuthoringPublicationConstant.LIVE_REVISION_PROP).getString();
      Node versionNode = node.getVersionHistory().getSession().getNodeByUUID(versionNodeUUID);
      node = versionNode.getNode("jcr:frozenNode");
    }
    News news = new News();

    Node originalNode;
    // Retrieve the real news id if it is a frozen node
    if (node.hasProperty("jcr:frozenUuid")) {
      String uuid = node.getProperty("jcr:frozenUuid").getString();
      originalNode = node.getSession().getNodeByUUID(uuid);
      news.setId(originalNode.getUUID());
    } 
    else {
      originalNode = node;
      news.setId(node.getUUID());
    }

    String portalName = PortalContainer.getCurrentPortalContainerName();
    String portalOwner = CommonsUtils.getCurrentPortalOwner();

    news.setTitle(getStringProperty(node, NodetypeConstant.EXO_TITLE));
    news.setSummary(getStringProperty(node, EXO_SUMMARY_PROP));
    String body = getStringProperty(node, EXO_BODY_PROP);
    String sanitizedBody = HTMLSanitizer.sanitize(body);
    sanitizedBody = StringEscapeUtils.unescapeHtml(sanitizedBody);
    sanitizedBody = sanitizedBody.replaceAll(HTML_AT_SYMBOL_ESCAPED_PATTERN, HTML_AT_SYMBOL_PATTERN);
    news.setBody(substituteUsernames(portalOwner, sanitizedBody));
    news.setAuthor(getStringProperty(node, "exo:author"));
    news.setCreationDate(getDateProperty(node, "exo:dateCreated"));
    news.setPublicationDate(getPublicationDate(node));
    news.setUpdater(getLastUpdater(node));
    news.setUpdateDate(getLastUpdatedDate(node));
    news.setDraftUpdater(getStringProperty(node, "exo:lastModifier"));
    news.setDraftUpdateDate(getDateProperty(node, "exo:dateModified"));
    news.setPath(getPath(node));
    if (node.hasProperty(StageAndVersionPublicationConstant.CURRENT_STATE)) {
      news.setPublicationState(node.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE).getString());
    }
    if (originalNode.hasProperty("exo:pinned")) {
      news.setPublished(originalNode.getProperty("exo:pinned").getBoolean());
    }
    if (originalNode.hasProperty("exo:archived")) {
      news.setArchived(originalNode.getProperty("exo:archived").getBoolean());
    }
    if (originalNode.hasProperty("exo:spaceId")) {
      news.setSpaceId(node.getProperty("exo:spaceId").getString());
    }
    if (originalNode.hasProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP)) {
      news.setDraftVisible(Boolean.valueOf(node.getProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP).getString()));
    } 
    else {
      news.setDraftVisible(false);
    }
    if (originalNode.hasProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP)) {
      news.setActivityPosted(Boolean.valueOf(node.getProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP).getString()));
    } 
    else {
      news.setActivityPosted(false);
    }
    StringBuilder newsUrl = new StringBuilder("");
    if (originalNode.hasProperty("exo:activities")) {
      String strActivities = originalNode.getProperty("exo:activities").getString();
      if(StringUtils.isNotEmpty(strActivities)) {
        String[] activities = strActivities.split(";");
        StringBuilder memberSpaceActivities = new StringBuilder();
        //TODO Check if can be retrieved from higher layer 
        org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
        String currentUsername = currentIdentity == null ? null : currentIdentity.getUserId();
        String newsActivityId = activities[0].split(":")[1];
        news.setActivityId(newsActivityId);
        Space newsPostedInSpace = spaceService.getSpaceById(activities[0].split(":")[0]);
        if (currentUsername != null && spaceService.isMember(newsPostedInSpace, currentUsername)) {
          newsUrl.append("/").append(portalName).append("/").append(portalOwner).append("/activity?id=").append(newsActivityId);
          news.setUrl(newsUrl.toString());
        } 
        else {
          newsUrl.append("/").append(portalName).append("/").append(portalOwner).append("/news/detail?newsId=").append(news.getId());
          news.setUrl(newsUrl.toString());
        }
        memberSpaceActivities.append(activities[0]).append(";");
        for (int i = 1; i < activities.length; i++) {
          Space space = spaceService.getSpaceById(activities[i].split(":")[0]);
          String activityId = activities[i].split(":")[1];
          if (space != null && currentUsername != null && spaceService.isMember(space, currentUsername) && activityManager.isActivityExists(activityId)) {
            memberSpaceActivities.append(activities[i]).append(";");
          }
        }
        news.setActivities(memberSpaceActivities.toString());
      } 
      else {
        newsUrl.append("/").append(portalName).append("/").append(portalOwner).append("/news/detail?newsId=").append(news.getId());
        news.setUrl(newsUrl.toString());
      }
    }
    if (node.hasProperty(AuthoringPublicationConstant.START_TIME_PROPERTY)) {
      news.setSchedulePostDate(node.getProperty(AuthoringPublicationConstant.START_TIME_PROPERTY).getString());
    }
    if (!node.hasProperty("exo:viewsCount")) {
      news.setViewsCount(0L);
    } 
    else {
      news.setViewsCount(node.getProperty("exo:viewsCount").getLong());
    }
    if (node.hasNode("illustration")) {
      Node illustrationContentNode = node.getNode("illustration").getNode(NodetypeConstant.JCR_CONTENT);
      byte[] bytes = IOUtils.toByteArray(illustrationContentNode.getProperty("jcr:data").getStream());
      news.setIllustration(bytes);
      news.setIllustrationUpdateDate(illustrationContentNode.getProperty("exo:dateModified").getDate().getTime());
      news.setIllustrationURL("/portal/rest/v1/news/" + news.getId() + "/illustration");
    }

    news.setAttachments(newsAttachmentsService.getNewsAttachments(node));

    Space space = spaceService.getSpaceById(news.getSpaceId());
    if (space != null) {
      String spaceName = space.getDisplayName();
      String currentUser = getCurrentUserId();
      boolean hiddenSpace = space.getVisibility().equals(Space.HIDDEN) && !spaceService.isMember(space, currentUser) && !spaceService.isSuperManager(currentUser);
      news.setHiddenSpace(hiddenSpace);
      boolean isSpaceMember =  spaceService.isSuperManager(getCurrentUserId()) || spaceService.isMember(space, getCurrentUserId());
      news.setSpaceMember(isSpaceMember);
      news.setSpaceDisplayName(spaceName);
      if(StringUtils.isNotEmpty(space.getGroupId())) {
        String spaceGroupId = space.getGroupId().split("/")[2];
        news.setSpaceAvatarUrl(space.getAvatarUrl());
        String spaceUrl = "/portal/g/:spaces:" + spaceGroupId +
                                "/" + space.getPrettyName();
        news.setSpaceUrl(spaceUrl);
      }
    }

    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor());
    if (identity != null && identity.getProfile() != null) {
      news.setAuthorDisplayName(identity.getProfile().getFullName());
      news.setAuthorAvatarUrl(identity.getProfile().getAvatarUrl());
    }

    if(StringUtils.isNotBlank(news.getUpdater())) {
      Identity updaterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getUpdater());
      if (updaterIdentity != null && updaterIdentity.getProfile() != null) {
        news.setUpdaterFullName(updaterIdentity.getProfile().getFullName());
      }
    }

    Identity draftUpdaterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getDraftUpdater());
    if (draftUpdaterIdentity != null && draftUpdaterIdentity.getProfile() != null) {
      news.setDraftUpdaterUserName(draftUpdaterIdentity.getRemoteId());
      news.setDraftUpdaterDisplayName(draftUpdaterIdentity.getProfile().getFullName());
    }

    return news;
  }
  
  public String getNewsIllustration(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    
    StringBuffer illustrationURL = new StringBuffer();
    String currentDomain = CommonsUtils.getCurrentDomain();
    if (!currentDomain.endsWith("/")) {
      currentDomain += "/";
    }
    Node newsNode = session.getNodeByUUID(news.getId());
    if (newsNode == null) {
      throw new ItemNotFoundException("Cannot find a node with UUID equals to " + news.getId() + ", it may not exist");
    }
    if (newsNode.hasNode("illustration")) {
      illustrationURL.append(currentDomain).append("portal/rest/v1/news/").append(news.getId()).append("/illustration");
    } 
    else {
      illustrationURL.append(currentDomain).append("news/images/news.png");
    }
    return illustrationURL.toString();
  }
  
  public News updateNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node newsNode = session.getNodeByUUID(news.getId());
    if (newsNode != null) {
      String processedBody = imageProcessor.processImages(news.getBody(), newsNode.getUUID(), "images");
      newsNode.setProperty(NodetypeConstant.EXO_TITLE, news.getTitle());
      newsNode.setProperty("exo:name", news.getTitle());
      newsNode.setProperty(EXO_SUMMARY_PROP, news.getSummary());
      news.setBody(processedBody);
      newsNode.setProperty(EXO_BODY_PROP, processedBody);
      newsNode.setProperty("exo:dateModified", Calendar.getInstance());

      // illustration
      if (StringUtils.isNotEmpty(news.getUploadId())) {
        attachIllustration(newsNode, news.getUploadId());
      } 
      else if ("".equals(news.getUploadId())) {
        removeIllustration(newsNode);
      }
      //draft visible
      if (newsNode.hasProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP)) {
        newsNode.setProperty(NEWS_DRAFT_VISIBILE_MIXIN_PROP, String.valueOf(news.isDraftVisible()));
      }

      //news activity
      if (newsNode.hasProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP)) {
        newsNode.setProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP, String.valueOf(news.isActivityPosted()));
      }

      // attachments
      news.setAttachments(newsAttachmentsService.updateNewsAttachments(news, newsNode));

      newsNode.save();

      // update name of node
      if (StringUtils.isNotBlank(news.getTitle()) && !news.getTitle().equals(newsNode.getName())) {
        String srcPath = newsNode.getPath();
        String destPath = (newsNode.getParent().getPath().equals("/") ? org.apache.commons.lang.StringUtils.EMPTY : newsNode.getParent().getPath()) + "/"
                + Utils.cleanName(news.getTitle()).trim();
        session.getWorkspace().move(srcPath, destPath);
      }

      if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
        publicationService.changeState(newsNode, PublicationDefaultStates.PUBLISHED, new HashMap<>());
        if (newsNode.isNodeType(MIX_NEWS_MODIFIERS)) {
          newsNode.removeMixin(MIX_NEWS_MODIFIERS);
          newsNode.save();
        }
      } 
      else if (PublicationDefaultStates.DRAFT.equals(news.getPublicationState())) {
        publicationService.changeState(newsNode, PublicationDefaultStates.DRAFT, new HashMap<>());
        Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentUserId());
        String currentIdentityId = currentIdentity.getId();
        if (!newsNode.isNodeType(MIX_NEWS_MODIFIERS)) {
          newsNode.addMixin(MIX_NEWS_MODIFIERS);
        }
        Value[] newsModifiers = new Value[0];
        boolean alreadyExist = false;
        if (newsNode.hasProperty(MIX_NEWS_MODIFIERS_PROP)) {
          newsModifiers = newsNode.getProperty(MIX_NEWS_MODIFIERS_PROP).getValues();
          alreadyExist = Arrays.stream(newsModifiers).map(value -> {
            try {
              return value.getString();
            } catch (RepositoryException e) {
              return null;
            }
          }).anyMatch(newsModifier -> newsModifier.equals(currentIdentityId));
        }
        if (!alreadyExist) {
          newsNode.setProperty(MIX_NEWS_MODIFIERS_PROP, ArrayUtils.add(newsModifiers, new StringValue(currentIdentityId)));
          newsNode.save();
        }
      }
    }
    return news;
  }
  
  @Override
  public void deleteNews(String newsId, boolean isDraft) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);

    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node node = session.getNodeByUUID(newsId);

    if (node.hasProperty("exo:activities")) {
      String newActivities = node.getProperty("exo:activities").getString();
      if (StringUtils.isNotEmpty(newActivities)) {
        if (isDraft && node.hasProperty(StageAndVersionPublicationConstant.CURRENT_STATE)
                && node.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE).getString().equals(PublicationDefaultStates.DRAFT)) {
          String versionNodeUUID = node.hasProperty(AuthoringPublicationConstant.LIVE_REVISION_PROP) ?
                  node.getProperty(AuthoringPublicationConstant.LIVE_REVISION_PROP).getString() : null;
          String versionName = node.getVersionHistory().getSession().getNodeByUUID(versionNodeUUID).getName();
          if (!versionName.isEmpty()) {
            node.restore(versionName, true);
            if (!node.isCheckedOut()) {
              node.checkout();
            }
            publicationService.changeState(node, PublicationDefaultStates.PUBLISHED, new HashMap<>());
            return;
          }
        }
        Stream.of(newActivities.split(";"))
                .map(activity -> activity.split(":")[1])
                .forEach(newsActivityId -> activityManager.deleteActivity(newsActivityId));
      }
    }
    Utils.removeDeadSymlinks(node, false);
    node.remove();
    session.save();
  }
  
  public News scheduleNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
     
                                                                  repositoryService.getCurrentRepository());
    News scheduledNews = null;
    try {
      Node scheduledNewsNode = session.getNodeByUUID(news.getId());
      if (scheduledNewsNode == null) {
        throw new ItemNotFoundException("Unable to find a node with an UUID equal to: " + news.getId());
      }
      String schedulePostDate = news.getSchedulePostDate();
      if (schedulePostDate != null) {
        ZoneId userTimeZone = StringUtils.isBlank(news.getTimeZoneId()) ? ZoneOffset.UTC : ZoneId.of(news.getTimeZoneId());
        String offsetTimeZone = String.valueOf(OffsetTime.now(userTimeZone).getOffset()).replace(":", "");
        schedulePostDate = schedulePostDate.concat(" ").concat(offsetTimeZone);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss" + "Z");
        Calendar startPublishedDate = Calendar.getInstance();
        startPublishedDate.setTime(format.parse(schedulePostDate));
        scheduledNewsNode.setProperty(AuthoringPublicationConstant.START_TIME_PROPERTY, startPublishedDate);
        scheduledNewsNode.setProperty(LAST_PUBLISHER, getCurrentUserId());
        scheduledNewsNode.setProperty("exo:pinned", news.isPublished());
        scheduledNewsNode.setProperty(NEWS_ACTIVITY_POSTED_MIXIN_PROP, news.isActivityPosted());
        scheduledNewsNode.save();
        publicationService.changeState(scheduledNewsNode, PublicationDefaultStates.STAGED, new HashMap<>());
      }
      scheduledNews = getNewsById(news.getId(), false);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return scheduledNews;
  }
  
  public void updateNewsActivities(String newsActivityId, News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    if (newsActivityId != null && !StringUtils.isEmpty(news.getId())) {
      Node newsNode = session.getNodeByUUID(news.getId());
      if (newsNode.hasProperty("exo:activities")) {
        String updatedNewsActivities = news.getSpaceId().concat(":").concat(newsActivityId);
        newsNode.setProperty("exo:activities", updatedNewsActivities);
        newsNode.save();
        news.setActivities(updatedNewsActivities);
      }
    }
  }
  
  private void attachIllustration(Node newsNode, String uploadId) throws Exception {
    UploadResource uploadedResource = uploadService.getUploadResource(uploadId);
    if (uploadedResource == null) {
      throw new Exception("Cannot attach uploaded file " + uploadId + ", it may not exist");
    }

    boolean illustrationExists = newsNode.hasNode("illustration");
    Node illustrationNode;
    if (illustrationExists) {
      illustrationNode = newsNode.getNode("illustration");
    } 
    else {
      illustrationNode = newsNode.addNode("illustration", NodetypeConstant.NT_FILE);
    }
    illustrationNode.setProperty(NodetypeConstant.EXO_TITLE, uploadedResource.getFileName());
    Node resourceNode;
    if (illustrationExists) {
      resourceNode = illustrationNode.getNode(NodetypeConstant.JCR_CONTENT);
    } 
    else {
      resourceNode = illustrationNode.addNode(NodetypeConstant.JCR_CONTENT, NodetypeConstant.NT_RESOURCE);
    }
    resourceNode.setProperty(NodetypeConstant.JCR_MIME_TYPE, uploadedResource.getMimeType());
    Calendar now = Calendar.getInstance();
    resourceNode.setProperty(NodetypeConstant.JCR_LAST_MODIFIED, now);
    resourceNode.setProperty("exo:dateModified", now);
    String fileDiskLocation = uploadedResource.getStoreLocation();
    try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
      resourceNode.setProperty("jcr:data", inputStream);
      newsNode.save();
    }
  }
  
  /**
   * Get the root folder for published news
   *
   * @return the published folder node
   * @throws Exception when error
   */
  private Node getPublishedNewsFolder(Session session) throws Exception {
    Node applicationDataNode = (Node) session.getItem(APPLICATION_DATA_PATH);
    Node newsRootNode;
    if (!applicationDataNode.hasNode(NEWS_NODES_FOLDER)) {
      newsRootNode = applicationDataNode.addNode(NEWS_NODES_FOLDER, "nt:unstructured");
      applicationDataNode.save();
    } 
    else {
      newsRootNode = applicationDataNode.getNode(NEWS_NODES_FOLDER);
    }
    Node publishedRootNode;
    if (!newsRootNode.hasNode(PUBLISHED_NEWS_NODES_FOLDER)) {
      publishedRootNode = newsRootNode.addNode(PUBLISHED_NEWS_NODES_FOLDER, "nt:unstructured");
      newsRootNode.save();
    } 
    else {
      publishedRootNode = newsRootNode.getNode(PUBLISHED_NEWS_NODES_FOLDER);
    }
    return publishedRootNode;
  }
  
  private Node getSpaceNewsRootNode(String spaceId, Session session) throws RepositoryException {
    Space space = spaceService.getSpaceById(spaceId);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    String spaceParentPath = groupPath + space.getGroupId();

    Node spaceRootNode = (Node) session.getItem(spaceParentPath);

    Node spaceNewsRootNode;
    if (!spaceRootNode.hasNode(NEWS_NODES_FOLDER)) {
      spaceNewsRootNode = spaceRootNode.addNode(NEWS_NODES_FOLDER, "nt:unstructured");
      if (spaceNewsRootNode.canAddMixin("exo:privilegeable")) {
        spaceNewsRootNode.addMixin("exo:privilegeable");
      }
      Map<String, String[]> permissions = new HashMap<>();
      permissions.put("*:" + space.getGroupId(), PermissionType.ALL);
      ((ExtendedNode) spaceNewsRootNode).setPermissions(permissions);

      spaceRootNode.save();
    } 
    else {
      spaceNewsRootNode = spaceRootNode.getNode(NEWS_NODES_FOLDER);
    }
    return spaceNewsRootNode;
  }
  
  private String getNodeRelativePath(Calendar now) {
    return now.get(Calendar.YEAR) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.DAY_OF_MONTH);
  }
  
  private String getStringProperty(Node node, String propertyName) throws RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getString();
    }

    return "";
  }
  
  private Date getDateProperty(Node node, String propertyName) throws RepositoryException {
    if (node.hasProperty(propertyName)) {
      return node.getProperty(propertyName).getDate().getTime();
    }

    return null;
  }
  
  public String substituteUsernames(String portalOwner, String message) {
    if (message == null || message.trim().isEmpty()) {
      return message;
    }
    //
    Matcher matcher = MENTION_PATTERN.matcher(message);

    // Replace all occurrences of pattern in input
    StringBuffer buf = new StringBuffer();
    while (matcher.find()) {
      // Get the match result
      String username = matcher.group().substring(1);
      if (username == null || username.isEmpty()) {
        continue;
      }
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
      if (identity == null || identity.isDeleted() || !identity.isEnable()) {
        continue;
      }
      try {
        username = LinkProvider.getProfileLink(username, portalOwner);
      } catch (Exception e) {
        LOG.warn("Error while retrieving link for profile of user {}", username, e);
        continue;
      }
      // Insert replacement
      if (username != null) {
        matcher.appendReplacement(buf, username);
      }
    }
    if (buf.length() > 0) {
      matcher.appendTail(buf);
      return buf.toString();
    }
    return message;
  }
  
  /**
   * Return the date of the first published version of the node
   * 
   * @param node The News node
   * @return The date of the first published version of the node
   * @throws RepositoryException when error
   */
  private Date getPublicationDate(Node node) throws RepositoryException {
    VersionNode versionNode = new VersionNode(node, node.getSession());
    List<VersionNode> versions = versionNode.getChildren();
    if (!versions.isEmpty()) {
      versions.sort(Comparator.comparingInt(v -> Integer.parseInt(v.getName())));
      return versions.get(0).getCreatedTime().getTime();
    }

    return null;
  }
  
  /**
   * Return the author of the last published version of the node
   *
   * @param node The News node
   * @return The author of the last published version of the node
   * @throws RepositoryException when error
   */
  private String getLastUpdater(Node node) throws RepositoryException {
    VersionNode lastUpdatedVersion = getLastUpdatedVersion(node);
    if(lastUpdatedVersion != null) {
      return lastUpdatedVersion.getAuthor();
    } 
    else {
      return getStringProperty(node, "exo:lastModifier");
    }
  }
  
  /**
   * Return the date of the last published version of the node
   *
   * @param node The News node
   * @return The date of the last published version of the node
   * @throws RepositoryException
   */
  private Date getLastUpdatedDate(Node node) throws RepositoryException {
    VersionNode lastUpdatedVersion = getLastUpdatedVersion(node);
    if(lastUpdatedVersion != null) {
      return lastUpdatedVersion.getCreatedTime().getTime();
    } 
    else {
      return getDateProperty(node, "exo:dateModified");
    }
  }
  
  private VersionNode getLastUpdatedVersion(Node node) throws RepositoryException {
    VersionNode versionNode = new VersionNode(node, node.getSession());
    List<VersionNode> versions = versionNode.getChildren();
    if (!versions.isEmpty()) {
      versions.sort(Comparator.comparingInt(v -> Integer.parseInt(v.getName())));
      return versions.get(versions.size() - 1);
    }

    return null;
  }
  
  private String getCurrentUserId() {
    org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
    return currentIdentity != null ? currentIdentity.getUserId() : null;
  }
  
  private org.exoplatform.services.security.Identity getCurrentIdentity() {
    ConversationState conversationState = ConversationState.getCurrent();
    return conversationState != null ? conversationState.getIdentity() : null;
  }
  
  private String getPath(Node node) throws Exception {
    String nodePath = null;
    NodeLocation nodeLocation = NodeLocation.getNodeLocationByNode(node);

    if (nodeLocation != null) {
      StringBuilder sb = new StringBuilder();
      sb.append("/").append(nodeLocation.getRepository()).append("/").append(nodeLocation.getWorkspace()).append(node.getPath());
      nodePath = Text.escapeIllegalJcrChars(sb.toString());
    }

    return nodePath;
  }
  
  private void removeIllustration(Node newsNode) throws Exception {
    if (newsNode.hasNode("illustration")) {
      newsNode.getNode("illustration").remove();
      newsNode.save();
    }
  }
  
  /**
   * Get the root folder for published news
   *
   * @return the published folder node
   * @throws Exception when error
   */
  private Node getPublishedNewsFolder() throws Exception {
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
    } 
    else {
      newsRootNode = applicationDataNode.getNode(NEWS_NODES_FOLDER);
    }
    Node publishedRootNode;
    if (!newsRootNode.hasNode(PUBLISHED_NEWS_NODES_FOLDER)) {
      publishedRootNode = newsRootNode.addNode(PUBLISHED_NEWS_NODES_FOLDER, "nt:unstructured");
      newsRootNode.save();
    } 
    else {
      publishedRootNode = newsRootNode.getNode(PUBLISHED_NEWS_NODES_FOLDER);
    }
    return publishedRootNode;
  }
  
  public void markAsRead(News news, String userId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node newsNode = session.getNodeByUUID(news.getId());
    if (newsNode == null) {
      throw new Exception("Unable to find a node with an UUID equal to: " + news.getId());
    }
    if (!newsNode.hasProperty("exo:viewers")) {
      newsNode.setProperty("exo:viewers", "");
    }

    String newsViewers = newsNode.getProperty("exo:viewers").getString();
    if (newsViewers.isEmpty()) {
      newsViewers = newsViewers.concat(userId);
    } 
    else {
      newsViewers = newsViewers.concat(",").concat(userId);
    }
    newsNode.setProperty("exo:viewers", newsViewers);
    Long newsViewsCount = news.getViewsCount() == null ? (long) 1 : news.getViewsCount() + 1;
    newsNode.setProperty("exo:viewsCount", newsViewsCount);
    newsNode.save();
  }
  
  public boolean isCurrentUserInNewsViewers(String newsId, String userId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());

    Node newsNode = session.getNodeByUUID(newsId);
    if (newsNode == null) {
      throw new Exception("Unable to find a node with an UUID equal to: " + newsId);
    }
    if (!newsNode.hasProperty("exo:viewers")) {
      newsNode.setProperty("exo:viewers", "");
    }
    String newsViewers = newsNode.getProperty("exo:viewers").getString();
    boolean isCurrentUserInNewsViewers = false;
    if (!newsViewers.isEmpty()) {
      String[] newsViewersArray = newsViewers.split(",");
      isCurrentUserInNewsViewers = Arrays.stream(newsViewersArray).anyMatch(userId::equals);
    }
    return isCurrentUserInNewsViewers;
  }
  
  public void unpublishNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    News news = getNewsById(newsId, false);
    if (news == null) {
      throw new Exception("Unable to find a news with an id equal to: " + newsId);
    }

    // Update News node
    Node newsNode = session.getNodeByUUID(newsId);
    if (newsNode == null) {
      throw new Exception("Unable to find a node with an UUID equal to: " + newsId);
    }
    newsNode.setProperty("exo:pinned", false);
    if(newsNode.isNodeType("exo:privilegeable")) {
      ((ExtendedNode) newsNode).removePermission("*:/platform/users");
    }
    newsNode.save();

    newsAttachmentsService.unmakeAttachmentsPublic(newsNode);

    // Remove publish symlink
    Node publishedRootNode = getPublishedNewsFolder();
    if (publishedRootNode == null) {
      throw new Exception("Unable to find the root published folder: /Application Data/News/pinned");
    }
    Calendar newsCreationCalendar = Calendar.getInstance();
    newsCreationCalendar.setTime(news.getCreationDate());
    Node newsFolderNode = dataDistributionType.getOrCreateDataNode(publishedRootNode, getNodeRelativePath(newsCreationCalendar));
    if (newsFolderNode == null) {
      throw new Exception("Unable to find the parent node of the current published node");
    }
    Node publishedNode = newsFolderNode.getNode(newsNode.getName());
    if (publishedNode == null) {
      throw new Exception("Unable to find the current published node");
    }
    publishedNode.remove();
    newsFolderNode.save();
  }
  
  public News unScheduleNews(News news) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),

                                                 repositoryService.getCurrentRepository());
    News draftNews = null;
    try {
      Node unScheduledNewsNode = session.getNodeByUUID(news.getId());
      if (unScheduledNewsNode == null) {
        throw new ItemNotFoundException("Unable to find a node with an UUID equal to: " + news.getId());
      }
      String schedulePostDate = news.getSchedulePostDate();
      if (schedulePostDate != null) {
        unScheduledNewsNode.setProperty(LAST_PUBLISHER, getCurrentUserId());
        unScheduledNewsNode.save();
        publicationService.changeState(unScheduledNewsNode, PublicationDefaultStates.DRAFT, new HashMap<>());
      }
      draftNews = getNewsById(news.getId(), false);
      draftNews.setSchedulePostDate(null);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    return draftNews;
  }
  
  /**
   * Archive a news
   *
   * @param newsId The id of the news to be archived
   * @throws Exception when error
   */
  public void archiveNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    Node newsNode = session.getNodeByUUID(newsId);
    if (newsNode == null) {
      throw new ItemNotFoundException("Unable to find a node with an UUID equal to: " + newsId);
    }
    boolean isPublished = newsNode.getProperty("exo:pinned").getBoolean();
    if (isPublished) {
      unpublishNews(newsId);
    }
    newsNode.setProperty("exo:archived", true);
    newsNode.save();
  }
  
  /**
   * Unarchive a news
   *
   * @param newsId The id of the news to be unarchived
   * @throws Exception when error
   */
  public void unarchiveNews(String newsId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSystemSessionProvider(null);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    Node newsNode = session.getNodeByUUID(newsId);
    if (newsNode == null) {
      throw new ItemNotFoundException("Unable to find a node with an UUID equal to: " + newsId);
    }
    newsNode.setProperty("exo:archived", false);
    newsNode.save();
  }
  
  /**
   * Search news with the given text
   * 
   * @param filter news filter
   * @param lang language
   * @throws Exception when error
   */
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {

    SearchContext context = new SearchContext(null, null);
    context.lang(lang);
    List<News> newsList = new ArrayList<>();
    List<SearchResult> searchResults = newsSearchConnector.search(filter,
                                                                  filter.getOffset(),
                                                                  filter.getLimit(),
                                                                  "relevancy",
                                                                  "desc");
    searchResults.forEach(res -> {
      try {
        Node newsNode = ((NewsSearchResult) res).getNode();
        News news = getNewsById(newsNode.getUUID(), false);
        newsList.add(news);

      } catch (Exception e) {
        LOG.error("Error while processing search result in News", e);
      }
    });
    return newsList;
  }
  
  @Override
  public void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws IllegalAccessException, ObjectNotFoundException {
    String newsId = news.getId();
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      ExtendedNode newsNode = (ExtendedNode) getNewsNodeById(newsId, sessionProvider);
      if (newsNode == null) {
        throw new ObjectNotFoundException("News with id " + newsId + "wasn't found");
      }
      // Update news node permissions
      if (newsNode.canAddMixin("exo:privilegeable")) {
        newsNode.addMixin("exo:privilegeable");
      }
      newsNode.setPermission("*:" + space.getGroupId(), SHARE_NEWS_PERMISSIONS);
      newsNode.save();
      if (sharedActivityId != null) {
        if (newsNode.hasProperty("exo:activities")) {
          String activities = newsNode.getProperty("exo:activities").getString();
          activities = activities.concat(";").concat(space.getId()).concat(":").concat(sharedActivityId);
          newsNode.setProperty("exo:activities", activities);
        } 
        else {
          newsNode.setProperty("exo:activities", sharedActivityId);
        }
        newsNode.save();
      }
    } catch (RepositoryException e) {
      throw new IllegalStateException("Error while sharing news with id " + newsId + " to space " + space.getId() + " by user"
          + userIdentity.getId(), e);
    }
  }
  
  public Node getNewsNodeById(String newsId, SessionProvider sessionProvider) throws RepositoryException, ItemNotFoundException {
    Session session = getSession(sessionProvider);
    Node node = session.getNodeByUUID(newsId);
    return node;
  }
  
  private Session getSession(SessionProvider sessionProvider) throws RepositoryException {
    return sessionProvider.getSession(repositoryService.getCurrentRepository()
                                                       .getConfiguration()
                                                       .getDefaultWorkspaceName(),
                                      repositoryService.getCurrentRepository());
  }
  
}