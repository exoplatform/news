package org.exoplatform.news.search;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.IdentityManager;

@RunWith(MockitoJUnitRunner.class)
public class NewsIndexingServiceConnectorTest {

  NewsIndexingServiceConnector newsIndexingServiceConnector = null;

  @Mock
  NewsService                  newsService;

  @Mock
  IdentityManager              identityManager;

  @Test
  public void testGetAllIds() {
    newsIndexingServiceConnector = new NewsIndexingServiceConnector(identityManager, getParams(), newsService);
    try {
      newsIndexingServiceConnector.getAllIds(0, 10);
      fail("getAllIds shouldn't be supported");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testCanReindex() {
    newsIndexingServiceConnector = new NewsIndexingServiceConnector(identityManager, getParams(), newsService);
    assertFalse(newsIndexingServiceConnector.canReindex());
  }

  @Test
  public void testGetType() {
    newsIndexingServiceConnector = new NewsIndexingServiceConnector(identityManager, getParams(), newsService);
    assertEquals(NewsIndexingServiceConnector.TYPE, newsIndexingServiceConnector.getType());
  }

  @Test
  public void testCreate() {
    newsIndexingServiceConnector = new NewsIndexingServiceConnector(identityManager, getParams(), newsService);

    try {
      newsIndexingServiceConnector.create(null);
      fail("IllegalArgumentException should be thrown");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    try {
      newsIndexingServiceConnector.create("1");
      fail("IllegalStateException should be thrown");
    } catch (IllegalStateException e) {
      // Expected
    }

    News news = new News();
    news.setId("1");
    news.setTitle("news1");
    news.setAuthor("root");
    news.setCreationDate(new Date());
    news.setUpdateDate(new Date());
    news.setActivities(":1:3");

    Identity posterIdentity = new Identity("posterId");
    Profile posterProfile = new Profile(posterIdentity);
    posterProfile.setProperty("fullName", "Root Root");
    posterIdentity.setProfile(posterProfile);
    try {
      when(newsService.getNewsById("1")).thenReturn(news);
    } catch (Exception e) {
      e.printStackTrace();
    }
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(posterIdentity);

    Document document = newsIndexingServiceConnector.create("1");
    assertNotNull(document);
    assertEquals("1", document.getId());
    assertEquals(NewsIndexingServiceConnector.TYPE, document.getType());
    assertEquals("root", document.getFields().get("posterId"));
    assertEquals("Root Root", document.getFields().get("posterName"));
    assertNotNull(document.getLastUpdatedDate());
    assertNotNull(document.getPermissions());
    assertEquals(1, document.getPermissions().size());
  }

  private InitParams getParams() {
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    params.addParameter(propertiesParam);
    return params;
  }

}
