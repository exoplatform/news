package org.exoplatform.news.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.Serializable;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class PublishNewsNotificationPluginTest {

  @Mock
  private InitParams       initParams;

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @PrepareForTest({ IdGenerator.class, WCMCoreUtils.class, PluginKey.class, CommonsUtils.class, ExoContainerContext.class })
  @Test
  public void shouldMakeNotificationForPublishNewsContext() throws Exception {
    // Given
    PublishNewsNotificationPlugin newsPlugin = new PublishNewsNotificationPlugin(initParams);

    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    when(CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
    NotificationContext ctx =
                            NotificationContextImpl.cloneInstance()
                                                   .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                   .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "root")
                                                   .append(PostNewsNotificationPlugin.CURRENT_USER, "root")
                                                   .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                   .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                   .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                           "http://localhost:8080/rest/v1/news/id123/illustration")
                                                   .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL,
                                                           "http://localhost:8080/portal/rest/v1/social/users/default-image/avatar")
                                                   .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                           "http://localhost:8080/portal/intranet/activity?id=38")
                                                   .append(PostNewsNotificationPlugin.CONTEXT,
                                                           NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS);

    PowerMockito.mockStatic(IdGenerator.class);
    when(IdGenerator.generate()).thenReturn("123456");
    PostNewsNotificationPluginTest.mockIdGeneratorService();

    // When
    NotificationInfo notificationInfo = newsPlugin.makeNotification(ctx);

    // Then
    assertEquals("root", notificationInfo.getFrom());
    assertEquals("", notificationInfo.getTitle());
    assertEquals("title", notificationInfo.getValueOwnerParameter("CONTENT_TITLE"));
    assertEquals("http://localhost:8080/rest/v1/news/id123/illustration",
                 notificationInfo.getValueOwnerParameter("ILLUSTRATION_URL"));
    assertEquals("space1", notificationInfo.getValueOwnerParameter("CONTENT_SPACE"));
    assertEquals("http://localhost:8080/portal/intranet/activity?id=38",
                 notificationInfo.getValueOwnerParameter("ACTIVITY_LINK"));
  }
}
