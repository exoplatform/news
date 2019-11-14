package org.exoplatform.news.notification.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class PostNewsNotificationPlugin extends BaseNotificationPlugin {
  private static final Log                    LOG              = ExoLogger.getLogger(PostNewsNotificationPlugin.class);

  public final static String                  ID               = "PostNewsNotificationPlugin";

  public final static ArgumentLiteral<String> CONTENT_TITLE    = new ArgumentLiteral<String>(String.class, "CONTENT_TITLE");

  public final static ArgumentLiteral<String> CONTENT_UPDATER  = new ArgumentLiteral<String>(String.class, "CONTENT_UPDATER");

  public static final ArgumentLiteral<String> CONTENT_AUTHOR   = new ArgumentLiteral<String>(String.class, "CONTENT_AUTHOR");

  public static final ArgumentLiteral<String> CONTENT_SPACE    = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE");

  public static final ArgumentLiteral<String> CONTENT_SPACE_ID = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE_ID");

  public static final ArgumentLiteral<String> ILLUSTRATION_URL = new ArgumentLiteral<String>(String.class, "ILLUSTRATION_URL");

  public static final ArgumentLiteral<String> ACTIVITY_LINK    = new ArgumentLiteral<String>(String.class, "ACTIVITY_LINK");

  private SpaceService                        spaceService;

  private UserHandler                         userhandler;

  public PostNewsNotificationPlugin(InitParams initParams, SpaceService spaceService, OrganizationService organizationService) {
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
    String contentTitle = ctx.value(CONTENT_TITLE);
    String contentAuthorUserName = ctx.value(CONTENT_AUTHOR);
    User contentAuthorUser;
    String contentAuthor = contentAuthorUserName;
    try {
      contentAuthorUser = userhandler.findUserByName(contentAuthorUserName);
      contentAuthor = contentAuthorUser.getFullName();
    } catch (Exception e) {
      LOG.error("An error occured when trying to retreive a user with username " + contentAuthorUserName + " " + e.getMessage(), e);
    }
    String contentSpaceId = ctx.value(CONTENT_SPACE_ID);
    String contentSpaceName = ctx.value(CONTENT_SPACE);
    String illustrationUrl = ctx.value(ILLUSTRATION_URL);
    String activityLink = ctx.value(ACTIVITY_LINK);

    List<String> receivers = new ArrayList<String>();
    try {
      receivers = getReceivers(contentSpaceId, contentAuthorUserName);
    } catch (Exception e) {
      LOG.error("An error occured when trying to have the list of receivers " + e.getMessage(), e);
    }

    return NotificationInfo.instance()
                           .setFrom(contentAuthorUserName)
                           .with(NotificationConstants.CONTENT_TITLE, contentTitle)
                           .to(receivers)
                           .with(NotificationConstants.CONTENT_AUTHOR, contentAuthor)
                           .with(NotificationConstants.CONTENT_SPACE, contentSpaceName)
                           .with(NotificationConstants.ILLUSTRATION_URL, illustrationUrl)
                           .with(NotificationConstants.ACTIVITY_LINK, activityLink)
                           .key(getKey())
                           .end();

  }

  private List<String> getReceivers(String contentSpaceId, String contentAuthorUserName) throws Exception {
    Space space = spaceService.getSpaceById(contentSpaceId);
    ListAccess<User> members = userhandler.findUsersByGroupId(space.getGroupId());
    User[] userArray = members.load(0, members.getSize());
    List<User> receiverUsers = Arrays.stream(userArray)
                                     .filter(u -> !u.getUserName().equals(contentAuthorUserName))
                                     .collect(Collectors.toList());
    List<String> receiversIds = new ArrayList<String>();
    receiverUsers.forEach(u -> receiversIds.add(u.getUserName()));
    // remove redondance
    Set<String> receiversSet = new HashSet<String>(receiversIds);
    // convert the set to List to be used after in to method
    List<String> receivers = new ArrayList(receiversSet);
    return receivers;
  }
}
