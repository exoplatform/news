package org.exoplatform.news.notification.plugin;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.NotificationCompletionService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
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
public class LikeNewsNotificationPluginTest {

  @Mock
  private SpaceService           spaceService;

  @Mock
  private InitParams             initParams;

  @Mock
  private SessionProviderService sessionProviderService;

  @Mock
  private IdentityManager        identityManager;

  @Mock
  private NewsService            newsService;

  @PrepareForTest({ IdGenerator.class, WCMCoreUtils.class, PluginKey.class, CommonsUtils.class, SessionProvider.class,
      LinkProvider.class, PropertyManager.class })
  @Test
  public void shouldMakeNotificationForLikeNewsContext() throws Exception {
    // Given
    LikeNewsNotificationPlugin linkPlugin = new LikeNewsNotificationPlugin(initParams);

    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(NotificationService.class)).thenReturn(null);
    when(CommonsUtils.getService(NotificationCompletionService.class)).thenReturn(null);
    NotificationContext ctx = NotificationContextImpl.cloneInstance()
                                                     .append(LikeNewsNotificationPlugin.CONTENT_TITLE, "title")
                                                     .append(LikeNewsNotificationPlugin.CONTENT_AUTHOR, "jean")
                                                     .append(LikeNewsNotificationPlugin.CURRENT_USER, "root")
                                                     .append(LikeNewsNotificationPlugin.CONTENT_SPACE, "space1")
                                                     .append(LikeNewsNotificationPlugin.ILLUSTRATION_URL,
                                                             "http://localhost:8080/rest/v1/news/id123/illustration")
                                                     .append(LikeNewsNotificationPlugin.ACTIVITY_LINK,
                                                             "http://localhost:8080/portal/intranet/activity?id=39")
                                                     .append(LikeNewsNotificationPlugin.CONTEXT,
                                                             NotificationConstants.NOTIFICATION_CONTEXT.LIKE_MY_NEWS);

    User currentUser = mock(User.class);
    OrganizationService orgService = mock(OrganizationService.class);
    UserHandler userhandler = mock(UserHandler.class);
    when(CommonsUtils.getService(OrganizationService.class)).thenReturn(orgService);
    when(orgService.getUserHandler()).thenReturn(userhandler);
    when(userhandler.findUserByName("root")).thenReturn(currentUser);
    when(currentUser.getFullName()).thenReturn("root root");
    when(CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
    RepositoryService repositoryService = mock(RepositoryService.class);
    when(CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
    SessionProvider systemProvider = mock(SessionProvider.class);
    PowerMockito.mockStatic(SessionProvider.class);
    when(SessionProvider.createSystemProvider()).thenReturn(systemProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(systemProvider);
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    Session session = mock(Session.class);
    when(systemProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(session.getNodeByUUID("id123")).thenReturn(node);
    when(node.hasNode("illustration")).thenReturn(true);

    PowerMockito.mockStatic(IdGenerator.class);
    when(IdGenerator.generate()).thenReturn("123456");
    PowerMockito.mockStatic(PropertyManager.class);
    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");

    News news = new News();
    news.setTitle("title");
    news.setAuthor("jean");
    news.setId("id123");
    news.setSpaceId("3");
    news.setActivities("3:39;1:11");

    Space space1 = new Space();
    space1.setId("3");
    space1.setDisplayName("space1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");

    when(newsService.getNewsById("id123", false)).thenReturn(news);
    Identity posterActivity = mock(Identity.class);
    when(identityManager.getIdentity("2")).thenReturn(posterActivity);
    Profile profile = mock(Profile.class);
    Profile profile1 = new Profile(posterActivity);
    profile1.setProperty("username", "jean");
    when(posterActivity.getProfile()).thenReturn(profile1);

    when(spaceService.getSpaceByPrettyName("space1")).thenReturn(space1);
    when(spaceService.isMember(space1, "jean")).thenReturn(true);
    PowerMockito.mockStatic(LinkProvider.class);
    when(LinkProvider.getSingleActivityUrl("39")).thenReturn("portal/intranet/activity?id=39");
    // When
    NotificationInfo notificationInfo = linkPlugin.makeNotification(ctx);

    // Then
    assertEquals("root", notificationInfo.getFrom());
    assertEquals("", notificationInfo.getTitle());
    assertEquals("LIKE MY NEWS", notificationInfo.getValueOwnerParameter("CONTEXT"));
    assertEquals("http://localhost:8080/rest/v1/news/id123/illustration",
                 notificationInfo.getValueOwnerParameter("ILLUSTRATION_URL"));
    assertEquals("space1", notificationInfo.getValueOwnerParameter("CONTENT_SPACE"));
    assertEquals("http://localhost:8080/portal/intranet/activity?id=39",
                 notificationInfo.getValueOwnerParameter("ACTIVITY_LINK"));
  }

}
