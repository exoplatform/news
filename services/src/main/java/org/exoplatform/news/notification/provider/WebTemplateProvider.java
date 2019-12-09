package org.exoplatform.news.notification.provider;

import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.plugin.CommentNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.CommentSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.PostNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareMyNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareNewsNotificationPlugin;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PostNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = ShareNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = ShareMyNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeSharedNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = CommentNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = CommentSharedNewsNotificationPlugin.ID, template = "war:/notification/templates/web/postNewsNotificationPlugin.gtmpl") })
public class WebTemplateProvider extends TemplateProvider {
  protected static Log log = ExoLogger.getLogger(WebTemplateProvider.class);

  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(PostNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(ShareNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(ShareMyNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(LikeNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(LikeSharedNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(CommentNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(CommentSharedNewsNotificationPlugin.ID), new TemplateBuilder());
  }

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String pluginId = notification.getKey().getId();

      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);

      String contentAuthor = notification.getValueOwnerParameter("CONTENT_AUTHOR");
      String currentUser = notification.getValueOwnerParameter(NotificationConstants.CURRENT_USER);
      String contentTitle = notification.getValueOwnerParameter(NotificationConstants.CONTENT_TITLE);
      String contentSpaceName = notification.getValueOwnerParameter(NotificationConstants.CONTENT_SPACE);
      String illustrationUrl = notification.getValueOwnerParameter(NotificationConstants.ILLUSTRATION_URL);
      String activityLink = notification.getValueOwnerParameter(NotificationConstants.ACTIVITY_LINK);
      String context = notification.getValueOwnerParameter(NotificationConstants.CONTEXT);

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      templateContext.put("CONTENT_TITLE", encoder.encode(contentTitle));
      templateContext.put(NotificationConstants.CONTENT_SPACE, encoder.encode(contentSpaceName));
      templateContext.put("CONTENT_AUTHOR", encoder.encode(contentAuthor));
      templateContext.put("CURRENT_USER", currentUser);
      templateContext.put("ILLUSTRATION_URL", encoder.encode(illustrationUrl));
      templateContext.put("ACTIVITY_LINK", encoder.encode(activityLink));
      templateContext.put("CONTEXT", encoder.encode(context));
      templateContext.put("READ",
                          Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read"
                                                                                                                                : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("LAST_UPDATED_TIME",
                          TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),
                                                                       "EE, dd yyyy",
                                                                       new Locale(language),
                                                                       TimeConvertUtils.YEAR));

      //
      String body = TemplateUtils.processGroovy(templateContext);
      // binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }

  };

}
