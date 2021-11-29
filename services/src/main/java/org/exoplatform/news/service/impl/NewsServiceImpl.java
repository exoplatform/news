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

/**
 * Service managing News and storing them in ECMS
 */
public class NewsServiceImpl implements NewsService {

  private static final String   PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private static final String   PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private static final String   NEWS_ID                         = "newsId";

  private SpaceService          spaceService;

  private ActivityManager       activityManager;

  private IdentityManager       identityManager;

  private NewsESSearchConnector newsESSearchConnector;

  private IndexingService       indexingService;

  private NewsStorage           newsStorage;

  private UserACL               userACL;

  private static final Log      LOG                             = ExoLogger.getLogger(NewsServiceImpl.class);

  public NewsServiceImpl(SpaceService spaceService,
                         ActivityManager activityManager,
                         IdentityManager identityManager,
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
        throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " not authorized to create news");
      }
      News createdNews;
      if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
        createdNews = postNews(news, currentIdentity.getUserId());
      } else if(news.getSchedulePostDate() != null){
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
        publishNews(news.getId(), updater);
      } else {
        unpublishNews(news.getId());
      }
    }
    
    newsStorage.updateNews(news);
    
    if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
      // Send mention notifs
      if (StringUtils.isNotEmpty(news.getId()) && news.getCreationDate() != null) {
        News newMentionedNews = news;
        if (!previousMentions.isEmpty()) {
          //clear old mentions from news body before sending a custom object to notification context.
          previousMentions.forEach(username -> newMentionedNews.setBody(newMentionedNews.getBody().replaceAll("@"+username, "")));
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
    
    newsStorage.deleteNews(newsId, isDraft);
    indexingService.unindex(NewsIndexingServiceConnector.TYPE, String.valueOf(news.getId()));
    NewsUtils.broadcastEvent(NewsUtils.DELETE_NEWS, currentIdentity.getUserId(), news);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, org.exoplatform.services.security.Identity currentIdentity, boolean editMode) throws IllegalAccessException {
    try {
      News news = getNewsById(newsId, editMode);
      if (editMode) {
        if (!canEditNews(news, currentIdentity.getUserId())) {
          throw new IllegalAccessException("User " + currentIdentity.getUserId() + " is not authorized to edit News");
        }
      } else if (!canViewNews(news, currentIdentity.getUserId())) {
        throw new IllegalAccessException("User " + currentIdentity.getUserId() + " is not authorized to view News");
      }
      news.setCanEdit(canEditNews(news, currentIdentity.getUserId()));
      news.setCanDelete(canDeleteNews(currentIdentity, news.getAuthor(), news.getSpaceId()));
      news.setCanPublish(canPublishNews(currentIdentity));
      news.setCanArchive(canArchiveNews(currentIdentity, news.getAuthor()));
      return news;
    } catch (Exception e) {
      throw new IllegalStateException("An error occurred while retrieving news with id " + newsId, e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    try {
      return newsStorage.getNewsById(newsId, editMode);
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
      news.setCanPublish(canPublishNews(currentIdentity));
      news.setCanArchive(canArchiveNews(currentIdentity, news.getAuthor()));
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
    if (templateParams == null || !activity.getTemplateParams().containsKey(NEWS_ID)) {
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
    return newsStorage.unScheduleNews(news);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public List<NewsESSearchResult> search(Identity currentIdentity, String term, int offset, int limit) {
    return newsESSearchConnector.search(currentIdentity, term, offset, limit);
  }
  
  /**
   * {@inheritDoc}
   */
  public News postNews(News news, String poster) throws Exception {
    postNewsActivity(news);
    updateNews(news, poster, null, news.isPublished());
    sendNotification(poster, news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);
    if (news.isPublished()) {
      publishNews(news.getId(), poster);
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
    if(!newsStorage.isCurrentUserInNewsViewers(news.getId(), userId)) {
      newsStorage.markAsRead(news, userId);
      NewsUtils.broadcastEvent(NewsUtils.VIEW_NEWS, userId, news);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void publishNews(String newsId, String publisher) throws Exception {
    News news = getNewsById(newsId, false);
    newsStorage.publishNews(news);
    NewsUtils.broadcastEvent(NewsUtils.PUBLISH_NEWS, news.getId(), news);
    sendNotification(publisher, news, NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS);
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
    activity.setBody("");
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

  /**
   * {@inheritDoc}
   */
  @Override
  public void unarchiveNews(String newsId) throws Exception {
    newsStorage.unarchiveNews(newsId);
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
  public boolean canPublishNews(org.exoplatform.services.security.Identity currentIdentity) {
    return currentIdentity != null && currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
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
        Map<String, String> mentionNewsNotificationInformations = new HashMap<String, String>();
        mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_BODY, contentBody);
        mentionNewsNotificationInformations.put(NotificationConstants.CURRENT_USER, currentUser);
        mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_AUTHOR, contentAuthor);
        mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_SPACE_ID, contentSpaceId);
        mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_TITLE, contentTitle);
        mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_SPACE, contentSpaceName);
        mentionNewsNotificationInformations.put(NotificationConstants.ILLUSTRATION_URL, illustrationURL);
        mentionNewsNotificationInformations.put(NotificationConstants.AUTHOR_AVATAR_URL, authorAvatarUrl);
        mentionNewsNotificationInformations.put(NotificationConstants.ACTIVITY_LINK, activityLink);
        sendMentionInNewsNotification(mentionNewsNotificationInformations);
      }
    } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS)) {
      Map<String, String> mentionNewsNotificationInformations = new HashMap<String, String>();
      mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_BODY, contentBody);
      mentionNewsNotificationInformations.put(NotificationConstants.CURRENT_USER, currentUser);
      mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_AUTHOR, contentAuthor);
      mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_SPACE_ID, contentSpaceId);
      mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_TITLE, contentTitle);
      mentionNewsNotificationInformations.put(NotificationConstants.CONTENT_SPACE, contentSpaceName);
      mentionNewsNotificationInformations.put(NotificationConstants.ILLUSTRATION_URL, illustrationURL);
      mentionNewsNotificationInformations.put(NotificationConstants.AUTHOR_AVATAR_URL, authorAvatarUrl);
      mentionNewsNotificationInformations.put(NotificationConstants.ACTIVITY_LINK, activityLink);
      sendMentionInNewsNotification(mentionNewsNotificationInformations);
    } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS)) {
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(PublishNewsNotificationPlugin.ID))).execute(ctx);
    }
  }
  
  private void sendMentionInNewsNotification(Map<String, String> mentionNewsNotificationInformations) {
    Set<String> mentionedIds = NewsUtils.processMentions(mentionNewsNotificationInformations.get(NotificationConstants.CONTENT_BODY));
    NotificationContext mentionNotificationCtx = NotificationContextImpl.cloneInstance()
            .append(MentionInNewsNotificationPlugin.CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS)
            .append(PostNewsNotificationPlugin.CURRENT_USER, mentionNewsNotificationInformations.get(NotificationConstants.CURRENT_USER))
            .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, mentionNewsNotificationInformations.get(NotificationConstants.CONTENT_AUTHOR))
            .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, mentionNewsNotificationInformations.get(NotificationConstants.CONTENT_SPACE_ID))
            .append(PostNewsNotificationPlugin.CONTENT_TITLE, mentionNewsNotificationInformations.get(NotificationConstants.CONTENT_TITLE))
            .append(PostNewsNotificationPlugin.CONTENT_SPACE, mentionNewsNotificationInformations.get(NotificationConstants.CONTENT_SPACE))
            .append(PostNewsNotificationPlugin.ILLUSTRATION_URL, mentionNewsNotificationInformations.get(NotificationConstants.ILLUSTRATION_URL))
            .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL, mentionNewsNotificationInformations.get(NotificationConstants.AUTHOR_AVATAR_URL))
            .append(PostNewsNotificationPlugin.ACTIVITY_LINK, mentionNewsNotificationInformations.get(NotificationConstants.ACTIVITY_LINK))
            .append(MentionInNewsNotificationPlugin.MENTIONED_IDS, mentionedIds);
    mentionNotificationCtx.getNotificationExecutor().with(mentionNotificationCtx.makeCommand(PluginKey.key(MentionInNewsNotificationPlugin.ID))).execute(mentionNotificationCtx);
  }
  
  private void updateNewsActivity(News news, boolean post) {
    ExoSocialActivity activity = activityManager.getActivity(news.getActivityId());
    if(activity != null) {
      if (post) {
        activity.setUpdated(System.currentTimeMillis());
      }
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
    if (spaceService.isRedactor(currentSpace, authenticatedUser) && news.isDraftVisible() && news.getActivities() == null) {
      return true;
    }
    org.exoplatform.services.security.Identity authenticatedSecurityIdentity = NewsUtils.getUserIdentity(authenticatedUser);
    return authenticatedSecurityIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
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
}