package org.exoplatform.news.search;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.ActivityStorage;

@RunWith(MockitoJUnitRunner.class)
public class NewsESSearchConnectorTest {

  private static final String ES_TYPE         = "news";

  private static final String ES_INDEX        = "news_alias";

  public static final String  FAKE_ES_QUERY   =
                                            "{offset: @offset@, limit: @limit@, term1: @term@, term2: @term@, permissions: @permissions@}";

  @Mock
  IdentityManager             identityManager;

  @Mock
  ActivityStorage             activityStorage;

  @Mock
  ConfigurationManager        configurationManager;

  @Mock
  ElasticSearchingClient      client;

  String                      searchResult    = null;

  boolean                     developingValue = false;

  @Before
  public void setUp() throws Exception {// NOSONAR
    searchResult = IOUtil.getStreamContentAsString(getClass().getClassLoader().getResourceAsStream("news-search-result.json"));

    try {
      Mockito.reset(configurationManager);
      when(configurationManager.getInputStream("FILE_PATH")).thenReturn(new ByteArrayInputStream(FAKE_ES_QUERY.getBytes()));
    } catch (Exception e) {
      throw new IllegalStateException("Error retrieving ES Query content", e);
    }
    developingValue = PropertyManager.isDevelopping();
    PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
    PropertyManager.refresh();
  }

  @After
  public void tearDown() {
    PropertyManager.setProperty(PropertyManager.DEVELOPING, String.valueOf(developingValue));
    PropertyManager.refresh();
  }

  @Test
  public void testSearchArguments() {
    NewsESSearchConnector newsESSearchConnector = new NewsESSearchConnector(configurationManager,
                                                                            identityManager,
                                                                            activityStorage,
                                                                            client,
                                                                            getParams());
    try {
      newsESSearchConnector.search(null, "term", 0, 10);
      fail("Should throw IllegalArgumentException: viewer identity is mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    Identity identity = mock(Identity.class);
    when(identity.getId()).thenReturn("1");
    try {
      newsESSearchConnector.search(identity, null, 0, 10);
      fail("Should throw IllegalArgumentException: filter is mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      newsESSearchConnector.search(identity, "term", -1, 10);
      fail("Should throw IllegalArgumentException: offset should be positive");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      newsESSearchConnector.search(identity, "term", 0, -1);
      fail("Should throw IllegalArgumentException: limit should be positive");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  @Test
  public void testSearchNoResult() {
    NewsESSearchConnector newsESSearchConnector = new NewsESSearchConnector(configurationManager,
                                                                            identityManager,
                                                                            activityStorage,
                                                                            client,
                                                                            getParams());

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    when(identity.getId()).thenReturn("1");
    when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term@", "term")
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX), eq(ES_TYPE))).thenReturn("{}");

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, "term", 0, 10);
    assertNotNull(result);
    assertEquals(0, result.size());
  }

  @Test
  public void testSearchWithResult() {
    NewsESSearchConnector newsESSearchConnector = new NewsESSearchConnector(configurationManager,
                                                                            identityManager,
                                                                            activityStorage,
                                                                            client,
                                                                            getParams());

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    when(identity.getId()).thenReturn("1");
    when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term@", "term")
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX), eq(ES_TYPE))).thenReturn(searchResult);

    Identity rootIdentity = new Identity("organization", "root");
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "posterId")).thenReturn(rootIdentity);

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, "term", 0, 10);
    assertNotNull(result);
    assertEquals(2, result.size());

    NewsESSearchResult newsESSearchResult = result.iterator().next();
    assertEquals("6", newsESSearchResult.getId());
    assertEquals(1592227545758L, newsESSearchResult.getPostedTime());
    assertEquals(1592227545758L, newsESSearchResult.getLastUpdatedTime());
    assertNotNull(newsESSearchResult.getExcerpts());
  }

  @Test
  public void testSearchWithIdentityResult() throws IOException {// NOSONAR
    NewsESSearchConnector newsESSearchConnector = new NewsESSearchConnector(configurationManager,
                                                                            identityManager,
                                                                            activityStorage,
                                                                            client,
                                                                            getParams());

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    when(identity.getId()).thenReturn("1");
    when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term@", "john")
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    searchResult = IOUtil.getStreamContentAsString(getClass().getClassLoader()
                                                             .getResourceAsStream("news-search-result-by-identity.json"));
    when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX), eq(ES_TYPE))).thenReturn(searchResult);

    Identity poster = new Identity(OrganizationIdentityProvider.NAME, "posterId");
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "posterId")).thenReturn(poster);

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, "john", 0, 10);
    assertNotNull(result);
    assertEquals(1, result.size());

    NewsESSearchResult newsESSearchResult = result.iterator().next();
    assertEquals("6", newsESSearchResult.getId());
    assertEquals(1592227545758L, newsESSearchResult.getPostedTime());
    assertEquals(1592227545758L, newsESSearchResult.getLastUpdatedTime());
    assertNotNull(newsESSearchResult.getExcerpts());
    assertEquals(0, newsESSearchResult.getExcerpts().size());
  }

  private InitParams getParams() {
    InitParams params = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    propertiesParam.setName("constructor.params");
    propertiesParam.setProperty("index", ES_INDEX);
    propertiesParam.setProperty("searchType", ES_TYPE);

    ValueParam valueParam = new ValueParam();
    valueParam.setName("query.file.path");
    valueParam.setValue("FILE_PATH");

    params.addParameter(propertiesParam);
    params.addParameter(valueParam);
    return params;
  }

}
