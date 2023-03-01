package org.exoplatform.news.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockStatic;

import java.io.Serializable;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

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
import org.exoplatform.social.core.space.spi.SpaceService;

@RunWith(MockitoJUnitRunner.class)
public class PublishNewsNotificationPluginTest {

  private static MockedStatic<CommonsUtils>        COMMONS_UTILS;

  private static MockedStatic<WCMCoreUtils>        WCM_CORE_UTILS;

  private static MockedStatic<IdGenerator>         ID_GENERATOR;

  private static MockedStatic<PluginKey>           PLUGIN_KEY;

  private static MockedStatic<ExoContainerContext> EXO_CONTAINER_CONTEXT;

  @Mock
  private InitParams       initParams;
  
  @Mock
  SpaceService                spaceService;

  @BeforeClass
  public static void beforeRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS = mockStatic(CommonsUtils.class);
    WCM_CORE_UTILS = mockStatic(WCMCoreUtils.class);
    ID_GENERATOR = mockStatic(IdGenerator.class);
    PLUGIN_KEY = mockStatic(PluginKey.class);
    EXO_CONTAINER_CONTEXT = mockStatic(ExoContainerContext.class);
  }

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    WCM_CORE_UTILS.close();
    ID_GENERATOR.close();
    PLUGIN_KEY.close();
    EXO_CONTAINER_CONTEXT.close();
  }

  @Test
  public void shouldMakeNotificationForPublishNewsContext() throws Exception {
    // Given
    PublishNewsNotificationPlugin newsPlugin = new PublishNewsNotificationPlugin(initParams, spaceService);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
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
                                                           NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_NEWS)
                                                  .append(PostNewsNotificationPlugin.AUDIENCE,
                                                           "all");

    ID_GENERATOR.when(() -> IdGenerator.generate()).thenReturn("123456");
    mockIdGeneratorService();

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

  public static void mockIdGeneratorService() {
    EXO_CONTAINER_CONTEXT.when(() -> ExoContainerContext.getService(IDGeneratorService.class)).thenReturn(new IDGeneratorService() {
      @Override
      public String generateStringID(Object o) {
        return "123456";
      }

      @Override
      public long generateLongID(Object o) {
        return 123456;
      }

      @Override
      public Serializable generateID(Object o) {
        return 123456;
      }

      @Override
      public int generatIntegerID(Object o) {
        return 123456;
      }
    });
  }

}
