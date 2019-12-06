package org.exoplatform.news.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class LikeSharedNewsNotificationPlugin extends LikeNewsNotificationPlugin {
  private static final Log   LOG = ExoLogger.getLogger(LikeSharedNewsNotificationPlugin.class);

  public final static String ID  = "LikeSharedNewsNotificationPlugin";

  public LikeSharedNewsNotificationPlugin(InitParams initParams) {
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
