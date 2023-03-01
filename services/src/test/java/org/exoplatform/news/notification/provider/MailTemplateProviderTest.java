package org.exoplatform.news.notification.provider;

import static org.exoplatform.commons.api.notification.channel.template.TemplateProvider.CHANNEL_ID_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
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
import org.exoplatform.news.notification.plugin.PostNewsNotificationPlugin;
import org.exoplatform.news.notification.provider.MailTemplateProvider.TemplateBuilder;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;

@RunWith(MockitoJUnitRunner.class)
public class MailTemplateProviderTest {

  private static final MockedStatic<CommonsUtils>             COMMONS_UTILS              = mockStatic(CommonsUtils.class);

  private static final MockedStatic<PluginKey>                PLUGIN_KEY           = mockStatic(PluginKey.class);

  private static final MockedStatic<NotificationPluginUtils>  NOTIFICATION_PLUGIN_UTILS    =
                                                                                      mockStatic(NotificationPluginUtils.class);

  private static final MockedStatic<TemplateContext>          TEMPLATE_CONTEXT            = mockStatic(TemplateContext.class);

  private static final MockedStatic<PropertyManager>          PROPERTY_MANAGER           = mockStatic(PropertyManager.class);

  private static final MockedStatic<HTMLEntityEncoder>        HTML_ENTITY_ENCODER        = mockStatic(HTMLEntityEncoder.class);

  private static final MockedStatic<NotificationMessageUtils> NOTIFICATION_MESSAGE_UTILS =
                                                                                         mockStatic(NotificationMessageUtils.class);

  private static final MockedStatic<TimeConvertUtils>         TIME_CONVERT_UTILS         = mockStatic(TimeConvertUtils.class);

  private static final MockedStatic<TemplateUtils>            TEMPLATE_UTILS             = mockStatic(TemplateUtils.class);

  private static final MockedStatic<NotificationContextImpl>  NOTIFICATION_CONTEXT_IMPL  =
                                                                                        mockStatic(NotificationContextImpl.class);
  private static final MockedStatic<LinkProviderUtils>  LINK_PROVIDER_UTILS  =
      mockStatic(LinkProviderUtils.class);

  @Mock
  private InitParams                                          initParams;

  @Mock
  private IdentityManager                                     identityManager;

