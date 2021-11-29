package org.exoplatform.news.queryBuilder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
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
@PowerMockIgnore({"com.sun.*", "org.w3c.*", "javax.naming.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PrepareForTest(CommonsUtils.class)
public class NewsQueryBuilderTest {
  @Mock
  SpaceService               spaceService;

  @Test
  public void shouldCreateQueryWithPinnedStateAndSearchTextAndAuthorAndOneSpaceAndCurrentUserIsNoPublisher() throws Exception {
    // Given
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    NewsFilter filter = new NewsFilter();
    filter.setPublishedNews(true);
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
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1') AND exo:author = 'john' AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
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
    assertEquals("SELECT * FROM exo:news WHERE ( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = 'john')) AND CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1' OR exo:spaceId = '2' OR exo:spaceId = '3') AND exo:author = 'john' AND (publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' )) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC",
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

  @PrepareForTest(NewsUtils.class)
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

    PowerMockito.mockStatic(NewsUtils.class);
    when(NewsUtils.getRedactorOrManagerSpaces(currentIdentity.getUserId())).thenReturn(spaces);

    // when
    StringBuilder query = queryBuilder.buildQuery(filter);

    // then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE publication:currentState = 'staged' AND (exo:author = 'john' OR exo:spaceId = '1' OR exo:spaceId = '2' ) AND jcr:path LIKE '/Groups/spaces/%' ORDER BY null DESC",
                 query.toString());
  }
}
