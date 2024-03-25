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

import static org.exoplatform.news.utils.NewsUtils.NewsObjectType.ARTICLE;
import static org.exoplatform.news.utils.NewsUtils.NewsObjectType.DRAFT;
import static org.exoplatform.news.utils.NewsUtils.NewsObjectType.LATEST_DRAFT;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsDraftObject;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;
import org.exoplatform.social.metadata.model.MetadataType;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.wiki.WikiException;
import org.exoplatform.wiki.model.DraftPage;
import org.exoplatform.wiki.model.Page;
import org.exoplatform.wiki.service.NoteService;

public class NewsServiceImplV2 implements NewsService {

  public static final String         NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME = "Articles";

  public static final MetadataType   NEWS_METADATA_TYPE                = new MetadataType(1000, "news");

  public static final String         NEWS_METADATA_NAME                = "news";

  public static final String         NEWS_METADATA_DRAFT_OBJECT_TYPE   = "newsDraftPage";

  public static final String         NEWS_FILE_API_NAME_SPACE          = "news";

  public static final String         NEWS_SUMMARY                      = "summary";

  public static final String         NEWS_ILLUSTRATION_ID              = "illustrationId";

  public static final String         NEWS_UPLOAD_ID                    = "uploadId";

  private static final Log           LOG                               = ExoLogger.getLogger(NewsServiceImplV2.class);

  private final SpaceService         spaceService;

  private final NoteService          noteService;

  private final MetadataService      metadataService;

  private final FileService          fileService;

  private final UploadService        uploadService;

  private final NewsTargetingService newsTargetingService;

  private final IndexingService      indexingService;

  private final IdentityManager      identityManager;

