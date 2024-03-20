package org.exoplatform.news.storage;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.social.core.space.model.Space;

public interface NewsAttachmentsStorage {

  List<NewsAttachment> getNewsAttachments(Node newsNode) throws Exception;

  void makeAttachmentsPublic(Node newsNode) throws Exception;

  void unmakeAttachmentsPublic(Node newsNode) throws Exception;

  void shareAttachments(Node newsNode, Space space);
}
