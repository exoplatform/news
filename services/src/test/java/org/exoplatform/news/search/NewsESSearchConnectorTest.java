package org.exoplatform.news.search;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ActivitySearchConnector;
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
      lenient().when(configurationManager.getInputStream("FILE_PATH")).thenReturn(new ByteArrayInputStream(FAKE_ES_QUERY.getBytes()));
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
    NewsFilter filter = new NewsFilter();
    filter.setSearchText("term");
    filter.setLimit(0);
    filter.setOffset(10);
    try {
      newsESSearchConnector.search(null, filter);
      fail("Should throw IllegalArgumentException: viewer identity is mandatory");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    try {
      NewsFilter filter2 = new NewsFilter();
      filter.setSearchText("term");
      filter.setLimit(-1);
      filter.setOffset(10);
      newsESSearchConnector.search(identity, filter2);
      fail("Should throw IllegalArgumentException: limit should be positive");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    try {
      NewsFilter filter3 = new NewsFilter();
      filter.setSearchText("term");
      filter.setLimit(0);
      filter.setOffset(-1);
      newsESSearchConnector.search(identity, filter3);
      fail("Should throw IllegalArgumentException: offset should be positive");
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

    NewsFilter filter = new NewsFilter();
    filter.setSearchText("term");
    filter.setLimit(10);
    filter.setOffset(0);

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    lenient().when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
            ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                    filter.getSearchText())
                    .replace("@term_query@",
                            filter.getSearchText()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    lenient().when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX))).thenReturn("{}");

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, filter);
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

    NewsFilter filter = new NewsFilter();
    filter.setSearchText("term");
    filter.setLimit(10);
    filter.setOffset(0);

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    lenient().when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
            ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                    filter.getSearchText())
                    .replace("@term_query@",
                            filter.getSearchText()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    lenient().when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX))).thenReturn(searchResult);

    Identity rootIdentity = new Identity("organization", "root");
    lenient().when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "posterId")).thenReturn(rootIdentity);

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, filter);
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

    NewsFilter filter = new NewsFilter();
    filter.setSearchText("john");
    filter.setLimit(10);
    filter.setOffset(0);

    HashSet<Long> permissions = new HashSet<>(Arrays.asList(10L, 20L, 30L));
    Identity identity = mock(Identity.class);
    lenient().when(identity.getId()).thenReturn("1");
    lenient().when(activityStorage.getStreamFeedOwnerIds(eq(identity))).thenReturn(permissions);
    String expectedESQuery = FAKE_ES_QUERY.replaceAll("@term_query@",
            ActivitySearchConnector.SEARCH_QUERY_TERM.replace("@term@",
                    filter.getSearchText())
                    .replace("@term_query@",
                            filter.getSearchText()))
                                          .replaceAll("@permissions@", StringUtils.join(permissions, ","))
                                          .replaceAll("@offset@", "0")
                                          .replaceAll("@limit@", "10");
    searchResult = IOUtil.getStreamContentAsString(getClass().getClassLoader()
                                                             .getResourceAsStream("news-search-result-by-identity.json"));
    lenient().when(client.sendRequest(eq(expectedESQuery), eq(ES_INDEX))).thenReturn(searchResult);

    Identity poster = new Identity(OrganizationIdentityProvider.NAME, "posterId");
    lenient().when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "posterId")).thenReturn(poster);

    List<NewsESSearchResult> result = newsESSearchConnector.search(identity, filter);
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

    ValueParam valueParam = new ValueParam();
    valueParam.setName("query.file.path");
    valueParam.setValue("FILE_PATH");

    params.addParameter(propertiesParam);
    params.addParameter(valueParam);
    return params;
  }

}
