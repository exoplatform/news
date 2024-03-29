package org.exoplatform.news.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.idgenerator.IDGeneratorService;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@RunWith(MockitoJUnitRunner.class)
public class PostNewsNotificationPluginTest {

  private static MockedStatic<CommonsUtils>        COMMONS_UTILS;

  private static MockedStatic<WCMCoreUtils>        WCM_CORE_UTILS;

  private static MockedStatic<IdGenerator>         ID_GENERATOR;

  private static MockedStatic<PluginKey>           PLUGIN_KEY;

  private static MockedStatic<ExoContainerContext> EXO_CONTAINER_CONTEXT;

  private static MockedStatic<NewsUtils>           NEWS_UTILS;

  @Mock
  private UserHandler         userhandler;

  @Mock
  private SpaceService spaceService;
  @Mock
  private NewsService     newsService;
  @Mock
  private ActivityManager activityManager;


  @Mock
  private InitParams          initParams;

  @Mock
  private OrganizationService orgService;

  @BeforeClass
  public static void beforeRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS = mockStatic(CommonsUtils.class);
    WCM_CORE_UTILS = mockStatic(WCMCoreUtils.class);
    ID_GENERATOR = mockStatic(IdGenerator.class);
    PLUGIN_KEY = mockStatic(PluginKey.class);
    EXO_CONTAINER_CONTEXT = mockStatic(ExoContainerContext.class);
    NEWS_UTILS = mockStatic(NewsUtils.class);
  }

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    WCM_CORE_UTILS.close();
    ID_GENERATOR.close();
    PLUGIN_KEY.close();
    EXO_CONTAINER_CONTEXT.close();
    NEWS_UTILS.close();
  }

  @Test
  public void shouldMakeNotificationForPostNewsContext() throws Exception {
    // Given
    when(orgService.getUserHandler()).thenReturn(userhandler);
    PostNewsNotificationPlugin newsPlugin = new PostNewsNotificationPlugin(initParams, spaceService, orgService, newsService, activityManager);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "root")
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, "root")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080//rest/v1/news/id123/illustration")
                                                     .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL,
                                                             "http://localhost:8080/portal/rest/v1/social/users/default-image/avatar")
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=38")
                                                     .append(PostNewsNotificationPlugin.NEWS_ID,
                                                             "456789")
                                                     .append(PostNewsNotificationPlugin.CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);

    User contentAuthorUser = mock(User.class);
    when(userhandler.findUserByName("root")).thenReturn(contentAuthorUser);
    User currentUser = mock(User.class);
    when(userhandler.findUserByName("root")).thenReturn(currentUser);
    when(currentUser.getFullName()).thenReturn("root root");
    User user1 = mock(User.class);
    when(user1.getUserName()).thenReturn("test");
    User user2 = mock(User.class);
    when(user2.getUserName()).thenReturn("john");

    User[] receivers = new User[2];
    receivers[0] = user1;
    receivers[1] = user2;

    ID_GENERATOR.when(() -> IdGenerator.generate()).thenReturn("123456");
    mockIdGeneratorService();
    COMMONS_UTILS.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(orgService);
    NEWS_UTILS.when(()-> NewsUtils.getUserIdentity(anyString())).thenReturn(new Identity("root"));
    Space space = new Space();
    space.setId("1");
    space.setGroupId("space1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    ListAccess<User> members = mock(ListAccess.class);
    when(userhandler.findUsersByGroupId("space1")).thenReturn(members);
    when(members.getSize()).thenReturn(2);
    when(members.load(0, 2)).thenReturn(receivers);

    News news = mock(News.class);
    when(news.getActivityId()).thenReturn("12345");
    when(newsService.getNewsById(anyString(), any(Identity.class), Mockito.anyBoolean(), anyString())).thenReturn(news);

    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activity.isHidden()).thenReturn(false);
    when(activityManager.getActivity("12345")).thenReturn(activity);

    // When
    NotificationInfo notificationInfo = newsPlugin.makeNotification(ctx);

    // Then
    assertEquals("root", notificationInfo.getFrom());
    assertEquals("", notificationInfo.getTitle());
    assertEquals("title", notificationInfo.getValueOwnerParameter("CONTENT_TITLE"));
    assertEquals("root root", notificationInfo.getValueOwnerParameter("CONTENT_AUTHOR"));
    assertEquals("http://localhost:8080//rest/v1/news/id123/illustration",
                 notificationInfo.getValueOwnerParameter("ILLUSTRATION_URL"));
    assertEquals("space1", notificationInfo.getValueOwnerParameter("CONTENT_SPACE"));
    assertEquals("http://localhost:8080/portal/intranet/activity?id=38",
                 notificationInfo.getValueOwnerParameter("ACTIVITY_LINK"));
  }

  @Test
  public void shouldMakeNotificationForPostNewsContextAndDoNotSendNotificationToCreator() throws Exception {
    // Given
    when(orgService.getUserHandler()).thenReturn(userhandler);
    PostNewsNotificationPlugin newsPlugin = new PostNewsNotificationPlugin(initParams, spaceService, orgService, newsService, activityManager);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "root")
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, "root")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080//rest/v1/news/id123/illustration")
                                                     .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL,
                                                             "http://localhost:8080/portal/rest/v1/social/users/default-image/avatar")
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=38")
                                                     .append(PostNewsNotificationPlugin.NEWS_ID,
                                                             "456789")
                                                     .append(PostNewsNotificationPlugin.CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);

    User contentAuthorUser = mock(User.class);
    when(userhandler.findUserByName("root")).thenReturn(contentAuthorUser);
    when(contentAuthorUser.getFullName()).thenReturn("root root");
    User user1 = mock(User.class);
    when(user1.getUserName()).thenReturn("root");
    User user2 = mock(User.class);
    when(user2.getUserName()).thenReturn("john");

    User[] receivers = new User[2];
    receivers[0] = user1;
    receivers[1] = user2;

    ID_GENERATOR.when(() -> IdGenerator.generate()).thenReturn("123456");
    mockIdGeneratorService();
    COMMONS_UTILS.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(orgService);
    Space space = new Space();
    space.setId("1");
    space.setGroupId("space1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    ListAccess<User> members = mock(ListAccess.class);
    when(userhandler.findUsersByGroupId("space1")).thenReturn(members);
    when(members.getSize()).thenReturn(2);
    when(members.load(0, 2)).thenReturn(receivers);

    News news = mock(News.class);
    when(news.getActivityId()).thenReturn("12345");
    NEWS_UTILS.when(()-> NewsUtils.getUserIdentity(anyString())).thenReturn(new Identity("root"));
    when(newsService.getNewsById(anyString(), any(Identity.class), Mockito.anyBoolean(), anyString())).thenReturn(news);

    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activity.isHidden()).thenReturn(false);
    when(activityManager.getActivity("12345")).thenReturn(activity);

    // When
    NotificationInfo notificationInfo = newsPlugin.makeNotification(ctx);

    // Then
    assertEquals("root", notificationInfo.getFrom());
    assertEquals(1, notificationInfo.getSendToUserIds().size());
    assertEquals("john", notificationInfo.getSendToUserIds().get(0));
    assertEquals("", notificationInfo.getTitle());
    assertEquals("title", notificationInfo.getValueOwnerParameter("CONTENT_TITLE"));
    assertEquals("root root", notificationInfo.getValueOwnerParameter("CONTENT_AUTHOR"));
    assertEquals("http://localhost:8080//rest/v1/news/id123/illustration",
                 notificationInfo.getValueOwnerParameter("ILLUSTRATION_URL"));
    assertEquals("space1", notificationInfo.getValueOwnerParameter("CONTENT_SPACE"));
    assertEquals("http://localhost:8080/portal/intranet/activity?id=38",
                 notificationInfo.getValueOwnerParameter("ACTIVITY_LINK"));
  }

  @Test
  public void shouldMakeNotificationForPostNewsContextAndAuthorUserIsNull() throws Exception {
    // Given
    when(orgService.getUserHandler()).thenReturn(userhandler);
    PostNewsNotificationPlugin newsPlugin = new PostNewsNotificationPlugin(initParams, spaceService, orgService, newsService, activityManager);

    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "jean")
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, "jean")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080//rest/v1/news/id123/illustration")
                                                     .append(PostNewsNotificationPlugin.AUTHOR_AVATAR_URL,
                                                             "http://localhost:8080/portal/rest/v1/social/users/default-image/avatar")
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=38")
                                                     .append(PostNewsNotificationPlugin.NEWS_ID,
                                                             "456789")
                                                     .append(PostNewsNotificationPlugin.CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS);

    when(userhandler.findUserByName("jean")).thenReturn(null);

    User user1 = mock(User.class);
    when(user1.getUserName()).thenReturn("test");
    User user2 = mock(User.class);
    when(user2.getUserName()).thenReturn("john");

    User[] receivers = new User[2];
    receivers[0] = user1;
    receivers[1] = user2;

    ID_GENERATOR.when(() -> IdGenerator.generate()).thenReturn("123456");
    mockIdGeneratorService();
    COMMONS_UTILS.when(() -> CommonsUtils.getService(OrganizationService.class)).thenReturn(orgService);
    Space space = new Space();
    space.setId("1");
    space.setGroupId("space1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    ListAccess<User> members = mock(ListAccess.class);
    when(userhandler.findUsersByGroupId("space1")).thenReturn(members);
    when(members.getSize()).thenReturn(2);
    when(members.load(0, 2)).thenReturn(receivers);


    News news = mock(News.class);
    when(news.getActivityId()).thenReturn("12345");
    NEWS_UTILS.when(()-> NewsUtils.getUserIdentity(anyString())).thenReturn(new Identity("root"));
    when(newsService.getNewsById(anyString(), any(Identity.class), Mockito.anyBoolean(), anyString())).thenReturn(news);

    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(activity.isHidden()).thenReturn(false);
    when(activityManager.getActivity("12345")).thenReturn(activity);
    // When
    NotificationInfo notificationInfo = newsPlugin.makeNotification(ctx);

    // Then
    assertEquals("jean", notificationInfo.getFrom());
    assertEquals("", notificationInfo.getTitle());
    assertEquals("title", notificationInfo.getValueOwnerParameter("CONTENT_TITLE"));
    assertEquals("jean", notificationInfo.getValueOwnerParameter("CONTENT_AUTHOR"));
    assertEquals("http://localhost:8080//rest/v1/news/id123/illustration",
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
