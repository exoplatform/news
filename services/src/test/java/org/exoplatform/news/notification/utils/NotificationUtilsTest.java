package org.exoplatform.news.notification.utils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
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

  @PrepareForTest({ PortalContainer.class, PropertyManager.class})
  @Test
  public void shouldGetTheSpaceUrlWhenTheUserIsNotMember() {
    // Given
    Space space1 = new Space();
    space1.setId("3");
    space1.setDisplayName("space1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");
    PowerMockito.mockStatic(PortalContainer.class);
    when(PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    PowerMockito.mockStatic(PropertyManager.class);
    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080");


    // When
    String activityUrl = NotificationUtils.getNotificationActivityLink(space1, "13", false);

    assertEquals("http://localhost:8080/portal/g/:spaces:space1/space1", activityUrl);
  }

}
