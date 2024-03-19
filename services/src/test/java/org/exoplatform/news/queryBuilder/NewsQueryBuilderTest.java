package org.exoplatform.news.queryBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.model.Space;

@RunWith(MockitoJUnitRunner.class)
public class NewsQueryBuilderTest {

  private static final MockedStatic<NewsUtils> NEWS_UTILS = mockStatic(NewsUtils.class);
  private static final MockedStatic<CommonsUtils> COMMONS_UTILS = mockStatic(CommonsUtils.class);

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    NEWS_UTILS.close();
    COMMONS_UTILS.close();
  }

  @Test
  public void shouldCreateQueryWithPinnedStateAndSearchTextAndAuthorAndOneSpaceAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(true);
    filter.setSearchText("text");
    filter.setOrder("jcr:score");
    filter.setAuthor("john");
    filter.setTagNames(Arrays.asList(new String[]{"text","tex"}));
    List<String> spaces = new ArrayList<>();
    spaces.add("1");
    filter.setSpaces(spaces);
    filter.setDraftNews(true);
    Space space1 = new Space();
    space1.setId("1");
    List<Space> allowedDraftNewsSpaces = new ArrayList<>();
    allowedDraftNewsSpaces.add(space1);
    NEWS_UTILS.when(() -> NewsUtils.getAllowedDraftNewsSpaces(any())).thenReturn(allowedDraftNewsSpaces);
    Identity identity = new Identity(OrganizationIdentityProvider.NAME, "jean");
    identity.setRemoteId("jean");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "redactor");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND (CONTAINS(.,'text~0.6') OR (exo:body LIKE '%text%'))AND exo:pinned = 'true' AND ( exo:spaceId = '1') AND publication:currentState = 'draft' AND (exo:author = 'john' OR (exo:spaceId = '1')) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithPinnedStateAndAuthorAndSearchTextAndSpacesListAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(true);
    filter.setSearchText("text");
    filter.setOrder("jcr:score");
    filter.setAuthor("john");
    List<String> spaces = new ArrayList<>();
    spaces.add("1");
    spaces.add("2");
    spaces.add("3");
    filter.setSpaces(spaces);
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "redactor");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND (CONTAINS(.,'text~0.6') OR (exo:body LIKE '%text%'))AND exo:pinned = 'true' AND ( exo:spaceId = '1' OR exo:spaceId = '2' OR exo:spaceId = '3') AND exo:author = 'john' AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndNoArchivedStateAndNoAuthorAndCurrentUserIsPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(false);
    filter.setArchivedNews(false);
    filter.setOrder("jcr:score");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndArchivedStateAndNoAuthorAndCurrentUserIsPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(false);
    filter.setArchivedNews(true);
    filter.setOrder("jcr:score");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE exo:archived = 'true' AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndArchivedStateAndNoAuthorAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(false);
    filter.setArchivedNews(true);
    filter.setOrder("jcr:score");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "redactor");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived = 'true' AND exo:author = 'john') AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithStagedStateWhenCurrentUserIsAuthor() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setScheduledNews(true);
    filter.setAuthor("john");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
    List<Space> spaces = new ArrayList<>();
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    Space space2 = new Space();
    space2.setId("2");
    space2.setPrettyName("space2");
    spaces.add(space1);
    spaces.add(space2);

    NEWS_UTILS.when(() -> NewsUtils.getAllowedScheduledNewsSpaces(currentIdentity)).thenReturn(spaces);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE publication:currentState = 'staged' AND (exo:author = 'john' OR exo:spaceId = '1' OR exo:spaceId = '2' ) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY null DESC",
                 query.toString());
  }
  @Test
  public void shouldCreateQueryWithNoFilter() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
    filter = null;
    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE ", query.toString());
  }

  @Test
  public void testBuildQueryWithTagNames() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
    NewsFilter filter = new NewsFilter();
    filter.setAuthor("john");
    filter.setTagNames(Arrays.asList(new String[]{"text"}));

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND  exo:body LIKE '%#text%' AND exo:author = 'john' AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY null DESC",
            query.toString());
  }

  @Test
  public void shouldAddFuzzySyntaxWhitQuotedWord() throws Exception {
    // Given
    String text = "\"quoted\"";
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();

    // When
    String result = queryBuilder.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals("\"quoted\"~0.6", result);
  }

  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsMultiWords() throws Exception {
    // Given
    String text = "search text";
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();

    // When
    String result = queryBuilder.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals("search~0.6 OR text~0.6", result);
  }

  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsQuotedMultiWords() throws Exception {
    // Given
    String text = "\"search text\"";
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();

    // When
    String result = queryBuilder.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals("\"search text\"~0.6", result);
  }
  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsMultiWordsAndQuotedOne() throws Exception {
    // Given
    String text = "this is a \"search text\"";
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();

    // When
    String result = queryBuilder.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals("this~0.6 OR is~0.6 OR a~0.6 OR \"search text\"~0.6", result);
  }

  @Test
  public void testAddFuzzySyntaxAndORQuoteInsideQuote() {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    String text = "This \"quoted\" text.";

    // when
    String result = queryBuilder.addFuzzySyntaxAndOR(text);

    // then
    assertEquals("This~0.6 OR \"quoted\"~0.6 OR  text.~0.6", result);
  }
}
