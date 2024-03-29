package org.exoplatform.news.notification.plugin;

import java.lang.reflect.AccessibleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.model.MetadataObject;

import static org.exoplatform.news.utils.NewsUtils.NewsObjectType.ARTICLE;

public class PostNewsNotificationPlugin extends BaseNotificationPlugin {
  private static final Log                    LOG              = ExoLogger.getLogger(PostNewsNotificationPlugin.class);

  public static final  String                  ID               = "PostNewsNotificationPlugin";

  public static final ArgumentLiteral<String> CONTENT_TITLE    = new ArgumentLiteral<>(String.class, "CONTENT_TITLE");

  public static final ArgumentLiteral<String> CONTENT_AUTHOR   = new ArgumentLiteral<>(String.class, "CONTENT_AUTHOR");

  public static final ArgumentLiteral<String> CONTENT_SPACE    = new ArgumentLiteral<>(String.class, "CONTENT_SPACE");

  public static final ArgumentLiteral<String> CONTENT_SPACE_ID = new ArgumentLiteral<>(String.class, "CONTENT_SPACE_ID");

  public static final ArgumentLiteral<String> ILLUSTRATION_URL = new ArgumentLiteral<>(String.class, "ILLUSTRATION_URL");

  public static final ArgumentLiteral<String> AUTHOR_AVATAR_URL = new ArgumentLiteral<>(String.class, "AUTHOR_AVATAR_URL");

  public static final ArgumentLiteral<String> ACTIVITY_LINK    = new ArgumentLiteral<>(String.class, "ACTIVITY_LINK");

  public static final ArgumentLiteral<String> NEWS_ID          = new ArgumentLiteral<>(String.class, "NEWS_ID");

  public static final ArgumentLiteral<String> AUDIENCE         = new ArgumentLiteral<>(String.class, "AUDIENCE");

  public static final ArgumentLiteral<NotificationConstants.NOTIFICATION_CONTEXT> CONTEXT          = new ArgumentLiteral<>(NotificationConstants.NOTIFICATION_CONTEXT.class, "CONTEXT");

  public static final ArgumentLiteral<String> CURRENT_USER     = new ArgumentLiteral<>(String.class, "CURRENT_USER");

  private SpaceService                        spaceService;
  private NewsService                         newsService;
  private ActivityManager                     activityManager;

  private UserHandler                         userhandler;

  public PostNewsNotificationPlugin(InitParams initParams, SpaceService spaceService, OrganizationService organizationService, NewsService newsService, ActivityManager activityManager) {
    super(initParams);
    this.spaceService = spaceService;
    this.newsService = newsService;
    this.activityManager = activityManager;
    this.userhandler = organizationService.getUserHandler();
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {
    String contentTitle = ctx.value(CONTENT_TITLE);
    NotificationConstants.NOTIFICATION_CONTEXT context = ctx.value(CONTEXT);
    String contentAuthorUserName = ctx.value(CONTENT_AUTHOR);
    String contentAuthor = contentAuthorUserName;
    try {
      contentAuthor = NotificationUtils.getUserFullName(contentAuthorUserName);
    } catch (Exception e) {
      LOG.error("An error occured when trying to retreive a user with username " + contentAuthorUserName + " " + e.getMessage(),
                e);
    }
    String currentUserName = ctx.value(CURRENT_USER);
    String currentUserFullName = currentUserName;
    try {
      currentUserFullName = NotificationUtils.getUserFullName(currentUserName);
    } catch (Exception e) {
      LOG.error("An error occured when trying to retreive a user with username " + currentUserName + " " + e.getMessage(), e);
    }
    String contentSpaceId = ctx.value(CONTENT_SPACE_ID);
    String contentSpaceName = ctx.value(CONTENT_SPACE);
    String illustrationUrl = ctx.value(ILLUSTRATION_URL);
    String authorAvatarUrl = ctx.value(AUTHOR_AVATAR_URL);
    String activityLink = ctx.value(ACTIVITY_LINK);
    String newsId = ctx.value(NEWS_ID);

    if (mustSendNotification(newsId, currentUserName)) {
      List<String> receivers = new ArrayList<String>();
      try {
        receivers = getReceivers(contentSpaceId, currentUserName);
      } catch (Exception e) {
        LOG.error("An error occured when trying to have the list of receivers " + e.getMessage(), e);
      }

      return NotificationInfo.instance()
                             .setFrom(currentUserName)
                             .setSpaceId(Long.parseLong(contentSpaceId))
                             .with(NotificationConstants.CONTENT_TITLE, contentTitle)
                             .to(receivers)
                             .with(NotificationConstants.CONTENT_AUTHOR, contentAuthor)
                             .with(NotificationConstants.CURRENT_USER, currentUserFullName)
                             .with(NotificationConstants.CONTENT_SPACE, contentSpaceName)
                             .with(NotificationConstants.ILLUSTRATION_URL, illustrationUrl)
                             .with(NotificationConstants.AUTHOR_AVATAR_URL, authorAvatarUrl)
                             .with(NotificationConstants.ACTIVITY_LINK, activityLink)
                             .with(NotificationConstants.CONTEXT, context.getContext())
                             .with(NotificationConstants.NEWS_ID, newsId)
                             .key(getKey())
                             .end();
    }

    return null;
  }

  private boolean mustSendNotification(String newsId, String userName) {
    News news = null;
    try {
      news = newsService.getNewsById(newsId, NewsUtils.getUserIdentity(userName), false, ARTICLE.name().toLowerCase());
    } catch (Exception e) {
      LOG.warn("Error retrieving news by id {}", newsId, e);
      return false;
    }
    if (news == null) {
      LOG.debug("News by id {} wasn't found. The space web notification will not be sent.", newsId);
      return false;
    }
    String activityId = news.getActivityId();
    if (StringUtils.isBlank(activityId)) {
      return false;
    }
    ExoSocialActivity activity = activityManager.getActivity(activityId);
    if (activity.isHidden()) {
      return false;
    }
    return true;
  }

  private List<String> getReceivers(String contentSpaceId,
                                    String currentUserName) throws Exception {
    Space space = spaceService.getSpaceById(contentSpaceId);
    ListAccess<User> members = userhandler.findUsersByGroupId(space.getGroupId());
    User[] userArray = members.load(0, members.getSize());
    List<String> receiverUsers = Arrays.stream(userArray)
            .filter(u -> !u.getUserName().equals(currentUserName))
            .distinct()
            .map(User::getUserName)
            .collect(Collectors.toList());
    return receiverUsers;
  }
}
