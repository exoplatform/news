package org.exoplatform.news.service.impl;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.plugin.MentionInNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.PostNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.PublishNewsNotificationPlugin;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.news.search.NewsESSearchConnector;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.*;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.rest.api.RestUtils;
import org.exoplatform.upload.UploadService;

import javax.jcr.ItemNotFoundException;

/**
 * Service managing News and storing them in ECMS
 */
public class NewsServiceImpl implements NewsService {

  private static final String   PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String   PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private static final String   NEWS_ID                         = "newsId";

  public static final MetadataType METADATA_TYPE = new MetadataType(5, "archives");

  public SpaceService          spaceService;

  private ActivityManager       activityManager;

  private IdentityManager       identityManager;

  private NewsESSearchConnector newsESSearchConnector;

  private IndexingService indexingService;

  private MetadataService metadataService;

  private NewsStorage           newsStorage;

  private UserACL               userACL;

  private NewsTargetingService               newsTargetingService;

  private static final Log      LOG                             = ExoLogger.getLogger(NewsServiceImpl.class);

  public NewsServiceImpl(SpaceService spaceService,
                         ActivityManager activityManager,
                         IdentityManager identityManager,
                         UploadService uploadService,
                         NewsESSearchConnector newsESSearchConnector,
                         IndexingService indexingService,
                         NewsStorage newsStorage,
                         UserACL userACL,
                         NewsTargetingService newsTargetingService,
                         MetadataService metadataService) {
    this.spaceService = spaceService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.newsESSearchConnector = newsESSearchConnector;
    this.indexingService = indexingService;
    this.newsStorage = newsStorage;
    this.userACL = userACL;
    this.newsTargetingService = newsTargetingService;
    this.metadataService = metadataService;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News createNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
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
        createdNews = newsStorage.createNews(news);
      }
      return createdNews;
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return null;
    }
  }

  private News recreateIfDraftDeleted(News news) throws Exception {
    News existNews;
    try {
      existNews = newsStorage.getNewsById(news.getId(), false);
    } catch (ItemNotFoundException e) {
      existNews = newsStorage.createNews(news);
    }
    return existNews;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canCreateNews(Space space, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    return space != null
        && (spaceService.isSuperManager(currentIdentity.getUserId()) || spaceService.isManager(space, currentIdentity.getUserId())
            || spaceService.isRedactor(space, currentIdentity.getUserId())
            || spaceService.isMember(space, currentIdentity.getUserId()) && (ArrayUtils.isEmpty(space.getRedactors())
            || currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News updateNews(News news, String updater, Boolean post, boolean publish) throws Exception {

    if (!canEditNews(news, updater)) {
      throw new IllegalArgumentException("User " + updater + " is not authorized to update news");
    }
    Set<String> previousMentions = NewsUtils.processMentions(news.getBody());
    if (publish != news.isPublished() && news.isCanPublish()) {
      news.setPublished(publish);
      if (news.isPublished()) {
        publishNews(news, updater);
      } else {
        unpublishNews(news.getId(), updater);
      }
    }
    List<String> oldTargets = newsTargetingService.getTargetsByNewsId(news.getId());
    if (publish == news.isPublished() && news.isPublished() && news.isCanPublish() && news.getTargets() != null && !oldTargets.equals(news.getTargets())) {
      newsTargetingService.deleteNewsTargets(news.getId(), updater);
      newsTargetingService.saveNewsTarget(news.getId(), StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED), news.getTargets(), updater);
    }

    newsStorage.updateNews(news);

    if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
      // Send mention notifs
      if (StringUtils.isNotEmpty(news.getId()) && news.getCreationDate() != null) {
        News newMentionedNews = news;
        if (!previousMentions.isEmpty()) {
          //clear old mentions from news body before sending a custom object to notification context.
          previousMentions.forEach(username -> {
            newMentionedNews.setBody(newMentionedNews.getBody().replaceAll("@"+username, ""));
          });
        }
        sendNotification(updater, newMentionedNews, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS);
      }
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, String.valueOf(news.getId()));
    }
    if (post != null) {
      updateNewsActivity(news, post);
    }
    NewsUtils.broadcastEvent(NewsUtils.UPDATE_NEWS, updater, news);
    return news;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteNews(String newsId, org.exoplatform.services.security.Identity currentIdentity, boolean isDraft) throws Exception {
    News news = getNewsById(newsId, currentIdentity, false);
    if (!news.isCanDelete()) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " is not authorized to delete news");
    }

    List<String> newsTargets = newsTargetingService.getTargetsByNewsId(newsId);
    if(newsTargets != null) {
      newsTargetingService.deleteNewsTargets(newsId);
    }
    newsStorage.deleteNews(newsId, isDraft);
    indexingService.unindex(NewsIndexingServiceConnector.TYPE, String.valueOf(news.getId()));
    NewsUtils.broadcastEvent(NewsUtils.DELETE_NEWS, currentIdentity.getUserId(), news);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, org.exoplatform.services.security.Identity currentIdentity, boolean editMode) throws IllegalAccessException {
    News news = null;
    try {
      news = getNewsById(newsId, editMode);
    } catch (Exception e) {
      LOG.error("An error occurred while retrieving news with id " + newsId, e);
    }
    if (news != null) {
      if (editMode) {
        if (!canEditNews(news, currentIdentity.getUserId())) {
          throw new IllegalAccessException("User " + currentIdentity.getUserId() + " is not authorized to edit News");
        }
      } else if (!canViewNews(news, currentIdentity.getUserId())) {
        throw new IllegalAccessException("User " + currentIdentity.getUserId() + " is not authorized to view News");
      }
      news.setCanEdit(canEditNews(news, currentIdentity.getUserId()));
      news.setCanDelete(canDeleteNews(currentIdentity, news.getAuthor(), news.getSpaceId()));
      news.setCanPublish(NewsUtils.canPublishNews(currentIdentity));
      news.setCanArchive(canArchiveNews(currentIdentity, news.getAuthor()));
      news.setTargets(newsTargetingService.getTargetsByNewsId(newsId));
      ExoSocialActivity activity = activityManager.getActivity(news.getActivityId());
      if (activity != null) {
        RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity, true);
        news.setCommentsCount(listAccess.getSize());
        news.setLikesCount(activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length);
      }
    }
    return news;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    try {
      News news = newsStorage.getNewsById(newsId, editMode);
      return news;
    } catch (Exception e) {
      throw new Exception("An error occurred while retrieving news with id " + newsId, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> getNews(NewsFilter newsFilter, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    List<News> newsList = newsStorage.getNews(newsFilter);
    newsList.stream().forEach(news -> {
      news.setCanEdit(canEditNews(news, currentIdentity.getUserId()));
      news.setCanDelete(canDeleteNews(currentIdentity, news.getAuthor(), news.getSpaceId()));
      news.setCanPublish(NewsUtils.canPublishNews(currentIdentity));
      news.setCanArchive(canArchiveNews(currentIdentity, news.getAuthor()));
    });
    return newsList;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> getNewsByTargetName(NewsFilter newsFilter, String targetName, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    List<MetadataItem> newsTargetItems = newsTargetingService.getNewsTargetItemsByTargetName(targetName,newsFilter.isArchivedNews(), newsFilter.getOffset(), newsFilter.getLimit());
    return newsTargetItems.stream().map(target -> {
      try {
        News news = getNewsById(target.getObjectId(), currentIdentity, false);
        news.setPublishDate(new Date(target.getCreatedDate()));
        return news;
      } catch (Exception e) {
        return null;
      }
    }).collect(Collectors.toList());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getNewsCount(NewsFilter newsFilter) throws Exception {
    return newsStorage.getNewsCount(newsFilter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return newsStorage.searchNews(filter, lang);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsByActivityId(String activityId, org.exoplatform.services.security.Identity currentIdentity) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " wasn't found");
    }
    org.exoplatform.services.security.Identity viewerIdentity = NewsUtils.getUserIdentity(currentIdentity.getUserId());
    if (!activityManager.isActivityViewable(activity, viewerIdentity)) {
      throw new IllegalAccessException("User " + currentIdentity.getUserId() + " isn't allowed to access activity with id " + activityId);
    }
    Map<String, String> templateParams = activity.getTemplateParams();
    if (templateParams == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " isn't of type news nor a shared news");
    }
    String newsId = templateParams.get(NEWS_ID);
    if (StringUtils.isBlank(newsId)) {
      String originalActivityId = templateParams.get("originalActivityId");
      if (StringUtils.isNotBlank(originalActivityId)) {
        Identity sharedActivityPosterIdentity = identityManager.getIdentity(activity.getPosterId());
        if (sharedActivityPosterIdentity == null) {
          throw new IllegalAccessException("Shared Activity '" + activityId + "' Poster " + activity.getPosterId()
              + " isn't found");
        }
        return getNewsByActivityId(originalActivityId, NewsUtils.getUserIdentity(sharedActivityPosterIdentity.getRemoteId()));
      }
      throw new ObjectNotFoundException("Activity with id " + activityId + " isn't of type news nor a shared news");
    }
    return getNewsById(newsId, currentIdentity, false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News scheduleNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    Space space = news.getSpaceId() == null ? null : spaceService.getSpaceById(news.getSpaceId());
    if (!canScheduleNews(space, currentIdentity)) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " is not authorized to schedule news");
    }
    if (NewsUtils.canPublishNews(currentIdentity) && news.isPublished() && news.getTargets() != null) {
      newsTargetingService.deleteNewsTargets(news.getId());
      newsTargetingService.saveNewsTarget(news.getId(), StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED), news.getTargets(), currentIdentity.getUserId());
    }
    return newsStorage.scheduleNews(news);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News unScheduleNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    Space space = news.getSpaceId() == null ? null : spaceService.getSpaceById(news.getSpaceId());
    if (!canScheduleNews(space, currentIdentity)) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " is not authorized to unschedule news");
    }
    newsTargetingService.deleteNewsTargets(news.getId());
    return newsStorage.unScheduleNews(news);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<NewsESSearchResult> search(Identity currentIdentity, NewsFilter filter) {
    return newsESSearchConnector.search(currentIdentity, filter);
  }

  /**
   * {@inheritDoc}
   */
  public News postNews(News news, String poster) throws Exception {
    postNewsActivity(news);
    updateNews(news, poster, null, news.isPublished());
    sendNotification(poster, news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);
    if (news.isPublished()) {
      publishNews(news, poster);
    }
    NewsUtils.broadcastEvent(NewsUtils.POST_NEWS_ARTICLE, news.getId(), news);//Gamification
    NewsUtils.broadcastEvent(NewsUtils.POST_NEWS, news.getAuthor(), news);//Analytics
    return news;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void markAsRead(News news, String userId) throws Exception {
    if (!newsStorage.isCurrentUserInNewsViewers(news.getId(), userId)) {
      newsStorage.markAsRead(news, userId);
    }
    NewsUtils.broadcastEvent(NewsUtils.VIEW_NEWS, userId, news);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishNews(News publishedNews, String publisher) throws Exception {
    News news = getNewsById(publishedNews.getId(), false);
    newsStorage.publishNews(news);
    if(publishedNews.getTargets() != null) {
      newsTargetingService.deleteNewsTargets(publishedNews.getId(), publisher);
      newsTargetingService.saveNewsTarget(publishedNews.getId(), StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED), publishedNews.getTargets(), publisher);
    }
    NewsUtils.broadcastEvent(NewsUtils.PUBLISH_NEWS, news.getId(), news);
    sendNotification(publisher, news, NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unpublishNews(String newsId, String publisher) throws Exception {
    newsStorage.unpublishNews(newsId);
    newsTargetingService.deleteNewsTargets(newsId, publisher);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws Exception {
    if (!canViewNews(news, userIdentity.getRemoteId())) {
      throw new IllegalAccessException("User with id " + userIdentity.getRemoteId() + "doesn't have access to news");
    }
    newsStorage.shareNews(news, space, userIdentity, sharedActivityId);
    if (sharedActivityId != null) {
      NewsUtils.broadcastEvent(NewsUtils.SHARE_NEWS, userIdentity.getRemoteId(), news);
    }
  }

  /**
   * Post the news activity in the given space
   *
   * @param news The news to post as an activity
   * @throws Exception when error
   */
  public void postNewsActivity(News news) throws Exception {
    Identity poster = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor());

    Space space = spaceService.getSpaceById(news.getSpaceId());
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("");
    activity.setBody(news.getBody());
    activity.setType("news");
    activity.setUserId(poster.getId());
    activity.isHidden(news.isActivityPosted());
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put(NEWS_ID, news.getId());
    activity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    newsStorage.updateNewsActivities(activity.getId(), news);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void archiveNews(String newsId) throws Exception {
    newsStorage.archiveNews(newsId);
    this.createMetaDataArchive(newsId ,RestUtils.getCurrentUserIdentityId());
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canViewNews(News news, String username) {
    try {
      String spaceId = news.getSpaceId();
      Space space = spaceId == null ? null : spaceService.getSpaceById(spaceId);
      if (space == null) {
        LOG.warn("Can't find space with id {} when checking access on news with id {}", spaceId, news.getId());
        return false;
      }
      if (!news.isPublished()
          && StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.PUBLISHED)
          && !(spaceService.isSuperManager(username)
              || spaceService.isMember(space, username)
              || isMemberOfsharedInSpaces(news, username))) {
        return false;
      }
      if (StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED)
          && !(StringUtils.equals(news.getAuthor(), username)
              || spaceService.isManager(space, username)
              || spaceService.isRedactor(space, username))) {
        return false;
      }
    } catch (Exception e) {
      LOG.warn("Error retrieving access permission for user {} on news with id {}", username, news.getId());
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unarchiveNews(String newsId) throws Exception {
    newsStorage.unarchiveNews(newsId);
    this.deleteMetaDataArchive(newsId,RestUtils.getCurrentUserIdentityId());
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canScheduleNews(Space space, org.exoplatform.services.security.Identity currentIdentity) {
    return spaceService.isManager(space, currentIdentity.getUserId()) || spaceService.isRedactor(space, currentIdentity.getUserId()) || currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canArchiveNews(org.exoplatform.services.security.Identity currentIdentity, String newsAuthor) {
    return currentIdentity != null && (currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
        || currentIdentity.getUserId().equals(newsAuthor));
  }

  public void sendNotification(String currentUserId, News news, NotificationConstants.NOTIFICATION_CONTEXT context) throws Exception {
    String newsId = news.getId();
    String contentAuthor = news.getAuthor();
    String currentUser = currentUserId != null ? currentUserId : contentAuthor;
    String activities = news.getActivities();
    String contentTitle = news.getTitle();
    String contentBody = news.getBody();
    String lastSpaceIdActivityId = activities.split(";")[activities.split(";").length - 1];
    String contentSpaceId = lastSpaceIdActivityId.split(":")[0];
    String contentActivityId = lastSpaceIdActivityId.split(":")[1];
    Space contentSpace = spaceService.getSpaceById(contentSpaceId);
    boolean isMember = spaceService.isMember(contentSpace, contentAuthor);
    if (contentSpace == null) {
      throw new NullPointerException("Cannot find a space with id " + contentSpaceId + ", it may not exist");
    }
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, contentAuthor);
    String authorAvatarUrl = LinkProviderUtils.getUserAvatarUrl(identity.getProfile());
    String illustrationURL = newsStorage.getNewsIllustration(news);
    String activityLink = NotificationUtils.getNotificationActivityLink(contentSpace, contentActivityId, isMember);
    String contentSpaceName = contentSpace.getDisplayName();

    // Send Notification
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTEXT, context)
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, contentTitle)
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, contentAuthor)
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, currentUser)
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, contentSpaceId)
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, contentSpaceName)
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL, illustrationURL)
                                                     .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL, authorAvatarUrl)
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK, activityLink)
                                                     .append(PostNewsNotificationPlugin.NEWS_ID, newsId);

    if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS)) {
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(PostNewsNotificationPlugin.ID))).execute(ctx);
      Matcher matcher = MentionInNewsNotificationPlugin.MENTION_PATTERN.matcher(contentBody);
      if(matcher.find()) {
        sendMentionInNewsNotification(contentAuthor, currentUser, contentTitle, contentBody, contentSpaceId, illustrationURL, authorAvatarUrl, activityLink, contentSpaceName);
      }
    } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS)) {
      sendMentionInNewsNotification(contentAuthor, currentUser, contentTitle, contentBody, contentSpaceId, illustrationURL, authorAvatarUrl, activityLink, contentSpaceName);
    } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS)) {
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(PublishNewsNotificationPlugin.ID))).execute(ctx);
    }
  }

  private void sendMentionInNewsNotification(String contentAuthor, String currentUser, String contentTitle, String contentBody, String contentSpaceId, String illustrationURL, String authorAvatarUrl, String activityLink, String contentSpaceName) {
    Set<String> mentionedIds = NewsUtils.processMentions(contentBody);
    NotificationContext mentionNotificationCtx = NotificationContextImpl.cloneInstance()
            .append(MentionInNewsNotificationPlugin.CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS)
            .append(PostNewsNotificationPlugin.CURRENT_USER, currentUser)
            .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, contentAuthor)
            .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, contentSpaceId)
            .append(PostNewsNotificationPlugin.CONTENT_TITLE, contentTitle)
            .append(PostNewsNotificationPlugin.CONTENT_SPACE, contentSpaceName)
            .append(PostNewsNotificationPlugin.ILLUSTRATION_URL, illustrationURL)
            .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL, authorAvatarUrl)
            .append(PostNewsNotificationPlugin.ACTIVITY_LINK, activityLink)
            .append(MentionInNewsNotificationPlugin.MENTIONED_IDS, mentionedIds);
    mentionNotificationCtx.getNotificationExecutor().with(mentionNotificationCtx.makeCommand(PluginKey.key(MentionInNewsNotificationPlugin.ID))).execute(mentionNotificationCtx);
  }

  private void updateNewsActivity(News news, boolean post) {
    ExoSocialActivity activity = activityManager.getActivity(news.getActivityId());
    if(activity != null) {
      if (post) {
        activity.setUpdated(System.currentTimeMillis());
      }
      activity.setBody(news.getBody());
      activity.isHidden(news.isActivityPosted());
      activityManager.updateActivity(activity, true);
    }
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
    String posterUsername = news.getAuthor();
    if (authenticatedUser.equals(posterUsername)) {
      return true;
    }
    Space currentSpace = spaceService.getSpaceById(spaceId);
    // Posted news draft
    if ((spaceService.isManager(currentSpace, authenticatedUser) || spaceService.isSuperManager(authenticatedUser)
        || authenticatedUserIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME))
        && news.getActivities() != null) {
      return true;
    }
    // No Posted yet news draft
    if ((spaceService.isRedactor(currentSpace, authenticatedUser) || spaceService.isManager(currentSpace, authenticatedUser))
        && news.isDraftVisible() && news.getActivities() == null) {
      return true;
    }
    return false;
  }

  private boolean canDeleteNews(org.exoplatform.services.security.Identity currentIdentity, String posterId, String spaceId) {
    if (currentIdentity == null) {
      return false;
    }
    String authenticatedUser = currentIdentity.getUserId();
    Space currentSpace = spaceService.getSpaceById(spaceId);
    return authenticatedUser.equals(posterId) || userACL.isSuperUser() || spaceService.isSuperManager(authenticatedUser)
        || spaceService.isManager(currentSpace, authenticatedUser);
  }

  private boolean isMemberOfsharedInSpaces(News news, String username) {
    for (String sharedInSpaceId : news.getSharedInSpacesList()) {
      Space sharedInSpace = spaceService.getSpaceById(sharedInSpaceId);
      if(sharedInSpace != null && spaceService.isMember(sharedInSpace, username)) {
        return true;
      }
    }
    return false;
  }

  private void createMetaDataArchive(String newId , long userIdentityId) throws ObjectAlreadyExistsException {
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),"0",0);
    MetadataObject metadataObject = new MetadataObject(METADATA_TYPE.getName(),newId);

    metadataService.createMetadataItem(metadataObject,
                                       metadataKey,
                                       userIdentityId);
  }

  public void deleteMetaDataArchive(String newId ,long userIdentityId) throws ObjectNotFoundException {
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(),"0",0);
    MetadataObject metadataObject = new MetadataObject(METADATA_TYPE.getName(),newId);

    List<MetadataItem> archives = metadataService.getMetadataItemsByMetadataAndObject(metadataKey, metadataObject);
    if (CollectionUtils.isEmpty(archives)) {
      throw new ObjectNotFoundException("archive entity not found for archives " + archives);
    }
    for (MetadataItem archive : archives) {
      metadataService.deleteMetadataItem(archive.getId(), userIdentityId);
    }
  }
  @Override
  public boolean isArchived(long userIdentityId,String newId){
    MetadataKey metadataKey = new MetadataKey(METADATA_TYPE.getName(), String.valueOf(userIdentityId), userIdentityId);
    MetadataObject metadataObject = new MetadataObject(METADATA_TYPE.getName(),newId);
    List<MetadataItem> archives = metadataService.getMetadataItemsByMetadataAndObject(metadataKey,metadataObject);

    return !archives.isEmpty();
  }

}
