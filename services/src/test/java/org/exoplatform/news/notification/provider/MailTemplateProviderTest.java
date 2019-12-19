package org.exoplatform.news.notification.provider;

import static org.exoplatform.commons.api.notification.channel.template.TemplateProvider.CHANNEL_ID_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.news.notification.plugin.CommentNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.CommentSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.LikeSharedNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.PostNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareMyNewsNotificationPlugin;
import org.exoplatform.news.notification.plugin.ShareNewsNotificationPlugin;
import org.exoplatform.news.notification.provider.MailTemplateProvider.TemplateBuilder;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class MailTemplateProviderTest {

  @Mock
  private InitParams      initParams;

  @Mock
  private IdentityManager identityManager;

  @PrepareForTest({ CommonsUtils.class, PluginKey.class, NotificationPluginUtils.class, TemplateContext.class, PropertyManager.class,
      HTMLEntityEncoder.class, NotificationMessageUtils.class, TimeConvertUtils.class, LinkProviderUtils.class,
      TemplateUtils.class, NotificationContextImpl.class })
  @Test
  public void shoudIntantiateMailTemplate() {
    // Given
    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(any())).thenReturn(null);
    PowerMockito.mockStatic(PluginKey.class);
    PluginKey plugin = mock(PluginKey.class);
    when(PluginKey.key(PostNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(ShareNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(ShareMyNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(LikeNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(LikeSharedNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(CommentNewsNotificationPlugin.ID)).thenReturn(plugin);
    when(PluginKey.key(CommentSharedNewsNotificationPlugin.ID)).thenReturn(plugin);
    ValueParam channelParam = new ValueParam();
    channelParam.setName(CHANNEL_ID_KEY);
    channelParam.setValue("MAIL_CHANNEL");
    when(initParams.getValueParam(eq(CHANNEL_ID_KEY))).thenReturn(channelParam);
    MailTemplateProvider mailTemplate = new MailTemplateProvider(initParams, identityManager);
    TemplateBuilder templateBuilder = (TemplateBuilder) mailTemplate.getTemplateBuilder().get(plugin);
    PowerMockito.mockStatic(NotificationContextImpl.class);
    NotificationContext ctx = mock(NotificationContext.class);
    when(NotificationContextImpl.cloneInstance()).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.CONTENT_TITLE, "title")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.CONTENT_AUTHOR, "jean")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.CURRENT_USER, "root")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.CONTENT_SPACE, "space1")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.ILLUSTRATION_URL,
                    "http://localhost:8080/rest/v1/news/id123/illustration")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.ACTIVITY_LINK,
                    "http://localhost:8080/portal/intranet/activity?id=39")).thenReturn(ctx);
    when(ctx.append(CommentNewsNotificationPlugin.CONTEXT, "COMMENT MY NEWS")).thenReturn(ctx);
    NotificationInfo notification = mock(NotificationInfo.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    when(notification.getKey()).thenReturn(plugin);
    when(plugin.getId()).thenReturn("CommentNewsNotificationPlugin");
    PowerMockito.mockStatic(NotificationPluginUtils.class);
    when(NotificationPluginUtils.getLanguage(anyString())).thenReturn("en");
    TemplateContext templateContext = mock(TemplateContext.class);
    ChannelKey key = mock(ChannelKey.class);
    MailTemplateProvider mailTemplateSpy = Mockito.spy(mailTemplate);
    when(mailTemplateSpy.getChannelKey()).thenReturn(key);
    PowerMockito.mockStatic(TemplateContext.class);
    when(TemplateContext.newChannelInstance(Matchers.any(),
                                            eq("CommentNewsNotificationPlugin"),
                                            eq("en"))).thenReturn(templateContext);
    when(notification.getValueOwnerParameter("CONTENT_AUTHOR")).thenReturn("jean");
    when(notification.getValueOwnerParameter("CURRENT_USER")).thenReturn("root");
    when(notification.getValueOwnerParameter("CONTENT_TITLE")).thenReturn("title");
    when(notification.getValueOwnerParameter("CONTENT_SPACE")).thenReturn("space1");
    PowerMockito.mockStatic(PropertyManager.class);
    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");
    when(notification.getValueOwnerParameter("CONTENT_SPACE")).thenReturn("space1");
    when(notification.getValueOwnerParameter("CONTEXT")).thenReturn("COMMENT MY NEWS");
    when(notification.getValueOwnerParameter("ACTIVITY_LINK")).thenReturn("http://localhost:8080/portal/intranet/activity?id=39");
    HTMLEntityEncoder encoder = mock(HTMLEntityEncoder.class);
    PowerMockito.mockStatic(HTMLEntityEncoder.class);
    when(HTMLEntityEncoder.getInstance()).thenReturn(encoder);
    when(encoder.encode("title")).thenReturn("title");
    when(encoder.encode("space1")).thenReturn("space1");
    when(encoder.encode("jean")).thenReturn("jean");
    when(encoder.encode("root")).thenReturn("root");
    when(encoder.encode("http://localhost:8080/news/images/newsImageDefault.png")).thenReturn("http://localhost:8080/news/images/newsImageDefault.png");
    when(encoder.encode("COMMENT MY NEWS")).thenReturn("COMMENT MY NEWS");
    when(encoder.encode("http://localhost:8080/portal/intranet/activity?id=39")).thenReturn("http://localhost:8080/portal/intranet/activity?id=39");
    when(notification.getValueOwnerParameter("read")).thenReturn("true");
    when(notification.getId()).thenReturn("NotifId123");
    Date date = new Date();
    Long time = date.getTime();
    when(notification.getLastModifiedDate()).thenReturn(time);
    PowerMockito.mockStatic(TimeConvertUtils.class);
    when(TimeConvertUtils.convertXTimeAgoByTimeServer(date,
                                                      "EE, dd yyyy",
                                                      new Locale("en"),
                                                      TimeConvertUtils.YEAR)).thenReturn("9-11-2019");
    when(notification.getTo()).thenReturn("jean");
    Identity receiverIdentity = new Identity(OrganizationIdentityProvider.NAME, "jean");
    receiverIdentity.setRemoteId("jean");
    Profile profile = new Profile(receiverIdentity);
    receiverIdentity.setProfile(profile);
    profile.setProperty(Profile.FIRST_NAME, "jean");
    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME),
                                             eq("jean"),
                                             anyBoolean())).thenReturn(receiverIdentity);
    when(encoder.encode("jean")).thenReturn("jean");
    PowerMockito.mockStatic(LinkProviderUtils.class);
    when(LinkProviderUtils.getRedirectUrl("notification_settings",
                                          "jean")).thenReturn("http://localhost:8080//portal/intranet/allNotifications");
    PowerMockito.mockStatic(TemplateUtils.class);
    when(TemplateUtils.processSubject(templateContext)).thenReturn("root commented your article on space1 space");
    when(TemplateUtils.processGroovy(templateContext)).thenReturn("root commented your article \"title\" in the space1 space");
    when(templateContext.getException()).thenReturn(null);

    // When
    MessageInfo messageInfo = templateBuilder.makeMessage(ctx);

    // Then
    assertNotNull(messageInfo);
    assertEquals("root commented your article \"title\" in the space1 space", messageInfo.getBody());
    assertEquals("root commented your article on space1 space", messageInfo.getSubject());
  }
}
