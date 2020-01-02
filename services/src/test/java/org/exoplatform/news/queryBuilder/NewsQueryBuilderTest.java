package org.exoplatform.news.queryBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Test;

public class NewsQueryBuilderTest {

  @Test
  public void shouldCreateQueryWithPinnedStateAndSearchTextAndAuthorAndOneSpaceAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPinnedNews(true);
    filter.setSearchText("text");
    filter.setOrder("jcr:score");
    filter.setAuthor("john");
    List<String> spaces = new ArrayList<>();
    spaces.add("1");
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
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1') AND exo:author = 'john' AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithPinnedStateAndAuthorAndSearchTextAndSpacesListAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPinnedNews(true);
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
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1' OR exo:spaceId = '2' OR exo:spaceId = '3') AND exo:author = 'john' AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndNoArchivedStateAndNoAuthorAndCurrentUserIsPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPinnedNews(false);
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
    assertEquals("SELECT * FROM exo:news WHERE publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndArchivedStateAndNoAuthorAndCurrentUserIsPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPinnedNews(false);
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
    assertEquals("SELECT * FROM exo:news WHERE exo:archived = 'true' AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }

  @Test
  public void shouldCreateQueryWithNoPinnedStateAndArchivedStateAndNoAuthorAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPinnedNews(false);
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
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived = 'true' AND exo:author = 'john') AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
                 query.toString());
  }
}
