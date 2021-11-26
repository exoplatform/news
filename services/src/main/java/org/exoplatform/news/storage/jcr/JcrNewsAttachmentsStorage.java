package org.exoplatform.news.storage.jcr;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.documents.DocumentService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

import javax.jcr.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class JcrNewsAttachmentsStorage implements NewsAttachmentsStorage {

  private static final Log LOG = ExoLogger.getLogger(JcrNewsAttachmentsStorage.class);

  public static final String NEWS_ATTACHMENTS_NODES_FOLDER = "News Attachments";

  private SessionProviderService sessionProviderService;

  private RepositoryService repositoryService;

  private NodeHierarchyCreator nodeHierarchyCreator;

  private DataDistributionType dataDistributionType;

  private SpaceService spaceService;

  private UploadService uploadService;

  private DocumentService documentService;

  public JcrNewsAttachmentsStorage(SessionProviderService sessionProviderService, RepositoryService repositoryService,
                                    NodeHierarchyCreator nodeHierarchyCreator, DataDistributionManager dataDistributionManager,
                                    SpaceService spaceService, UploadService uploadService, DocumentService documentService) {
    this.sessionProviderService = sessionProviderService;
    this.repositoryService = repositoryService;
    this.nodeHierarchyCreator = nodeHierarchyCreator;
    this.dataDistributionType = dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE);
    this.spaceService = spaceService;
    this.uploadService = uploadService;
    this.documentService = documentService;
  }

  /**
   * Get the list of attachments of the given news node
   * @param newsNode The news node
   * @return The list of attachments
   * @throws Exception when error
   */
  @Override
  public List<NewsAttachment> getNewsAttachments(Node newsNode) throws Exception {
    List<NewsAttachment> attachments = new ArrayList<>();
    for (Node attachmentNode : getAttachmentsNodesOfNews(newsNode)) {
      try {
        attachments.add(convertNodeToNewsAttachment(attachmentNode));
      } catch (RepositoryException e) {
        LOG.error("Error while fetching attachment of News " + newsNode.getUUID(), e);
      }
    }

    return attachments;
  }

  /**
   * Get the attachment with the given id
   * @param attachmentId The attachment id
   * @return The attachment
   * @throws Exception when error
   */
  @Override
  public NewsAttachment getNewsAttachment(String attachmentId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession((repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName()),
            repositoryService.getCurrentRepository());

    Node attachmentNode = session.getNodeByUUID(attachmentId);
    if(attachmentNode != null) {
      return convertNodeToNewsAttachment(attachmentNode);
    }
    return null;
  }

  /**
   * Get data stream of the attachment with the given id
   * @param attachmentId The attachment id
   * @return The attachment data stream
   * @throws Exception when error
   */
  @Override
  public InputStream getNewsAttachmentStream(String attachmentId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession((repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName()),
            repositoryService.getCurrentRepository());

    Node attachmentNode = session.getNodeByUUID(attachmentId);
    if(attachmentNode != null) {
      Node resourceNode = attachmentNode.getNode("jcr:content");
      return resourceNode.getProperty("jcr:data").getStream();
    }
    return null;
  }

  /**
   * Get the URL to open the attachment with the given id
   * @param attachmentId The attachment id
   * @return The URl to open the attachment
   * @throws Exception when error
   */
  @Override
  public String getNewsAttachmentOpenUrl(String attachmentId) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession((repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName()),
            repositoryService.getCurrentRepository());

    Node attachmentNode = session.getNodeByUUID(attachmentId);
    if(attachmentNode != null) {
      return documentService.getLinkInDocumentsApp(attachmentNode.getPath());
    }
    return null;
  }


  /**
   * Adds new attachments to the News and remove the attachments to remove.
   * New attachments have only an uploadId, no id, so all attachments of the updatedNews with an uploadId are considered as new attachments.
   *
   * @param updatedNews The updated News
   * @param newsNode    The existing News node
   * @throws RepositoryException when error
   */
  @Override
  public List<NewsAttachment> updateNewsAttachments(News updatedNews, Node newsNode) throws Exception {
    List<NewsAttachment> updatedAttachments = Optional.ofNullable(updatedNews.getAttachments()).orElse(new ArrayList<>());

    // attachments to remove
    List<String> existingAttachmentsIds = new ArrayList<>();
    if (newsNode.hasProperty("exo:attachmentsIds")) {
      Value[] attachmentsIdsProperty = newsNode.getProperty("exo:attachmentsIds").getValues();
      existingAttachmentsIds = Arrays.stream(attachmentsIdsProperty).map(value -> {
        try {
          return value.getString();
        } catch (RepositoryException e) {
          return null;
        }
      }).collect(Collectors.toList());
    }

    List<String> updatedAttachmentsIds = updatedAttachments.stream()
            .map(NewsAttachment::getId)
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toList());

    existingAttachmentsIds.stream()
            .filter(attachmentId -> !updatedAttachmentsIds.contains(attachmentId))
            .forEach(attachmentId -> removeAttachment(newsNode, attachmentId));

    // new attachments
    List<NewsAttachment> updatedAttachmentsWithIds = new ArrayList<>();
    for (NewsAttachment updatedAttachment : updatedAttachments) {
      if (StringUtils.isEmpty(updatedAttachment.getId()) && StringUtils.isNotEmpty(updatedAttachment.getUploadId())) {
        try {
          String newAttachmentId = addAttachmentFromUploadedResource(newsNode, updatedAttachment.getUploadId());
          updatedAttachment.setId(newAttachmentId);
          updatedAttachment.setUploadId(null);
        } catch (Exception e) {
          LOG.error("Error while adding attachment to news " + updatedNews.getId(), e);
        }
      }
      //Attachments from existing resource
      else if (StringUtils.isEmpty(updatedAttachment.getUploadId()) && !existingAttachmentsIds.contains(updatedAttachment.getId())) {
        try {
          addAttachmentFromExistingResource(newsNode, updatedAttachment.getId());
        } catch (Exception e) {
          LOG.error("Error while adding attachment to news " + updatedAttachment.getId(), e);
        }
      }
      updatedAttachmentsWithIds.add(updatedAttachment);
    }
    return updatedAttachmentsWithIds;
  }

  /**
   * Add the resource with the given upload id to the given news node
   * @param newsNode The news node
   * @param uploadId The id of the uploaded resource
   * @return The id of the added attachment
   * @throws Exception when error
   */
  @Override
  public String addAttachmentFromUploadedResource(Node newsNode, String uploadId) throws Exception {
    UploadResource uploadedResource = uploadService.getUploadResource(uploadId);
    if (uploadedResource == null) {
      throw new Exception("Cannot attach uploaded file " + uploadId + ", it may not exist");
    }
    if(!newsNode.hasProperty("exo:spaceId")) {
      throw new Exception("Cannot get space id of news " + newsNode.getUUID());
    }

    Node spaceNewsAttachmentsRootNode = getSpaceNewsAttachmentsRootNode(newsNode.getProperty("exo:spaceId").getString(), newsNode.getSession());
    Node newsAttachmentsFolderNode = dataDistributionType.getOrCreateDataNode(spaceNewsAttachmentsRootNode, getNodeRelativePath(Calendar.getInstance()));

    Node attachmentNode = newsAttachmentsFolderNode.addNode(uploadedResource.getFileName(), "nt:file");
    attachmentNode.addMixin("mix:versionable");
    attachmentNode.setProperty("exo:title", uploadedResource.getFileName());
    Node resourceNode = attachmentNode.addNode("jcr:content", "nt:resource");
    resourceNode.setProperty("jcr:mimeType", uploadedResource.getMimeType());
    Calendar now = Calendar.getInstance();
    resourceNode.setProperty("jcr:lastModified", now);
    resourceNode.setProperty("exo:dateModified", now);
    String fileDiskLocation = uploadedResource.getStoreLocation();
    if(fileDiskLocation != null) {
      try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
        resourceNode.setProperty("jcr:data", inputStream);
        newsAttachmentsFolderNode.save();
      }
    } else {
      newsAttachmentsFolderNode.save();
    }

    Value[] attachmentsIdsProperty;
    if(newsNode.hasProperty("exo:attachmentsIds")) {
      attachmentsIdsProperty = newsNode.getProperty("exo:attachmentsIds").getValues();
    } else {
      attachmentsIdsProperty = new Value[0];
    }
    newsNode.setProperty("exo:attachmentsIds", ArrayUtils.add(attachmentsIdsProperty, new StringValue(attachmentNode.getUUID())));
    newsNode.save();

    return attachmentNode.getUUID();
  }

  /**
   * Add an existing resource with the given id to the given news node
   *
   * @param newsNode   The news node
   * @param resourceId The id of the existing resource
   * @throws Exception when error
   */
  @Override
  public void addAttachmentFromExistingResource(Node newsNode, String resourceId) throws Exception {
    Value[] attachmentsIdsProperty;
    if (newsNode.hasProperty("exo:attachmentsIds")) {
      attachmentsIdsProperty = newsNode.getProperty("exo:attachmentsIds").getValues();
    } else {
      attachmentsIdsProperty = new Value[0];
    }
    newsNode.setProperty("exo:attachmentsIds", ArrayUtils.add(attachmentsIdsProperty, new StringValue(resourceId)));
    newsNode.save();
  }

  @Override
  public void makeAttachmentsPublic(Node newsNode) throws Exception {
    for (Node attachmentNode : getAttachmentsNodesOfNews(newsNode)) {
      try {
        if (attachmentNode.canAddMixin("exo:privilegeable")) {
          attachmentNode.addMixin("exo:privilegeable");
        }
        ((ExtendedNode)attachmentNode).setPermission("*:/platform/users", new String[] { PermissionType.READ });
        attachmentNode.save();
      } catch (Exception e) {
        LOG.error("Cannot make News attachment " + attachmentNode.getUUID() + " of News " + newsNode.getUUID() + " public", e);
      }
    }
  }

  @Override
  public void unmakeAttachmentsPublic(Node newsNode) throws Exception {
    for (Node attachmentNode : getAttachmentsNodesOfNews(newsNode)) {
      try {
        if (attachmentNode.isNodeType("exo:privilegeable")) {
          ((ExtendedNode)attachmentNode).removePermission("*:/platform/users");
          attachmentNode.save();
        }
      } catch (Exception e) {
        LOG.error("Cannot remove public access of News attachment " + attachmentNode.getUUID() + " of News " + newsNode.getUUID(), e);
      }
    }
  }

  /**
   * Remove the attachment with the given id from the given news node
   * @param newsNode The news node
   * @param attachmentId The attachment id
   */
  @Override
  public void removeAttachment(Node newsNode, String attachmentId) {
    try {
      Session session = newsNode.getSession();
      Node attachmentNode = session.getNodeByUUID(attachmentId);
      if(attachmentNode != null) {
        attachmentNode.remove();
      }
      Value[] attachmentsIdsProperty;
      if(newsNode.hasProperty("exo:attachmentsIds")) {
        attachmentsIdsProperty = newsNode.getProperty("exo:attachmentsIds").getValues();
        newsNode.setProperty("exo:attachmentsIds", ArrayUtils.removeElement(attachmentsIdsProperty, new StringValue(attachmentId)));
        newsNode.save();
      }
    } catch (Exception e) {
      LOG.error("Error when deleting attachment node " + attachmentId, e);
    }
  }

  protected NewsAttachment convertNodeToNewsAttachment(Node attachmentNode) throws Exception {
    String mimetype = "";
    int attachmentSize = 0;

    Node resourceNode = attachmentNode.getNode("jcr:content");
    if(resourceNode != null) {
      if (resourceNode.hasProperty("jcr:mimeType")) {
        mimetype = resourceNode.getProperty("jcr:mimeType").getString();
      }
      InputStream attachmentStream = resourceNode.getProperty("jcr:data").getStream();
      byte[] attachmentBytes = IOUtils.toByteArray(attachmentStream);
      attachmentSize = attachmentBytes.length;
    }

    NewsAttachment attachment = new NewsAttachment(attachmentNode.getUUID(), null, attachmentNode.getName(), mimetype, attachmentSize);
    return attachment;
  }

  private Node getSpaceNewsAttachmentsRootNode(String spaceId, Session session) throws RepositoryException {
    Space space = spaceService.getSpaceById(spaceId);
    String groupPath = nodeHierarchyCreator.getJcrPath(BasePath.CMS_GROUPS_PATH);
    String spaceDocumentsFolderPath = groupPath + space.getGroupId() + "/Documents";

    Node spaceDocumentsFolderNode = (Node) session.getItem(spaceDocumentsFolderPath);

    Node spaceNewsRootNode;
    if(!spaceDocumentsFolderNode.hasNode(NEWS_ATTACHMENTS_NODES_FOLDER)) {
      spaceNewsRootNode = spaceDocumentsFolderNode.addNode(NEWS_ATTACHMENTS_NODES_FOLDER, "nt:unstructured");
      if(spaceNewsRootNode.canAddMixin("exo:privilegeable")) {
        spaceNewsRootNode.addMixin("exo:privilegeable");
      }
      Map<String, String[]> permissions = new HashMap<>();
      permissions.put("*:/platform/administrators", PermissionType.ALL);
      permissions.put("*:" + space.getGroupId(), PermissionType.ALL);
      ((ExtendedNode) spaceNewsRootNode).setPermissions(permissions);

      spaceDocumentsFolderNode.save();
    } else {
      spaceNewsRootNode = spaceDocumentsFolderNode.getNode(NEWS_ATTACHMENTS_NODES_FOLDER);
    }
    return spaceNewsRootNode;
  }

  private List<Node> getAttachmentsNodesOfNews(Node newsNode) throws Exception {
    List<Node> attachmentsNode = new ArrayList<>();
    if(newsNode != null && newsNode.hasProperty("exo:attachmentsIds")) {
      Property attachmentsIdsProperty = newsNode.getProperty("exo:attachmentsIds");
      if (attachmentsIdsProperty != null) {
        for (Value value : attachmentsIdsProperty.getValues()) {
          String attachmentId = value.getString();
          try {
            Node attachmentNode = newsNode.getSession().getNodeByUUID(attachmentId);
            if (!attachmentNode.getPath().startsWith("/Quarantine/")) {
              attachmentsNode.add(attachmentNode);
            }
          } catch (Exception e) {
            LOG.error("Cannot get News attachment " + attachmentId + " of News " + newsNode.getUUID(), e);
          }
        }
      }
    }

    return attachmentsNode;
  }

  private String getNodeRelativePath(Calendar now) {
    return now.get(Calendar.YEAR) + "/" + (now.get(Calendar.MONTH) + 1) + "/" + now.get(Calendar.DAY_OF_MONTH);
  }
}
