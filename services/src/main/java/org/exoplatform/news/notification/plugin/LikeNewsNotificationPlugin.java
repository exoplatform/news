package org.exoplatform.news.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.notification.utils.NotificationUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class LikeNewsNotificationPlugin extends BaseNotificationPlugin {
  private static final Log                    LOG                       = ExoLogger.getLogger(LikeNewsNotificationPlugin.class);

  public final static String                  ID                        = "LikeNewsNotificationPlugin";

  public final static ArgumentLiteral<String> CONTENT_TITLE             =
                                                            new ArgumentLiteral<String>(String.class, "CONTENT_TITLE");

  public static final ArgumentLiteral<String> ACTIVITY_LINK             =
                                                            new ArgumentLiteral<String>(String.class, "ACTIVITY_LINK");

  public static final ArgumentLiteral<String> CONTENT_SPACE             =
                                                            new ArgumentLiteral<String>(String.class, "CONTENT_SPACE");

  public static final ArgumentLiteral<String> CURRENT_USER              =
                                                           new ArgumentLiteral<String>(String.class, "CURRENT_USER");

  public static final ArgumentLiteral<String> ILLUSTRATION_URL          =
                                                               new ArgumentLiteral<String>(String.class, "ILLUSTRATION_URL");

  public static final ArgumentLiteral<String> CONTEXT                   = new ArgumentLiteral<String>(String.class, "CONTEXT");

  public static final ArgumentLiteral<String> CONTENT_AUTHOR            =
                                                             new ArgumentLiteral<String>(String.class, "CONTENT_AUTHOR");

  public static final ArgumentLiteral<String> POSTER_ACTIVITY_USER_NAME =
                                                                        new ArgumentLiteral<String>(String.class,
                                                                                                    "POSTER_ACTIVITY_USER_NAME");

  public LikeNewsNotificationPlugin(InitParams initParams) {
    super(initParams);
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
    String newsTitle = ctx.value(CONTENT_TITLE);
    String activityLink = ctx.value(ACTIVITY_LINK);
    String contentSpaceName = ctx.value(CONTENT_SPACE);
    String illustrationUrl = ctx.value(ILLUSTRATION_URL);
    String currentUserName = ctx.value(CURRENT_USER);
    String newsAuthor = ctx.value(CONTENT_AUTHOR);
    String context = ctx.value(CONTEXT);
    String posterActivityUserName = ctx.value(POSTER_ACTIVITY_USER_NAME);
    String currentUserFullName = currentUserName;
    try {
      currentUserFullName = NotificationUtils.getUserFullName(currentUserName);
    } catch (Exception e) {
      LOG.error("An error occured when trying to retreive a user with username " + currentUserName + " " + e.getMessage(), e);
    }

    return NotificationInfo.instance()
                           .setFrom(currentUserName)
                           .with(NotificationConstants.CONTENT_TITLE, newsTitle)
                           .to(posterActivityUserName)
                           .with(NotificationConstants.CONTENT_AUTHOR, newsAuthor)
                           .with(NotificationConstants.CURRENT_USER, currentUserFullName)
                           .with(NotificationConstants.CONTENT_SPACE, contentSpaceName)
                           .with(NotificationConstants.ILLUSTRATION_URL, illustrationUrl)
                           .with(NotificationConstants.ACTIVITY_LINK, activityLink)
                           .with(NotificationConstants.CONTEXT, context)
                           .key(getKey())
                           .end();

  }

}
