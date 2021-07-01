package org.exoplatform.news.notification.provider;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.exoplatform.commons.notification.template.DigestTemplate;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.plugin.*;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = PostNewsNotificationPlugin.ID, template = "war:/notification/templates/mail/postNewsNotificationPlugin.gtmpl"),
    @TemplateConfig(pluginId = MentionInNewsNotificationPlugin.ID, template = "war:/notification/templates/mail/postNewsNotificationPlugin.gtmpl") })
public class MailTemplateProvider extends TemplateProvider {
  protected static Log    log = ExoLogger.getLogger(MailTemplateProvider.class);

  private IdentityManager identityManager;

  public MailTemplateProvider(InitParams initParams, IdentityManager identityManager) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(PostNewsNotificationPlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(MentionInNewsNotificationPlugin.ID), new TemplateBuilder());
    this.identityManager = identityManager;
  }

  protected class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String pluginId = notification.getKey().getId();

      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);

      String contentAuthor = notification.getValueOwnerParameter(NotificationConstants.CONTENT_AUTHOR);
      String currentUser = notification.getValueOwnerParameter(NotificationConstants.CURRENT_USER);
      String contentTitle = notification.getValueOwnerParameter(NotificationConstants.CONTENT_TITLE);
      String contentSpaceName = notification.getValueOwnerParameter(NotificationConstants.CONTENT_SPACE);
      String authorAvatarUrl = notification.getValueOwnerParameter(NotificationConstants.AUTHOR_AVATAR_URL);
      String baseUrl = PropertyManager.getProperty("gatein.email.domain.url");
      String illustrationUrl = baseUrl.concat("/news/images/newsImageDefault.png");
      String activityLink = notification.getValueOwnerParameter(NotificationConstants.ACTIVITY_LINK);
      String context = notification.getValueOwnerParameter(NotificationConstants.CONTEXT);

      HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
      templateContext.put("CONTENT_TITLE", encoder.encode(contentTitle));
      templateContext.put(NotificationConstants.CONTENT_SPACE, encoder.encode(contentSpaceName));
      templateContext.put("CONTENT_AUTHOR", encoder.encode(contentAuthor));
      templateContext.put("CURRENT_USER", currentUser);
      templateContext.put("ILLUSTRATION_URL", encoder.encode(illustrationUrl));
      templateContext.put("AUTHOR_AVATAR_URL", encoder.encode(authorAvatarUrl));
      templateContext.put("CONTEXT", encoder.encode(context));
      templateContext.put("ACTIVITY_LINK", encoder.encode(activityLink));

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
      // Receiver
      Identity receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      // Footer
      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));
      String subject = TemplateUtils.processSubject(templateContext);
      String body = TemplateUtils.processGroovy(templateContext);
      // binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo notificationInfo = notifications.get(0);
      try {
        String spaceId = notificationInfo.getValueOwnerParameter(NotificationConstants.CONTENT_SPACE);
        Space space = Utils.getSpaceService().getSpaceByDisplayName(spaceId);
        if (!Utils.getSpaceService().isMember(space, notificationInfo.getTo())
            || notificationInfo.getTo().equals(notificationInfo.getFrom())) {
          return false;
        }
        String pluginId = notificationInfo.getKey().getId();
        if (pluginId.equals(MentionInNewsNotificationPlugin.ID)) {
          String mentionedIds = notificationInfo.getValueOwnerParameter(NotificationConstants.MENTIONED_IDS);
          String ids = mentionedIds.substring(1, mentionedIds.length() - 1);
          List<String> mentionedList = Stream.of(ids.split(",")).map(String::trim).collect(Collectors.toList());
          if (!mentionedList.contains(notificationInfo.getTo())) {
            return false;
          }
        }
        String language = getLanguage(notificationInfo);
        TemplateContext templateContext = new TemplateContext(pluginId, language);
        //
        Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, notificationInfo.getTo());
        templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
        templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));

        writer.append(buildDigestMsg(notifications, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }

    protected String buildDigestMsg(List<NotificationInfo> notifications, TemplateContext templateContext) {
      Map<String, List<NotificationInfo>> map = new HashMap<>();
      for (NotificationInfo notificationInfo : notifications) {
        String notificationId = notificationInfo.getValueOwnerParameter(NotificationConstants.CONTEXT);
        List<NotificationInfo> tmp = map.get(notificationId);
        if (tmp == null) {
          tmp = new LinkedList<>();
          map.put(notificationId, tmp);
        }
        tmp.add(notificationInfo);
      }

      StringBuilder sb = new StringBuilder();
      for (String notification : map.keySet()) {
        List<NotificationInfo> notificationInfos = map.get(notification);
        NotificationInfo first = notificationInfos.get(0);
        templateContext.put("CONTENT_TITLE", first.getValueOwnerParameter(NotificationConstants.CONTENT_TITLE));
        templateContext.put("USER", first.getValueOwnerParameter(NotificationConstants.CONTENT_AUTHOR));
        templateContext.put("SPACE_TITLE", first.getValueOwnerParameter(NotificationConstants.CONTENT_SPACE));
        templateContext.digestType(DigestTemplate.ElementType.DIGEST_ONE.getValue());

        sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
        String digester = TemplateUtils.processDigest(templateContext);
        sb.append(digester);
        sb.append("</div></li>");
      }
      return sb.toString();
    }

  }

}
