package org.exoplatform.news.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class CommentSharedNewsNotificationPlugin extends CommentNewsNotificationPlugin {
  private static final Log   LOG = ExoLogger.getLogger(CommentSharedNewsNotificationPlugin.class);

  public final static String ID  = "CommentSharedNewsNotificationPlugin";

  public CommentSharedNewsNotificationPlugin(InitParams initParams) {
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

}
