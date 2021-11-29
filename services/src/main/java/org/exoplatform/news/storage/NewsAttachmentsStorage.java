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
package org.exoplatform.news.storage;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;

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
}
