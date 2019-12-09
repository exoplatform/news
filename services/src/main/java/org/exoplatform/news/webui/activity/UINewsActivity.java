package org.exoplatform.news.webui.activity;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.plugin.CommentNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.CommentSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "war:/groovy/news/webui/activity/UINewsActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = UINewsActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = UINewsActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class) })
public class UINewsActivity extends BaseUIActivity {
  public static final String  ACTIVITY_TYPE                   = "news";

  private final static String PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private static final String MANAGER_MEMBERSHIP_NAME         = "manager";

  private News                news;

  public News getNews() {
    return news;
  }

  public void setNews(News news) {
    this.news = news;
  }

  public String getUserFullName(String userId) {
    String fullName = "";
    Identity identity = CommonsUtils.getService(IdentityManager.class)
                                    .getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
    if (identity != null) {
      fullName = identity.getProfile().getFullName();
    }
    return fullName;
  }

  public String getUserProfileURL(String userId) {
    return LinkProvider.getUserProfileUri(userId);
  }

  public boolean canEditNews(ExoSocialActivity activity) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = currentIdentity.getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    String authenticatedUserId = currentUser.getId();
    SpaceService spaceService = Utils.getSpaceService();
    Space currentSpace = spaceService.getSpaceById(news.getSpaceId());
    return authenticatedUserId.equals(activity.getPosterId()) || spaceService.isSuperManager(authenticatedUser)
        || currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
        || currentIdentity.isMemberOf(currentSpace.getGroupId(), MANAGER_MEMBERSHIP_NAME);
  }

  public boolean canPinNews(ExoSocialActivity activity) {
    return ConversationState.getCurrent().getIdentity().isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }

  public static class LikeActivityActionListener extends BaseUIActivity.LikeActivityActionListener {

    public void execute(Event<BaseUIActivity> event) throws Exception {
      super.execute(event);
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      boolean isLiked = Boolean.parseBoolean(isLikedStr);
      if (isLiked) {
        BaseUIActivity uiActivity = event.getSource();
        ExoSocialActivity activity = uiActivity.getActivity();
        String[] likersId = activity.getLikeIdentityIds();
        String liker = org.exoplatform.social.notification.Utils.getUserId(likersId[likersId.length - 1]);
        if (activity != null) {
          if (activity.getTemplateParams() != null) {
            if (activity.getTemplateParams().get("newsId") != null) {
              String newsId = activity.getTemplateParams().get("newsId");
              String activityId = activity.getId();
              String spaceId = activity.getActivityStream().getPrettyId();
              String posterId = activity.getPosterId();
              SessionProvider systemProvider = SessionProvider.createSystemProvider();
              SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
              sessionProviderService.setSessionProvider(null, systemProvider);
              NewsService newsService = CommonsUtils.getService(NewsService.class);
              News news = newsService.getNewsById(newsId);
              String posterActivityUserName = news.getAuthor();
              IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
              Identity posterActivity = identityManager.getIdentity(posterId);
              if (posterActivity != null) {
                posterActivityUserName = posterActivity.getProfile().getProperty(Profile.USERNAME).toString();
              }
              if (!liker.equals(posterActivityUserName)) {
                NotificationConstants.NOTIFICATION_CONTEXT context = NotificationConstants.NOTIFICATION_CONTEXT.LIKE_MY_NEWS;
                String activities = news.getActivities();
                String firstSpaceIdActivityId = activities.split(";")[0];
                String firstActivityId = firstSpaceIdActivityId.split(":")[1];
                SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
                Space contentSpace = spaceService.getSpaceByPrettyName(spaceId);
                if (!firstActivityId.equals(activityId) || !spaceId.equals(contentSpace.getPrettyName())) {
                  context = NotificationConstants.NOTIFICATION_CONTEXT.LIKE_MY_SHARED_NEWS;
                }
                boolean isMember = spaceService.isMember(contentSpace, posterActivityUserName);
                String contentSpaceName = contentSpace.getDisplayName();
                String illustrationUrl = NotificationUtils.getNewsIllustration(news);
                String activityLink = NotificationUtils.getNotificationActivityLink(contentSpace, activityId, isMember);
                NotificationContext ctx =
                                        NotificationContextImpl.cloneInstance()
                                                               .append(LikeNewsNotificationPlugin.CONTENT_TITLE, news.getTitle())
                                                               .append(LikeNewsNotificationPlugin.ACTIVITY_LINK, activityLink)
                                                               .append(LikeNewsNotificationPlugin.ILLUSTRATION_URL,
                                                                       illustrationUrl)
                                                               .append(LikeNewsNotificationPlugin.CONTENT_SPACE, contentSpaceName)
                                                               .append(LikeNewsNotificationPlugin.CURRENT_USER, liker)
                                                               .append(LikeNewsNotificationPlugin.CONTENT_AUTHOR,
                                                                       news.getAuthor())
                                                               .append(LikeNewsNotificationPlugin.POSTER_ACTIVITY_USER_NAME,
                                                                       posterActivityUserName)
                                                               .append(LikeNewsNotificationPlugin.CONTEXT, context);
                if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.LIKE_MY_NEWS)) {
                  ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(LikeNewsNotificationPlugin.ID))).execute(ctx);
                } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.LIKE_MY_SHARED_NEWS)) {
                  ctx.getNotificationExecutor()
                     .with(ctx.makeCommand(PluginKey.key(LikeSharedNewsNotificationPlugin.ID)))
                     .execute(ctx);
                }

              }
            }
          }

        }
      }
    }
  }

  public static class PostCommentActionListener extends BaseUIActivity.PostCommentActionListener {

    public void execute(Event<BaseUIActivity> event) throws Exception {
      super.execute(event);
      BaseUIActivity uiActivity = event.getSource();
      ExoSocialActivity activity = uiActivity.getActivity();
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      String authenticatedUser = currentIdentity.getUserId();
      Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                         .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
      String authenticatedUserName = currentUser.getProfile().getProperty(Profile.USERNAME).toString();
      if (activity != null) {
        if (activity.getTemplateParams() != null) {
          if (activity.getTemplateParams().get("newsId") != null) {
            String newsId = activity.getTemplateParams().get("newsId");
            String activityId = activity.getId();
            String spaceId = activity.getActivityStream().getPrettyId();
            String posterId = activity.getPosterId();
            SessionProvider systemProvider = SessionProvider.createSystemProvider();
            SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
            sessionProviderService.setSessionProvider(null, systemProvider);
            NewsService newsService = CommonsUtils.getService(NewsService.class);
            News news = newsService.getNewsById(newsId);
            String posterActivityUserName = news.getAuthor();
            IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
            Identity posterActivity = identityManager.getIdentity(posterId);
            if (posterActivity != null) {
              posterActivityUserName = posterActivity.getProfile().getProperty(Profile.USERNAME).toString();
            }
            if (!authenticatedUserName.equals(posterActivityUserName)) {
              NotificationConstants.NOTIFICATION_CONTEXT context = NotificationConstants.NOTIFICATION_CONTEXT.COMMENT_MY_NEWS;
              String activities = news.getActivities();
              String firstSpaceIdActivityId = activities.split(";")[0];
              String firstActivityId = firstSpaceIdActivityId.split(":")[1];
              SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
              Space contentSpace = spaceService.getSpaceByPrettyName(spaceId);
              if (!firstActivityId.equals(activityId) || !spaceId.equals(contentSpace.getPrettyName())) {
                context = NotificationConstants.NOTIFICATION_CONTEXT.COMMENT_MY_SHARED_NEWS;
              }
              boolean isMember = spaceService.isMember(contentSpace, posterActivityUserName);
              String contentSpaceName = contentSpace.getDisplayName();
              String illustrationUrl = NotificationUtils.getNewsIllustration(news);
              String activityLink = NotificationUtils.getNotificationActivityLink(contentSpace, activityId, isMember);
              NotificationContext ctx =
                                      NotificationContextImpl.cloneInstance()
                                                             .append(CommentNewsNotificationPlugin.CONTENT_TITLE, news.getTitle())
                                                             .append(CommentNewsNotificationPlugin.ACTIVITY_LINK, activityLink)
                                                             .append(CommentNewsNotificationPlugin.ILLUSTRATION_URL,
                                                                     illustrationUrl)
                                                             .append(CommentNewsNotificationPlugin.CONTENT_SPACE,
                                                                     contentSpaceName)
                                                             .append(CommentNewsNotificationPlugin.CURRENT_USER,
                                                                     authenticatedUserName)
                                                             .append(CommentNewsNotificationPlugin.CONTENT_AUTHOR,
                                                                     news.getAuthor())
                                                             .append(CommentNewsNotificationPlugin.POSTER_ACTIVITY_USER_NAME,
                                                                     posterActivityUserName)
                                                             .append(CommentNewsNotificationPlugin.CONTEXT, context);
              if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.COMMENT_MY_NEWS)) {
                ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(CommentNewsNotificationPlugin.ID))).execute(ctx);
              } else if (context.equals(NotificationConstants.NOTIFICATION_CONTEXT.COMMENT_MY_SHARED_NEWS)) {
                ctx.getNotificationExecutor()
                   .with(ctx.makeCommand(PluginKey.key(CommentSharedNewsNotificationPlugin.ID)))
                   .execute(ctx);
              }
            }

          }

        }
      }
    }
  }
}
