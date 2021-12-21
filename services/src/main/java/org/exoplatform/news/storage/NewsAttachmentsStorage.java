package org.exoplatform.news.storage;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.social.core.space.model.Space;

import javax.jcr.Node;
import java.io.InputStream;
import java.util.List;

public interface NewsAttachmentsStorage {

  List<NewsAttachment> getNewsAttachments(Node newsNode) throws Exception;

  NewsAttachment getNewsAttachment(String attachmentId) throws Exception;

  InputStream getNewsAttachmentStream(String attachmentId) throws Exception;

  String getNewsAttachmentOpenUrl(String attachmentId) throws Exception;

  List<NewsAttachment> updateNewsAttachments(News updatedNews, Node newsNode) throws Exception;

  String addAttachmentFromUploadedResource(Node newsNode, String uploadId) throws Exception;

  void addAttachmentFromExistingResource(Node newsNode, String attachmentId) throws Exception;

  void makeAttachmentsPublic(Node newsNode) throws Exception;

  void unmakeAttachmentsPublic(Node newsNode) throws Exception;

  void removeAttachment(Node newsNode, String attachmentId);

  void makeAttachmentsShareable(Node newsNode, Space space);
}