  @Mock
  private SpaceService                                        spaceService;

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    PLUGIN_KEY.close();
    NOTIFICATION_PLUGIN_UTILS.close();
    TEMPLATE_CONTEXT.close();
    PROPERTY_MANAGER.close();
    HTML_ENTITY_ENCODER.close();
    NOTIFICATION_MESSAGE_UTILS.close();
    TIME_CONVERT_UTILS.close();
    TEMPLATE_UTILS.close();
    LINK_PROVIDER_UTILS.close();
    NOTIFICATION_CONTEXT_IMPL.close();
  }

  @Test
  public void shouldInstantiateMailTemplate() {
    // Given
    COMMONS_UTILS.when(() -> CommonsUtils.getService(any())).thenReturn(null);
    PluginKey plugin = mock(PluginKey.class);
    PLUGIN_KEY.when(() -> PluginKey.key(PostNewsNotificationPlugin.ID)).thenReturn(plugin);
    ValueParam channelParam = new ValueParam();
    channelParam.setName(CHANNEL_ID_KEY);
    channelParam.setValue("MAIL_CHANNEL");
    when(initParams.getValueParam(CHANNEL_ID_KEY)).thenReturn(channelParam);
    MailTemplateProvider mailTemplate = new MailTemplateProvider(initParams, identityManager, spaceService);
    TemplateBuilder templateBuilder = (TemplateBuilder) mailTemplate.getTemplateBuilder().get(plugin);
    NotificationContext ctx = mock(NotificationContext.class);
    NOTIFICATION_CONTEXT_IMPL.when(() -> NotificationContextImpl.cloneInstance()).thenReturn(ctx);
    NotificationInfo notification = mock(NotificationInfo.class);
    when(ctx.getNotificationInfo()).thenReturn(notification);
    when(notification.getKey()).thenReturn(plugin);
    when(plugin.getId()).thenReturn("PostNewsNotificationPlugin");
    NOTIFICATION_PLUGIN_UTILS.when(() -> NotificationPluginUtils.getLanguage(anyString())).thenReturn("en");
    TemplateContext templateContext = mock(TemplateContext.class);
    TEMPLATE_CONTEXT.when(() -> TemplateContext.newChannelInstance(any(),
                                                                   eq("PostNewsNotificationPlugin"),
                                                                   eq("en")))
                    .thenReturn(templateContext);
    when(notification.getValueOwnerParameter("CONTENT_AUTHOR")).thenReturn("jean");
    when(notification.getValueOwnerParameter("CURRENT_USER")).thenReturn("root");
    when(notification.getValueOwnerParameter("CONTENT_TITLE")).thenReturn("title");
    when(notification.getValueOwnerParameter("CONTENT_SPACE")).thenReturn("space1");
    PROPERTY_MANAGER.when(() -> PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");
    when(notification.getValueOwnerParameter("CONTENT_SPACE")).thenReturn("space1");
    when(notification.getValueOwnerParameter("CONTEXT")).thenReturn("COMMENT MY NEWS");
    when(notification.getValueOwnerParameter("ACTIVITY_LINK")).thenReturn("http://localhost:8080/portal/intranet/activity?id=39");
    HTMLEntityEncoder encoder = mock(HTMLEntityEncoder.class);
    HTML_ENTITY_ENCODER.when(() -> HTMLEntityEncoder.getInstance()).thenReturn(encoder);
    when(encoder.encode("title")).thenReturn("title");
    when(encoder.encode("space1")).thenReturn("space1");
    when(encoder.encode("jean")).thenReturn("jean");
    when(encoder.encode("http://localhost:8080/news/images/newsImageDefault.png")).thenReturn("http://localhost:8080/news/images/newsImageDefault.png");
    when(encoder.encode("COMMENT MY NEWS")).thenReturn("COMMENT MY NEWS");
    when(encoder.encode("http://localhost:8080/portal/intranet/activity?id=39")).thenReturn("http://localhost:8080/portal/intranet/activity?id=39");
    when(notification.getValueOwnerParameter("read")).thenReturn("true");
    when(notification.getId()).thenReturn("NotifId123");
    Date date = new Date();
    Long time = date.getTime();
    when(notification.getLastModifiedDate()).thenReturn(time);
    TIME_CONVERT_UTILS.when(() -> TimeConvertUtils.convertXTimeAgoByTimeServer(date,
                                                                               "EE, dd yyyy",
                                                                               new Locale("en"),
                                                                               TimeConvertUtils.YEAR))
                      .thenReturn("9-11-2019");
    when(notification.getTo()).thenReturn("jean");
    Identity receiverIdentity = new Identity(OrganizationIdentityProvider.NAME, "jean");
    receiverIdentity.setRemoteId("jean");
    Profile profile = new Profile(receiverIdentity);
    receiverIdentity.setProfile(profile);
    profile.setProperty(Profile.FIRST_NAME, "jean");
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jean")).thenReturn(receiverIdentity);
    when(encoder.encode("jean")).thenReturn("jean");
    LINK_PROVIDER_UTILS.when(() -> LinkProviderUtils.getRedirectUrl("notification_settings",
                                                                    "jean"))
                       .thenReturn("http://localhost:8080//portal/intranet/allNotifications");
    TEMPLATE_UTILS.when(() -> TemplateUtils.processSubject(templateContext))
                  .thenReturn("Root Root has posted an article on space1 space");
    TEMPLATE_UTILS.when(() -> TemplateUtils.processGroovy(templateContext))
                  .thenReturn("Root Root has posted an article \"title\" in the space1 space");
    when(templateContext.getException()).thenReturn(null);

    // When
    MessageInfo messageInfo = templateBuilder.makeMessage(ctx);

    // Then
    assertNotNull(messageInfo);
    assertEquals("Root Root has posted an article \"title\" in the space1 space", messageInfo.getBody());
    assertEquals("Root Root has posted an article on space1 space", messageInfo.getSubject());
  }
}
