package org.exoplatform.news.notification.provider;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.plugin.*;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.spi.SpaceService;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PostNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = MentionInNewsNotificationPlugin.ID, template = "war:/notification/templates/push/postNewsNotificationPlugin.gtmpl") })
public class PushTemplateProvider extends WebTemplateProvider {
  protected static Log log = ExoLogger.getLogger(PushTemplateProvider.class);

  public PushTemplateProvider(InitParams initParams, SpaceService spaceService) {
    super(initParams, spaceService);
  }
}
