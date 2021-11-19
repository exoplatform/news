package org.exoplatform.news.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

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
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.upload.UploadService;

/**
 * Service managing News and storing them in ECMS
 */
public class NewsServiceImpl implements NewsService {

  private static final String      PUBLISHER_MEMBERSHIP_NAME        = "publisher";

  private final static String      PLATFORM_WEB_CONTRIBUTORS_GROUP  = "/platform/web-contributors";

  private SpaceService             spaceService;

  private ActivityManager          activityManager;

  private IdentityManager          identityManager;

  private NewsESSearchConnector    newsESSearchConnector;
  
  private IndexingService          indexingService;

  private NewsStorage              newsStorage;
  
  private UserACL                  userACL;

  private static final Log         LOG                             = ExoLogger.getLogger(NewsServiceImpl.class);

  public NewsServiceImpl(SpaceService spaceService,
                         ActivityManager activityManager,
                         IdentityManager identityManager,
                         UploadService uploadService,
                         NewsESSearchConnector newsESSearchConnector,
                         IndexingService indexingService,
                         NewsStorage newsStorage,
                         UserACL userACL) {
    this.spaceService = spaceService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.newsESSearchConnector = newsESSearchConnector;
    this.indexingService = indexingService;
    this.newsStorage = newsStorage;
    this.userACL = userACL;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public News createNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    Space space = spaceService.getSpaceById(news.getSpaceId());
    try {
      if (!canCreateNews(space, currentIdentity)) {
        throw new IllegalArgumentException("Not authorized");
      }
      News createdNews;
      if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
        createdNews = postNews(news);
      } else if(news.getSchedulePostDate() != null){
        createdNews = unScheduleNews(news);
      } else {
        createdNews = newsStorage.createNews(news);
      }
      return createdNews;
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return null;
    }
  }
  
  public News postNews(News news) throws Exception {
    if (StringUtils.isEmpty(news.getId())) {
      news = newsStorage.createNews(news);
    } else {
      postNewsActivity(news);
      updateNews(news);
      sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);
    }
    if (news.isPublished()) {
      publishNews(news.getId());
    }
    NewsUtils.broadcastEvent(NewsUtils.POST_NEWS_ARTICLE, news.getId(), news);
    return news;
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
  public News updateNews(News news) throws Exception {
    
    Set<String> previousMentions = NewsUtils.processMentions(news.getBody());
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
        sendNotification(newMentionedNews, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS);
      }
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, String.valueOf(news.getId()));
    }
    NewsUtils.broadcastEvent(NewsUtils.UPDATE_NEWS, getCurrentUserId(), news);
    return news;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void deleteNews(String newsId, String currentUser, boolean isDraft) throws Exception {
    News news = newsStorage.getNewsById(newsId, false);
    newsStorage.deleteNews(newsId, currentUser, isDraft);
    NewsUtils.broadcastEvent(NewsUtils.DELETE_NEWS, currentUser, news);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, boolean editMode) {
    try {
      News news = newsStorage.getNewsById(newsId, editMode);
      news.setCanEdit(canEditNews(news, getCurrentUserId()));
      news.setCanDelete(canDeleteNews(news.getAuthor(),news.getSpaceId()));
      news.setCanPublish(canPublishNews());
      news.setCanArchive(canArchiveNews(news.getAuthor()));
      return news;
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while retrieving news with id " + newsId, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<News> getNews(NewsFilter newsFilter) throws Exception {
    List<News> newsList = newsStorage.getNews(newsFilter);
    newsList.stream().forEach(news -> {
      news.setCanEdit(canEditNews(news, getCurrentUserId()));
      news.setCanDelete(canDeleteNews(news.getAuthor(),news.getSpaceId()));
      news.setCanPublish(canPublishNews());
      news.setCanArchive(canArchiveNews(news.getAuthor()));
    });
    return newsList;
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
  public void markAsRead(News news, String userId) throws Exception {
    newsStorage.markAsRead(news, userId);
    //if(!newsStorage.isCurrentUserInNewsViewers()) {//FIXME not working for now
      NewsUtils.broadcastEvent(NewsUtils.VIEW_NEWS, getCurrentUserId(), news);
    //}
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishNews(String newsId) throws Exception {
    News news = getNewsById(newsId, false);
    newsStorage.publishNews(news);
    NewsUtils.broadcastEvent(NewsUtils.PUBLISH_NEWS, news.getId(), news);
    sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void unpublishNews(String newsId) throws Exception {
    newsStorage.unpublishNews(newsId);
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
      NewsUtils.broadcastEvent(NewsUtils.SHARE_NEWS, getCurrentUserId(), news);
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
    activity.setBody("");
    activity.setType("news");
    activity.setUserId(poster.getId());
    activity.isHidden(news.isActivityPosted());
    Map<String, String> templateParams = new HashMap<>();
    templateParams.put("newsId", news.getId());
    activity.setTemplateParams(templateParams);

    activityManager.saveActivityNoReturn(spaceIdentity, activity);
    newsStorage.updateNewsActivities(activity.getId(), news);
    String newsPoster = news.getAuthor();
    //TODO to be checked for analytics
    NewsUtils.broadcastEvent(NewsUtils.POST_NEWS, newsPoster, news);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void archiveNews(String newsId) throws Exception {
    newsStorage.archiveNews(newsId);
  }
  
  /**
   * Search news by term
   *
   * @param term
   * @param offset
   * @param limit
   * @return News Search Result
   */
  public List<NewsESSearchResult> search(Identity currentUser, String term, int offset, int limit) {
    return newsESSearchConnector.search(currentUser,term, offset, limit);
  }
  
  @Override
  public News getNewsById(String newsId, String authenticatedUser, boolean editMode) throws IllegalAccessException {
    News news = getNewsById(newsId, editMode);
    if (editMode) {
      if (!canEditNews(news, authenticatedUser)) {
        throw new IllegalAccessException("User " + authenticatedUser + " is not authorized to edit News");
      }
    } else if (!canViewNews(news, authenticatedUser)) {
      throw new IllegalAccessException("User " + authenticatedUser + " is not authorized to view News");
    }
    return news;
  }

  @Override
  public News getNewsByActivityId(String activityId, String authenticatedUser) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " wasn't found");
    }
    org.exoplatform.services.security.Identity viewerIdentity = NewsUtils.getUserIdentity(authenticatedUser);
    if (!activityManager.isActivityViewable(activity, viewerIdentity)) {
      throw new IllegalAccessException("User " + authenticatedUser + " isn't allowed to access activity with id " + activityId);
    }
    Map<String, String> templateParams = activity.getTemplateParams();
    if (templateParams == null) {
      throw new ObjectNotFoundException("Activity with id " + activityId + " isn't of type news nor a shared news");
    }
    String newsId = templateParams.get("newsId");
    if (StringUtils.isBlank(newsId)) {
      String originalActivityId = templateParams.get("originalActivityId");
      if (StringUtils.isNotBlank(originalActivityId)) {
        Identity sharedActivityPosterIdentity = identityManager.getIdentity(activity.getPosterId());
        if (sharedActivityPosterIdentity == null) {
          throw new IllegalAccessException("Shared Activity '" + activityId + "' Poster " + activity.getPosterId()
              + " isn't found");
        }
        return getNewsByActivityId(originalActivityId, sharedActivityPosterIdentity.getRemoteId());
      }
      throw new ObjectNotFoundException("Activity with id " + activityId + " isn't of type news nor a shared news");
    }
    return getNewsById(newsId, authenticatedUser, false);
  }

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
              || spaceService.isMember(space, username))) {
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

  @Override
  public void updateNewsActivity(News news, boolean post) {
    ExoSocialActivity activity = activityManager.getActivity(news.getActivityId());
    if(activity != null) {
      if (post) {
        activity.setUpdated(System.currentTimeMillis());
      }
      activity.isHidden(news.isActivityPosted());
      activityManager.updateActivity(activity, true);
    }
  }

  public News scheduleNews(News news) throws Exception {
    return newsStorage.scheduleNews(news);
  }

  public News unScheduleNews(News news) throws Exception {
    return newsStorage.unScheduleNews(news);
  }
  
  /**
   * Search news with the given text
   * 
   * @param filter news filter
   * @param lang language
   * @throws Exception when error
   */
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return newsStorage.searchNews(filter, lang);
  }

  /**
   * Unarchive a news
   *
   * @param newsId The id of the news to be unarchived
   * @throws Exception when error
   */
  public void unarchiveNews(String newsId) throws Exception {
    newsStorage.unarchiveNews(newsId);
  }

  public void sendNotification(News news, NotificationConstants.NOTIFICATION_CONTEXT context) throws Exception {
    String newsId = news.getId();
    String contentAuthor = news.getAuthor();
    String currentUser = getCurrentUserId() != null ? getCurrentUserId() : contentAuthor;
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
  
  private String getCurrentUserId() {
    org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
    return currentIdentity != null ? currentIdentity.getUserId() : null;
  }
  
  private org.exoplatform.services.security.Identity getCurrentIdentity() {
    ConversationState conversationState = ConversationState.getCurrent();
    return conversationState != null ? conversationState.getIdentity() : null;
  }
  
  public boolean canEditNews(News news, String authenticatedUser) {
    String spaceId = news.getSpaceId();
    Space space = spaceId == null ? null : spaceService.getSpaceById(spaceId);
    if (space == null) {
      return false;
    }
    Identity authenticatedUserIdentity = identityManager.getOrCreateUserIdentity(authenticatedUser);
    if (authenticatedUserIdentity == null) {
      LOG.warn("Can't find user with id {} when checking access on news with id {}", authenticatedUser, news.getId());
      return false;
    }
    String posterUsername = news.getAuthor();
    if (authenticatedUser.equals(posterUsername) || spaceService.isSuperManager(authenticatedUser)) {
      return true;
    }
    Space currentSpace = spaceService.getSpaceById(spaceId);
    if (spaceService.isManager(currentSpace, authenticatedUser)) {
      return true;
    }
    if (spaceService.isRedactor(currentSpace, authenticatedUser) && (news.isDraftVisible() || news.getSchedulePostDate() != null)
        && news.getActivities() == null) {
      return true;
    }
    org.exoplatform.services.security.Identity authenticatedSecurityIdentity = NewsUtils.getUserIdentity(authenticatedUser);
    return authenticatedSecurityIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }
  
  public boolean canDeleteNews(String posterId, String spaceId) {
    org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
    if (currentIdentity == null) {
      return false;
    }
    String authenticatedUser = currentIdentity.getUserId();
    Space currentSpace = spaceService.getSpaceById(spaceId);
    return authenticatedUser.equals(posterId) || userACL.isSuperUser() || spaceService.isSuperManager(authenticatedUser)
        || spaceService.isManager(currentSpace, authenticatedUser);
  }
  
  public boolean canPublishNews() {
    // FIXME shouldn't use ConversationState in Service layer
    org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
    if (currentIdentity == null) {
      return false;
    }
    return currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }
  
  public boolean canArchiveNews(String newsAuthor) {
    org.exoplatform.services.security.Identity currentIdentity = getCurrentIdentity();
    return currentIdentity != null && (currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
        || currentIdentity.getUserId().equals(newsAuthor));
  }
}