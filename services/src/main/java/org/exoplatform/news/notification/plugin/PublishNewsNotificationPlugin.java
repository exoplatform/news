package org.exoplatform.news.notification.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class PublishNewsNotificationPlugin extends BaseNotificationPlugin {

  private static final Log   LOG = ExoLogger.getLogger(PublishNewsNotificationPlugin.class);

  public static final String ID  = "PublishNewsNotificationPlugin";
  
  private SpaceService       spaceService;
  
  public PublishNewsNotificationPlugin(InitParams initParams, SpaceService spaceService) {
    super(initParams);
    this.spaceService = spaceService;
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
    String contentTitle = ctx.value(PostNewsNotificationPlugin.CONTENT_TITLE);
    NotificationConstants.NOTIFICATION_CONTEXT context = ctx.value(PostNewsNotificationPlugin.CONTEXT);
    String contentAuthorUserName = ctx.value(PostNewsNotificationPlugin.CONTENT_AUTHOR);
    String contentAuthor = contentAuthorUserName;
    try {
      contentAuthor = NotificationUtils.getUserFullName(contentAuthorUserName);
    } catch (Exception e) {
      LOG.error("An error occurred when trying to retrieve a user with username " + contentAuthorUserName + " " + e.getMessage(),
                e);
    }
    String currentUserName = ctx.value(PostNewsNotificationPlugin.CURRENT_USER);
    String currentUserFullName = currentUserName;
    try {
      currentUserFullName = NotificationUtils.getUserFullName(currentUserName);
    } catch (Exception e) {
      LOG.error("An error occurred when trying to retrieve a user with username " + currentUserName + " " + e.getMessage(), e);
    }
    String contentSpaceName = ctx.value(PostNewsNotificationPlugin.CONTENT_SPACE);
    String illustrationUrl = ctx.value(PostNewsNotificationPlugin.ILLUSTRATION_URL);
    String authorAvatarUrl = ctx.value(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL);
    String activityLink = ctx.value(PostNewsNotificationPlugin.ACTIVITY_LINK);
    String newsId = ctx.value(PostNewsNotificationPlugin.NEWS_ID);
    String contentSpaceId = ctx.value(PostNewsNotificationPlugin.CONTENT_SPACE_ID);
    String audience = ctx.value(PostNewsNotificationPlugin.AUDIENCE);
    List<String> receivers = new ArrayList<String>();
    try {
      receivers = getReceivers(contentSpaceId, currentUserName);
    } catch (Exception e) {
      LOG.error("An error occured when trying to have the list of receivers " + e.getMessage(), e);
    }
    NotificationInfo notificationInfo = NotificationInfo.instance()
        .setFrom(currentUserName)
        .exclude(currentUserName)
        .with(NotificationConstants.CONTENT_TITLE, contentTitle)
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

    if (audience.equals("Only space members")) {
      notificationInfo.to(receivers);
    } 
    else {
      notificationInfo.setSendAllInternals(true);
    }
    return notificationInfo.end();

  }

  private List<String> getReceivers(String contentSpaceId,
                                    String currentUserName) throws Exception {
    Space space = spaceService.getSpaceById(contentSpaceId);
    List<String> receivers = Arrays.stream(space.getMembers())
            .filter(member -> !member.equals(currentUserName))
            .distinct()
            .collect(Collectors.toList());
    return receivers;
  }
}
