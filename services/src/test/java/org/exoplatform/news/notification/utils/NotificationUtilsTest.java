package org.exoplatform.news.notification.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.Session;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.model.News;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.social.core.space.model.Space;

@RunWith(MockitoJUnitRunner.class)
public class NotificationUtilsTest {

  private static final MockedStatic<CommonsUtils>    COMMONS_UTILS    = mockStatic(CommonsUtils.class);

  private static final MockedStatic<PortalContainer> PORTAL_CONTAINER = mockStatic(PortalContainer.class);

  private static final MockedStatic<PropertyManager> PROPERTY_MANAGER = mockStatic(PropertyManager.class);

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    PORTAL_CONTAINER.close();
    PROPERTY_MANAGER.close();
  }

  @Test
  public void shouldGetTheSpaceUrlWhenTheUserIsNotMember() {
    // Given
    Space space1 = new Space();
    space1.setId("3");
    space1.setDisplayName("space1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    PROPERTY_MANAGER.when(() -> PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");

    // When
    String activityUrl = NotificationUtils.getNotificationActivityLink(space1, "13", false);

    assertEquals("http://localhost:8080/portal/g/:spaces:space1/space1", activityUrl);
  }

  @Test
  public void shouldGetTheSpaceUrlWhenTheUserIsNotMemberAndAfterUpdatingSpaceName() {
    // Given
    Space space = new Space();
    space.setId("4");
    space.setDisplayName("Space1");
    space.setPrettyName(space.getDisplayName());
    space.setGroupId("space1");

    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    PROPERTY_MANAGER.when(() -> PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");

    // When
    String activityUrl = NotificationUtils.getNotificationActivityLink(space, "13", false);

    assertEquals("http://localhost:8080/portal/g/:spaces:space1/space1", activityUrl);

    Space updatedSpace = space;
    updatedSpace.setDisplayName("Space One");
    updatedSpace.setPrettyName(updatedSpace.getDisplayName());

    activityUrl = NotificationUtils.getNotificationActivityLink(updatedSpace, "13", false);
    assertEquals("http://localhost:8080/portal/g/:spaces:space1/space_one", activityUrl);
  }

  @Test
  public void shouldGetTheDefaultIllustrationWhenTheNodeHasNotIllustration() throws Exception {
    // Given
    SessionProviderService sessionProviderService = mock(SessionProviderService.class);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
    RepositoryService repositoryService = mock(RepositoryService.class);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
    Session session = mock(Session.class);
    Node node = mock(Node.class);
    when(session.getNodeByUUID("id123")).thenReturn(node);
    when(node.hasNode("illustration")).thenReturn(false);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentDomain()).thenReturn("http://localhost:8080");

    News news = new News();
    news.setId("id123");

    // When
    String illustrationUrl = NotificationUtils.getNewsIllustration(news, session);

    assertEquals("http://localhost:8080/news/images/news.png", illustrationUrl);
  }

}
