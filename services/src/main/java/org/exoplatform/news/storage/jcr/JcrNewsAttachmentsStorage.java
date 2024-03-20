package org.exoplatform.news.storage.jcr;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;

import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;

public class JcrNewsAttachmentsStorage implements NewsAttachmentsStorage {

  private static final Log       LOG                           = ExoLogger.getLogger(JcrNewsAttachmentsStorage.class);

  public static final String     NEWS_ATTACHMENTS_NODES_FOLDER = "News Attachments";


  /**
   * Get the list of attachments of the given news node
   * 
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

  @Override
  public void makeAttachmentsPublic(Node newsNode) throws Exception {
    for (Node attachmentNode : getAttachmentsNodesOfNews(newsNode)) {
      try {
        if (attachmentNode.canAddMixin("exo:privilegeable")) {
          attachmentNode.addMixin("exo:privilegeable");
        }
        ((ExtendedNode) attachmentNode).setPermission("any", new String[] { PermissionType.READ });
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
          ((ExtendedNode) attachmentNode).removePermission("any");
          attachmentNode.save();
        }
      } catch (Exception e) {
        LOG.error("Cannot remove public access of News attachment " + attachmentNode.getUUID() + " of News " + newsNode.getUUID(),
                  e);
      }
    }
  }

  @Override
  public void shareAttachments(Node newsNode, Space space) {
    try {
      for (Node attachmentNode : getAttachmentsNodesOfNews(newsNode)) {
        if (attachmentNode.canAddMixin("exo:privilegeable")) {
          attachmentNode.addMixin("exo:privilegeable");
        }
        ((ExtendedNode) attachmentNode).setPermission("*:" + space.getGroupId(), new String[] { PermissionType.READ });
        attachmentNode.save();
      }
    } catch (Exception e) {
      LOG.error("Cannot share News attachment of News " + newsNode, e);
    }
  }

  private NewsAttachment convertNodeToNewsAttachment(Node attachmentNode) throws Exception {
    String mimetype = "";
    int attachmentSize = 0;

    Node resourceNode = attachmentNode.getNode("jcr:content");
    if (resourceNode != null) {
      if (resourceNode.hasProperty("jcr:mimeType")) {
        mimetype = resourceNode.getProperty("jcr:mimeType").getString();
      }
      InputStream attachmentStream = resourceNode.getProperty("jcr:data").getStream();
      byte[] attachmentBytes = IOUtils.toByteArray(attachmentStream);
      attachmentSize = attachmentBytes.length;
    }

    NewsAttachment attachment = new NewsAttachment(attachmentNode.getUUID(),
                                                   null,
                                                   attachmentNode.getName(),
                                                   mimetype,
                                                   attachmentSize);
    return attachment;
  }

  private List<Node> getAttachmentsNodesOfNews(Node newsNode) throws Exception {
    List<Node> attachmentsNode = new ArrayList<>();
    if (newsNode != null && newsNode.hasProperty("exo:attachmentsIds")) {
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
}