  public NewsServiceImplV2(SpaceService spaceService,
                           NoteService noteService,
                           MetadataService metadataService,
                           FileService fileService,
                           NewsTargetingService newsTargetingService,
                           IndexingService indexingService,
                           IdentityManager identityManager,
                           UploadService uploadService) {
    this.spaceService = spaceService;
    this.noteService = noteService;
    this.metadataService = metadataService;
    this.fileService = fileService;
    this.uploadService = uploadService;
    this.newsTargetingService = newsTargetingService;
    this.indexingService = indexingService;
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
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
        createdNews = createDraftArticleForNewPage(news, space.getGroupId(), currentIdentity.getUserId());
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

  /**
   * {@inheritDoc}
   */
  @Override
  public News updateNews(News news, String updater, Boolean post, boolean publish) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News updateNews(News news, String updater, Boolean post, boolean publish, String newsObjectType) throws Exception {

    if (!canEditNews(news, updater)) {
      throw new IllegalArgumentException("User " + updater + " is not authorized to update news");
    }
    org.exoplatform.services.security.Identity updaterIdentity = NewsUtils.getUserIdentity(updater);
    News originalNews = getNewsById(news.getId(), updaterIdentity, false, newsObjectType);
    List<String> oldTargets = newsTargetingService.getTargetsByNewsId(news.getId());
    boolean canPublish = NewsUtils.canPublishNews(news.getSpaceId(), updaterIdentity);
    Set<String> previousMentions = NewsUtils.processMentions(originalNews.getOriginalBody(),
                                                             spaceService.getSpaceById(news.getSpaceId()));
    if (publish != news.isPublished() && news.isCanPublish()) {
      news.setPublished(publish);
      if (news.isPublished()) {
        publishNews(news, updater);
      } else {
        unpublishNews(news.getId(), updater);
      }
    }
    boolean displayed = !(StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED) || news.isArchived());
    if (publish == news.isPublished() && news.isPublished() && canPublish) {
      if (news.getTargets() != null && (oldTargets == null || !oldTargets.equals(news.getTargets()))) {
        newsTargetingService.deleteNewsTargets(news, updater);
        newsTargetingService.saveNewsTarget(news, displayed, news.getTargets(), updater);
      }
      if (news.getAudience() != null && news.getAudience().equals(NewsUtils.ALL_NEWS_AUDIENCE)
          && originalNews.getAudience() != null && originalNews.getAudience().equals(NewsUtils.SPACE_NEWS_AUDIENCE)) {
        sendNotification(updater, news, NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_NEWS);
      }
    }

    if (DRAFT.name().toLowerCase().equals(newsObjectType)) {
      news = updateDraftArticleForNewPage(news, updater);
    } else if (LATEST_DRAFT.name().toLowerCase().equals(newsObjectType)) {
      // TODO
    } else if (ARTICLE.name().toLowerCase().equals(newsObjectType)) {
      // TODO
    }

    if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
      // Send mention notifs
      if (StringUtils.isNotEmpty(news.getId()) && news.getCreationDate() != null) {
        News newMentionedNews = news;
        if (!previousMentions.isEmpty()) {
          // clear old mentions from news body before sending a custom object to
          // notification context.
          previousMentions.forEach(username -> newMentionedNews.setBody(newMentionedNews.getBody()
                                                                                        .replaceAll("@" + username, "")));
        }
        sendNotification(updater, newMentionedNews, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS);
      }
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, String.valueOf(news.getId()));
    }
    if (!news.getPublicationState().isEmpty() && !PublicationDefaultStates.DRAFT.equals(news.getPublicationState())) {
      if (post != null) {
        updateNewsActivity(news, post);
      }
      NewsUtils.broadcastEvent(NewsUtils.UPDATE_NEWS, updater, news);
    }
    return news;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteNews(String id, Identity currentIdentity, boolean isDraft) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishNews(News news, String publisher) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unpublishNews(String newsId, String publisher) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, Identity currentIdentity, boolean editMode) throws IllegalAccessException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId,
                          Identity currentIdentity,
                          boolean editMode,
                          String newsObjectType) throws IllegalAccessException {
    News news = null;
    try {
      if (newsObjectType == null) {
        throw new IllegalArgumentException("required argument news type could not be null");
      }
      if (DRAFT.name().toLowerCase().equals(newsObjectType)) {
        news = buildDraftArticle(newsId, currentIdentity.getUserId());
      } else if (LATEST_DRAFT.name().toLowerCase().equals(newsObjectType)) {
        // TODO
      } else if (ARTICLE.name().toLowerCase().equals(newsObjectType)) {
        // TODO
      }
      return news;
    } catch (Exception exception) {
      LOG.error("error when fetching news type " + newsObjectType, exception);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> getNews(NewsFilter filter, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> getNewsByTargetName(NewsFilter filter, String targetName, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNewsCount(NewsFilter filter) throws Exception {
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void markAsRead(News news, String userId) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsByActivityId(String activityId, Identity currentIdentity) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News scheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News unScheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<NewsESSearchResult> search(org.exoplatform.social.core.identity.model.Identity currentIdentity, NewsFilter filter) {
    return new ArrayList<>();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canScheduleNews(Space space, Identity currentIdentity) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canViewNews(News news, String authenticatedUser) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shareNews(News news,
                        Space space,
                        org.exoplatform.social.core.identity.model.Identity userIdentity,
                        String sharedActivityId) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void archiveNews(String newsId, String currentUserName) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unarchiveNews(String newsId, String currentUserName) throws Exception {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canArchiveNews(Identity currentIdentity, String newsAuthor) {
    return false;
  }

  private News createDraftArticleForNewPage(News draftArticle, String pageOwnerId, String draftArticleCreator) throws Exception {
    Page articlesRootPage = getArticlesRootPage(pageOwnerId);
    if (articlesRootPage != null) {
      DraftPage draftArticlePage = new DraftPage();
      draftArticlePage.setNewPage(true);
      draftArticlePage.setTargetPageId(null);
      draftArticlePage.setTitle(draftArticle.getTitle());
      draftArticlePage.setContent(draftArticle.getBody());
      draftArticlePage.setParentPageId(articlesRootPage.getId());
      draftArticlePage.setAuthor(draftArticle.getAuthor());
      draftArticlePage = noteService.createDraftForNewPage(draftArticlePage, System.currentTimeMillis());

      draftArticle.setId(draftArticlePage.getId());
      draftArticle.setCreationDate(draftArticlePage.getCreatedDate());
      draftArticle.setUpdateDate(draftArticlePage.getUpdatedDate());

      NewsDraftObject draftArticleMetaDataObject = new NewsDraftObject(NEWS_METADATA_DRAFT_OBJECT_TYPE,
                                                                       draftArticlePage.getId(),
                                                                       null);
      MetadataKey draftArticleMetadataKey = new MetadataKey(NEWS_METADATA_TYPE.getName(), NEWS_METADATA_NAME, 0);
      String draftArticleMetadataItemCreatorIdentityId = identityManager.getOrCreateUserIdentity(draftArticleCreator).getId();
      Map<String, String> draftArticleMetadataItemProperties = new HashMap<>();
      // save illustration
      if (StringUtils.isNotEmpty(draftArticle.getUploadId())) {
        Long draftArticleIllustrationId = saveArticleIllustration(draftArticle.getUploadId(), null);
        setArticleIllustration(draftArticle, draftArticleIllustrationId, DRAFT.name());
        draftArticleMetadataItemProperties.put(NEWS_ILLUSTRATION_ID, String.valueOf(draftArticleIllustrationId));
        draftArticleMetadataItemProperties.put(NEWS_UPLOAD_ID, draftArticle.getUploadId());
      }
      if (StringUtils.isNotEmpty(draftArticle.getSummary())) {
        draftArticleMetadataItemProperties.put(NEWS_SUMMARY, draftArticle.getSummary());
      }
      metadataService.createMetadataItem(draftArticleMetaDataObject,
                                         draftArticleMetadataKey,
                                         draftArticleMetadataItemProperties,
                                         Long.parseLong(draftArticleMetadataItemCreatorIdentityId));

      return draftArticle;
    }
    return null;
  }

  private Page getArticlesRootPage(String ownerId) {
    List<Page> notes =
                     noteService.getNotesOfWiki("group", ownerId)
                                .stream()
                                .filter(e -> e.getName().equals(NEWS_ARTICLES_ROOT_NOTE_PAGE_NAME) && e.getParentPageId() == null)
                                .toList();
    return notes.isEmpty() ? null : notes.get(0);
  }

  private News recreateIfDraftDeleted(News news) throws Exception {
    return null;
  }

  private News updateDraftArticleForNewPage(News draftArticle, String draftArticleUpdater) throws WikiException,
                                                                                           IllegalAccessException,
                                                                                           FileStorageException {
    DraftPage draftArticlePage = noteService.getDraftNoteById(draftArticle.getId(), draftArticleUpdater);
    if (draftArticlePage != null) {
      draftArticlePage.setTitle(draftArticle.getTitle());
      draftArticlePage.setContent(draftArticle.getBody());
      // created and updated date set by default during the draft creation
      // process
      draftArticlePage = noteService.updateDraftForNewPage(draftArticlePage, System.currentTimeMillis());
      NewsDraftObject draftArticleMetaDataObject = new NewsDraftObject(NEWS_METADATA_DRAFT_OBJECT_TYPE,
                                                                       draftArticlePage.getId(),
                                                                       null);
      MetadataKey draftArticleMetadataKey = new MetadataKey(NEWS_METADATA_TYPE.getName(), NEWS_METADATA_NAME, 0);
      List<MetadataItem> draftArticleMetadataItems =
                                                   metadataService.getMetadataItemsByMetadataAndObject(draftArticleMetadataKey,
                                                                                                       draftArticleMetaDataObject);
      if (draftArticleMetadataItems != null && !draftArticleMetadataItems.isEmpty()) {
        MetadataItem draftArticleMetadataItem = draftArticleMetadataItems.get(0);
        Map<String, String> draftArticleMetadataItemProperties = draftArticleMetadataItem.getProperties();
        if (draftArticleMetadataItemProperties == null) {
          draftArticleMetadataItemProperties = new HashMap<>();
        }
        // create or update the illustration
        if (StringUtils.isNotEmpty(draftArticle.getUploadId())) {
          if (draftArticleMetadataItemProperties.containsKey(NEWS_UPLOAD_ID)
              && draftArticleMetadataItemProperties.get(NEWS_UPLOAD_ID) != null
              && draftArticleMetadataItemProperties.containsKey(NEWS_ILLUSTRATION_ID)
              && draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID) != null) {
            if (!draftArticleMetadataItemProperties.get(NEWS_UPLOAD_ID).equals(draftArticle.getUploadId())) {
              FileItem draftArticleIllustrationFileItem =
                                                        fileService.getFile(Long.parseLong(draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID)));
              Long draftArticleIllustrationId = saveArticleIllustration(draftArticle.getUploadId(),
                                                                        draftArticleIllustrationFileItem.getFileInfo().getId());
              draftArticleMetadataItemProperties.put(NEWS_ILLUSTRATION_ID, String.valueOf(draftArticleIllustrationId));
              setArticleIllustration(draftArticle, draftArticleIllustrationId, DRAFT.name());
            }
          } else {
            Long draftArticleIllustrationId = saveArticleIllustration(draftArticle.getUploadId(), null);
            draftArticleMetadataItemProperties.put(NEWS_ILLUSTRATION_ID, String.valueOf(draftArticleIllustrationId));
            setArticleIllustration(draftArticle, draftArticleIllustrationId, DRAFT.name());
          }
          draftArticleMetadataItemProperties.put(NEWS_UPLOAD_ID, draftArticle.getUploadId());
        } else {
          if (draftArticleMetadataItemProperties.containsKey(NEWS_UPLOAD_ID)
              && draftArticleMetadataItemProperties.get(NEWS_UPLOAD_ID) != null
              && draftArticleMetadataItemProperties.containsKey(NEWS_ILLUSTRATION_ID)
              && draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID) != null) {
            draftArticleMetadataItemProperties.remove(NEWS_UPLOAD_ID);
            FileItem draftArticleIllustrationFileItem =
                                                      fileService.getFile(Long.parseLong(draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID)));
            draftArticleMetadataItemProperties.remove(NEWS_ILLUSTRATION_ID);

            fileService.deleteFile(draftArticleIllustrationFileItem.getFileInfo().getId());
          }
        }
        if (StringUtils.isNotEmpty(draftArticle.getSummary())) {
          draftArticleMetadataItemProperties.put(NEWS_SUMMARY, draftArticle.getSummary());
        }
        draftArticleMetadataItem.setProperties(draftArticleMetadataItemProperties);
        String draftArticleMetadataItemUpdaterIdentityId = identityManager.getOrCreateUserIdentity(draftArticleUpdater).getId();
        metadataService.updateMetadataItem(draftArticleMetadataItem, Long.parseLong(draftArticleMetadataItemUpdaterIdentityId));
      }
      return draftArticle;
    }
    return null;
  }

  private News buildDraftArticle(String draftArticleId, String draftArticleCreator) throws WikiException, IllegalAccessException {
    DraftPage draftArticlePage = noteService.getDraftNoteById(draftArticleId, draftArticleCreator);
    if (draftArticlePage != null) {
      News draftArticle = new News();
      draftArticle.setId(draftArticlePage.getId());
      draftArticle.setTitle(draftArticlePage.getTitle());
      draftArticle.setAuthor(draftArticlePage.getAuthor());
      draftArticle.setCreationDate(draftArticlePage.getCreatedDate());
      draftArticle.setUpdateDate(draftArticlePage.getUpdatedDate());
      draftArticle.setBody(draftArticlePage.getContent());
      draftArticle.setPublicationState(PublicationDefaultStates.DRAFT);
      NewsDraftObject draftArticleMetaDataObject =
                                                 new NewsDraftObject(NEWS_METADATA_DRAFT_OBJECT_TYPE, draftArticle.getId(), null);
      MetadataKey draftArticleMetadataKey = new MetadataKey(NEWS_METADATA_TYPE.getName(), NEWS_METADATA_NAME, 0);
      List<MetadataItem> draftArticleMetadataItems =
                                                   metadataService.getMetadataItemsByMetadataAndObject(draftArticleMetadataKey,
                                                                                                       draftArticleMetaDataObject);
      if (draftArticleMetadataItems != null && !draftArticleMetadataItems.isEmpty()) {
        Map<String, String> draftArticleMetadataItemProperties = draftArticleMetadataItems.get(0).getProperties();
        if (draftArticleMetadataItemProperties != null && !draftArticleMetadataItemProperties.isEmpty()) {
          if (draftArticleMetadataItemProperties.containsKey(NEWS_SUMMARY)) {
            draftArticle.setSummary(draftArticleMetadataItemProperties.get(NEWS_SUMMARY));
          }
          if (draftArticleMetadataItemProperties.containsKey(NEWS_ILLUSTRATION_ID)
              && draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID) != null) {
            setArticleIllustration(draftArticle,
                                   Long.valueOf(draftArticleMetadataItemProperties.get(NEWS_ILLUSTRATION_ID)),
                                   DRAFT.name().toLowerCase());
          }
        }
        if (draftArticlePage.getWikiOwner() != null) {
          Space draftArticleSpace = spaceService.getSpaceByGroupId(draftArticlePage.getWikiOwner());
          if (draftArticleSpace != null) {
            draftArticle.setSpaceId(draftArticleSpace.getId());
            draftArticle.setSpaceAvatarUrl(draftArticleSpace.getAvatarUrl());
            draftArticle.setSpaceDisplayName(draftArticleSpace.getDisplayName());
            boolean hiddenSpace = draftArticleSpace.getVisibility().equals(Space.HIDDEN)
                && !spaceService.isMember(draftArticleSpace, draftArticleCreator)
                && !spaceService.isSuperManager(draftArticleCreator);
            draftArticle.setHiddenSpace(hiddenSpace);
            boolean isSpaceMember = spaceService.isSuperManager(draftArticleCreator)
                || spaceService.isMember(draftArticleSpace, draftArticleCreator);
            draftArticle.setSpaceMember(isSpaceMember);
            if (StringUtils.isNotEmpty(draftArticleSpace.getGroupId())) {
              String spaceGroupId = draftArticleSpace.getGroupId().split("/")[2];
              String spaceUrl = "/portal/g/:spaces:" + spaceGroupId + "/" + draftArticleSpace.getPrettyName();
              draftArticle.setSpaceUrl(spaceUrl);
            }
          }
        }
        StringBuilder draftArticleUrl = new StringBuilder("");
        draftArticleUrl.append("/")
                       .append(PortalContainer.getCurrentPortalContainerName())
                       .append("/")
                       .append(CommonsUtils.getCurrentPortalOwner())
                       .append("/news/detail?newsId=")
                       .append(draftArticle.getId())
                       .append("&type=draft");
        draftArticle.setUrl(draftArticleUrl.toString());
      }
      return draftArticle;
    }
    return null;
  }

  private boolean canEditNews(News news, String authenticatedUser) {
    String spaceId = news.getSpaceId();
    Space space = spaceId == null ? null : spaceService.getSpaceById(spaceId);
    if (space == null) {
      return false;
    }
    org.exoplatform.services.security.Identity authenticatedUserIdentity = NewsUtils.getUserIdentity(authenticatedUser);
    if (authenticatedUserIdentity == null) {
      LOG.warn("Can't find user with id {} when checking access on news with id {}", authenticatedUser, news.getId());
      return false;
    }
    if (NewsUtils.canPublishNews(news.getSpaceId(), authenticatedUserIdentity) && news.getActivities() != null) {
      return true;
    }
    return spaceService.canRedactOnSpace(space, authenticatedUserIdentity);
  }

  private void setArticleIllustration(News article, Long articleIllustrationId, String newsObjectType) {
    try {
      FileItem articleIllustrationFileItem = fileService.getFile(articleIllustrationId);
      if (articleIllustrationFileItem != null) {
        article.setIllustration(articleIllustrationFileItem.getAsByte());
        article.setIllustrationMimeType(articleIllustrationFileItem.getFileInfo().getMimetype());
        article.setIllustrationUpdateDate(articleIllustrationFileItem.getFileInfo().getUpdatedDate());
        long lastModified = article.getIllustrationUpdateDate().getTime();
        article.setIllustrationURL("/portal/rest/v1/news/" + article.getId() + "/illustration?v=" + lastModified + "&type="
            + newsObjectType);
      }
    } catch (Exception exception) {
      LOG.info("Failed to set article illustration");
    }
  }

  private Long saveArticleIllustration(String articleUploadId, Long oldArticleIllustrationFileId) {
    if (StringUtils.isEmpty(articleUploadId)) {
      throw new IllegalArgumentException("Article uploadId is mandatory");
    }
    if (oldArticleIllustrationFileId != null && oldArticleIllustrationFileId != 0) {
      fileService.deleteFile(oldArticleIllustrationFileId);
    }
    UploadResource articleUploadResource = uploadService.getUploadResource(articleUploadId);
    if (articleUploadResource == null) {
      throw new IllegalStateException("Can't find article uploaded resource with id : " + articleUploadId);
    }
    try {
      InputStream articleIllustrationFileInputStream = new FileInputStream(articleUploadResource.getStoreLocation());
      FileItem articleIllustrationFileItem = new FileItem(null,
                                                          articleUploadResource.getFileName(),
                                                          articleUploadResource.getMimeType(),
                                                          NEWS_FILE_API_NAME_SPACE,
                                                          (long) articleUploadResource.getUploadedSize(),
                                                          new Date(),
                                                          IdentityConstants.SYSTEM,
                                                          false,
                                                          articleIllustrationFileInputStream);
      articleIllustrationFileItem = fileService.writeFile(articleIllustrationFileItem);
      return articleIllustrationFileItem != null
          && articleIllustrationFileItem.getFileInfo() != null ? articleIllustrationFileItem.getFileInfo().getId() : null;
    } catch (Exception e) {
      throw new IllegalStateException("Error while saving article illustration file", e);
    } finally {
      uploadService.removeUploadResource(articleUploadResource.getUploadId());
    }
  }

  private void sendNotification(String currentUserId,
                                News news,
                                NotificationConstants.NOTIFICATION_CONTEXT context) throws Exception {
    return;
  }

  private void updateNewsActivity(News news, boolean post) {
    // TODO
  }
}
