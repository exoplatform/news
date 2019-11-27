package org.exoplatform.news.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class PostNewsNotificationPluginTest {

  @Mock
  private UserHandler         userhandler;

  @Mock
  private SpaceService        spaceService;

  @Mock
  private InitParams          initParams;

  @Mock
  private OrganizationService orgService;

  @PrepareForTest({ IdGenerator.class, WCMCoreUtils.class, PluginKey.class })
  @Test
  public void shouldMakeNotificationForPostNewsContext() throws Exception {
    // Given
    when(orgService.getUserHandler()).thenReturn(userhandler);
    PostNewsNotificationPlugin newsPlugin = new PostNewsNotificationPlugin(initParams, spaceService, orgService);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "root")
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, "root")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080//rest/v1/news/id123/illustration")
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=38")
                                                     .append(PostNewsNotificationPlugin.CONTEXT, "POST NEWS");

    User contentAuthorUser = mock(User.class);
    when(userhandler.findUserByName("root")).thenReturn(contentAuthorUser);
    when(contentAuthorUser.getFullName()).thenReturn("root root");
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

    PowerMockito.mockStatic(IdGenerator.class);
    when(IdGenerator.generate()).thenReturn("123456");

    Space space = new Space();
    space.setId("1");
    space.setGroupId("space1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    ListAccess<User> members = mock(ListAccess.class);
    when(userhandler.findUsersByGroupId("space1")).thenReturn(members);
    when(members.getSize()).thenReturn(2);
    when(members.load(0, 2)).thenReturn(receivers);

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

  @PrepareForTest({ IdGenerator.class, WCMCoreUtils.class, PluginKey.class })
  @Test
  public void shouldMakeNotificationForShareNewsContext() throws Exception {
    // Given
    when(orgService.getUserHandler()).thenReturn(userhandler);
    PostNewsNotificationPlugin newsPlugin = new PostNewsNotificationPlugin(initParams, spaceService, orgService);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(PostNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(PostNewsNotificationPlugin.CONTENT_AUTHOR, "test")
                                                     .append(PostNewsNotificationPlugin.CURRENT_USER, "david")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE_ID, "1")
                                                     .append(PostNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(PostNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080//rest/v1/news/id123/illustration")
                                                     .append(PostNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=38")
                                                     .append(PostNewsNotificationPlugin.CONTEXT, "SHARE NEWS");

    User contentAuthorUser = mock(User.class);
    when(userhandler.findUserByName("test")).thenReturn(contentAuthorUser);
    when(contentAuthorUser.getFullName()).thenReturn("test test");
    User currentUser = mock(User.class);
    when(userhandler.findUserByName("david")).thenReturn(currentUser);
    when(currentUser.getFullName()).thenReturn("david david");
    User user1 = mock(User.class);
    when(user1.getUserName()).thenReturn("test");
    User user2 = mock(User.class);
    when(user2.getUserName()).thenReturn("john");

    User[] receivers = new User[2];
    receivers[0] = user1;
    receivers[1] = user2;

    PowerMockito.mockStatic(IdGenerator.class);
    when(IdGenerator.generate()).thenReturn("123456");

    Space space = new Space();
    space.setId("1");
    space.setGroupId("space1");
    when(spaceService.getSpaceById("1")).thenReturn(space);
    ListAccess<User> members = mock(ListAccess.class);
    when(userhandler.findUsersByGroupId("space1")).thenReturn(members);
    when(members.getSize()).thenReturn(2);
    when(members.load(0, 2)).thenReturn(receivers);

    // When
    NotificationInfo notificationInfo = newsPlugin.makeNotification(ctx);

    // Then
    assertEquals("david", notificationInfo.getFrom());
    assertEquals("", notificationInfo.getTitle());
    assertEquals("title", notificationInfo.getValueOwnerParameter("CONTENT_TITLE"));
    assertEquals("test test", notificationInfo.getValueOwnerParameter("CONTENT_AUTHOR"));
    assertEquals("david david", notificationInfo.getValueOwnerParameter("CURRENT_USER"));
    assertEquals("http://localhost:8080//rest/v1/news/id123/illustration",
                 notificationInfo.getValueOwnerParameter("ILLUSTRATION_URL"));
    assertEquals("space1", notificationInfo.getValueOwnerParameter("CONTENT_SPACE"));
    assertEquals("http://localhost:8080/portal/intranet/activity?id=38",
                 notificationInfo.getValueOwnerParameter("ACTIVITY_LINK"));
  }

}
