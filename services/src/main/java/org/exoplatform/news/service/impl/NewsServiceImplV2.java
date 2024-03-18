/*
 * Copyright (C) 2024 eXo Platform SAS.
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
package org.exoplatform.news.service.impl;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.gatein.api.EntityNotFoundException;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsDraftObject;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.wiki.model.DraftPage;
import org.exoplatform.wiki.model.Page;
import org.exoplatform.wiki.service.NoteService;

public class NewsServiceImplV2 implements NewsService {

  public static final String       NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME = "Articles";

  public static final MetadataType NEWS_METADATA_TYPE                = new MetadataType(1000, "news");

  public static final String       NEWS_METADATA_NAME                = "news";

  public static final String       NEWS_METADATA_DRAFT_OBJECT_TYPE   = "newsDraftPage";

  public static final String       NEWS_FILE_API_NAME_SPACE          = "news";

  public static final String       NEWS_SUMMARY                      = "summary";

  public static final String       NEWS_ILLUSTRATION_ID              = "illustrationId";

  private static final Log         LOG                               = ExoLogger.getLogger(NewsServiceImplV2.class);

  private final SpaceService       spaceService;

  private final NoteService        noteService;

  private final MetadataService    metadataService;

  private final FileService        fileService;

  private final UploadService      uploadService;

  public NewsServiceImplV2(SpaceService spaceService,
                           NoteService noteService,
                           MetadataService metadataService,
                           FileService fileService,
                           UploadService uploadService) {
    this.spaceService = spaceService;
    this.noteService = noteService;
    this.metadataService = metadataService;
    this.fileService = fileService;
    this.uploadService = uploadService;
  }

  @Override
  public News createNews(News news, Identity currentIdentity) throws Exception {
    Space space = spaceService.getSpaceById(news.getSpaceId());
    try {
      if (!canCreateNews(space, currentIdentity)) {
        throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " not authorized to create news");
      }
      News createdNews;
      if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState()) && recreateIfDraftDeleted(news) != null) {
        createdNews = postNews(news, currentIdentity.getUserId());
      } else if (news.getSchedulePostDate() != null) {
        createdNews = unScheduleNews(news, currentIdentity);
      } else {
        createdNews = createDraftArticleForNewPage(news, space.getGroupId());
      }
      return createdNews;
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return null;
    }
  }

  @Override
  public News postNews(News news, String poster) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canCreateNews(Space space, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    return space != null
        && (spaceService.isSuperManager(currentIdentity.getUserId()) || spaceService.isManager(space, currentIdentity.getUserId())
            || spaceService.isRedactor(space, currentIdentity.getUserId())
            || spaceService.isMember(space, currentIdentity.getUserId()) && ArrayUtils.isEmpty(space.getRedactors()));
  }

  @Override
  public News updateNews(News news, String updater, Boolean post, boolean publish) throws Exception {
    return null;
  }

  @Override
  public void deleteNews(String id, Identity currentIdentity, boolean isDraft) throws Exception {

  }

  @Override
  public void publishNews(News news, String publisher) throws Exception {

  }

  @Override
  public void unpublishNews(String newsId, String publisher) throws Exception {

  }

  @Override
  public News getNewsById(String newsId, Identity currentIdentity, boolean editMode) throws IllegalAccessException {
    return null;
  }

  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    return null;
  }

  @Override
  public List<News> getNews(NewsFilter filter, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public List<News> getNewsByTargetName(NewsFilter filter, String targetName, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public int getNewsCount(NewsFilter filter) throws Exception {
    return 0;
  }

  @Override
  public void markAsRead(News news, String userId) throws Exception {

  }

  @Override
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public News getNewsByActivityId(String activityId, Identity currentIdentity) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    return null;
  }

  @Override
  public News scheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  @Override
  public News unScheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  @Override
  public List<NewsESSearchResult> search(org.exoplatform.social.core.identity.model.Identity currentIdentity, NewsFilter filter) {
    return new ArrayList<>();
  }

  @Override
  public boolean canScheduleNews(Space space, Identity currentIdentity) {
    return false;
  }

  @Override
  public boolean canViewNews(News news, String authenticatedUser) {
    return false;
  }

  @Override
  public void shareNews(News news,
                        Space space,
                        org.exoplatform.social.core.identity.model.Identity userIdentity,
                        String sharedActivityId) throws Exception {

  }

  @Override
  public void archiveNews(String newsId, String currentUserName) throws Exception {

  }

  @Override
  public void unarchiveNews(String newsId, String currentUserName) throws Exception {

  }

  @Override
  public boolean canArchiveNews(Identity currentIdentity, String newsAuthor) {
    return false;
  }

  public News createDraftArticleForNewPage(News news, String pageOwnerId) throws Exception {
    Page rootPage = getArticlesRootPage(pageOwnerId);
    if (rootPage != null) {
      DraftPage draftArticle = new DraftPage();
      draftArticle.setNewPage(true);
      draftArticle.setTargetPageId(null);
      draftArticle.setTitle(news.getTitle());
      draftArticle.setContent(news.getBody());
      draftArticle.setParentPageId(rootPage.getId());
      draftArticle.setAuthor(news.getAuthor());
      // created and updated date set by default during the draft creation
      // process
      draftArticle = noteService.createDraftForNewPage(draftArticle, System.currentTimeMillis());
      if (draftArticle != null) {
        // save illustration
        Long illustrationId = -1L;
        if (news.getUploadId() != null && !news.getUploadId().isBlank()) {
          illustrationId = saveNewsIllustration(news.getUploadId(), null);
        }
        Map<String, String> properties = new HashMap<>();
        properties.put(NEWS_SUMMARY, news.getSummary());
        properties.put(NEWS_ILLUSTRATION_ID, illustrationId >= 0 ? String.valueOf(illustrationId) : null);
        NewsDraftObject newsDraftMetaDataObject =
                                                new NewsDraftObject(NEWS_METADATA_DRAFT_OBJECT_TYPE, draftArticle.getId(), null);
        MetadataKey newsDraftMetadataKey = new MetadataKey(NEWS_METADATA_TYPE.getName(), NEWS_METADATA_NAME, 0);
        metadataService.createMetadataItem(newsDraftMetaDataObject, newsDraftMetadataKey, properties);
        news.setId(draftArticle.getId());
        news.setCreationDate(draftArticle.getCreatedDate());
        news.setUpdateDate(draftArticle.getUpdatedDate());
        if (illustrationId != null) {
          FileItem fileItem = fileService.getFile(illustrationId);
          if (fileItem != null) {
            news.setIllustration(fileItem.getAsByte());
          }
        }
        return news;
      }
    }
    throw new EntityNotFoundException("News articles root note parent not found");
  }

  protected Page getArticlesRootPage(String ownerId) {
    List<Page> notes =
                     noteService.getNotesOfWiki("group", ownerId)
                                .stream()
                                .filter(e -> e.getName().equals(NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME) && e.getParentPageId() == null)
                                .toList();
    return notes.isEmpty() ? null : notes.get(0);
  }

  private Long saveNewsIllustration(String uploadId, Long oldFileId) {
    if (uploadId == null || uploadId.isBlank()) {
      throw new IllegalArgumentException("uploadId is mandatory");
    }
    if (oldFileId != null && oldFileId != 0) {
      fileService.deleteFile(oldFileId);
    }
    UploadResource uploadResource = uploadService.getUploadResource(uploadId);
    if (uploadResource == null) {
      throw new IllegalStateException("Can't find uploaded resource with id : " + uploadId);
    }
    try {
      InputStream inputStream = new FileInputStream(uploadResource.getStoreLocation());
      FileItem fileItem = new FileItem(null,
                                       uploadResource.getFileName(),
                                       uploadResource.getMimeType(),
                                       NEWS_FILE_API_NAME_SPACE,
                                       (long) uploadResource.getUploadedSize(),
                                       new Date(),
                                       IdentityConstants.SYSTEM,
                                       false,
                                       inputStream);
      fileItem = fileService.writeFile(fileItem);
      return fileItem != null && fileItem.getFileInfo() != null ? fileItem.getFileInfo().getId() : null;
    } catch (Exception e) {
      throw new IllegalStateException("Error while saving news illustration file", e);
    } finally {
      uploadService.removeUploadResource(uploadResource.getUploadId());
    }
  }

  private News recreateIfDraftDeleted(News news) throws Exception {
    return null;
  }
}
