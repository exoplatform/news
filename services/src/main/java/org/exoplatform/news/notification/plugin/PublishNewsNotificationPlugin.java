package org.exoplatform.news.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PublishNewsNotificationPlugin extends BaseNotificationPlugin {

  private static final Log   LOG = ExoLogger.getLogger(PublishNewsNotificationPlugin.class);

  public static final String ID  = "PublishNewsNotificationPlugin";
  
  private SpaceService       spaceService;
  
  private UserHandler        userhandler;

  public PublishNewsNotificationPlugin(InitParams initParams, SpaceService spaceService, OrganizationService organizationService) {
    super(initParams);
    this.spaceService = spaceService;
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
      receivers = getReceivers(contentSpaceId, currentUserName, audience);
    } catch (Exception e) {
      LOG.error("An error occured when trying to have the list of receivers " + e.getMessage(), e);
    }


    return NotificationInfo.instance()
                           .setFrom(currentUserName)
                           .to(receivers)
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

  }

  private List<String> getReceivers(String contentSpaceId,
                                    String currentUserName, String audience) throws Exception {
    ListAccess<User> members = null;
    if (audience.equals("Only space members")){
      Space space = spaceService.getSpaceById(contentSpaceId);
      members = userhandler.findUsersByGroupId(space.getGroupId());
    } else {
      members = userhandler.findUsersByGroupId("/platform/users");
    }
    User[] userArray = members.load(0, members.getSize());
    List<String> receiverUsers = Arrays.stream(userArray)
            .filter(u -> !u.getUserName().equals(currentUserName))
            .distinct()
            .map(User::getUserName)
            .collect(Collectors.toList());

    return receiverUsers;
  }
}
