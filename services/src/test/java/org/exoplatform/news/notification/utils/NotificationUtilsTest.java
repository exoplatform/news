package org.exoplatform.news.notification.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.model.News;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.social.core.space.model.Space;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class NotificationUtilsTest {

  @PrepareForTest({ ExoContainerContext.class, PortalContainer.class, PropertyManager.class })
  @Test
  public void shouldGetTheSpaceUrlWhenTheUserIsNotMember() {
    // Given
    Space space1 = new Space();
    space1.setId("3");
    space1.setDisplayName("space1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");
    PowerMockito.mockStatic(ExoContainerContext.class);
    PowerMockito.mockStatic(PortalContainer.class);
    when(PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    PowerMockito.mockStatic(PropertyManager.class);
    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");

    // When
    String activityUrl = NotificationUtils.getNotificationActivityLink(space1, "13", false);

    assertEquals("http://localhost:8080/portal/g/:spaces:space1/space1", activityUrl);
  }

  @PrepareForTest({ PropertyManager.class, CommonsUtils.class })
  @Test
  public void shouldGetTheDefaultIllustrationWhenTheNodeHasNotIllustration() throws Exception {
    // Given
    SessionProviderService sessionProviderService = mock(SessionProviderService.class);
    PowerMockito.mockStatic(CommonsUtils.class);
    when(CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
    SessionProvider sessionProvider = mock(SessionProvider.class);
    when(sessionProviderService.getSessionProvider(null)).thenReturn(sessionProvider);
    RepositoryService repositoryService = mock(RepositoryService.class);
    when(CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
    ManageableRepository repository = mock(ManageableRepository.class);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    RepositoryEntry repositoryEntry = mock(RepositoryEntry.class);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    Session session = mock(Session.class);
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Node node = mock(Node.class);
    when(session.getNodeByUUID("id123")).thenReturn(node);
    when(node.hasNode("illustration")).thenReturn(false);
    PowerMockito.mockStatic(PropertyManager.class);
    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");

    News news = new News();
    news.setId("id123");

    // When
    String illustrationUrl = NotificationUtils.getNewsIllustration(news);

    assertEquals("http://localhost:8080/news/images/newsImageDefault.png", illustrationUrl);
  }

}
