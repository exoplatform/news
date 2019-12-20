package org.exoplatform.news.queryBuilder;

import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.social.core.space.model.Space;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class NewsQueryBuilderTest {

  @Test
  public void shouldCreateQueryWithPinnedStateAndSearchTextAndAuthorAndOneSpace() throws Exception {
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

    //when
    StringBuilder query = queryBuilder.buildQuery(filter);

    //then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1') AND exo:author = 'john' AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC", query.toString());
  }

  @Test
  public void shouldCreateQueryWithPinnedStateAndAuthorAndSearchTextAndSpacesList() throws Exception {
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

    //when
    StringBuilder query = queryBuilder.buildQuery(filter);

    //then
    assertNotNull(query);
    assertEquals("SELECT * FROM exo:news WHERE CONTAINS(.,'text') AND exo:pinned = 'true' AND ( exo:spaceId = '1' OR exo:spaceId = '2' OR exo:spaceId = '3') AND exo:author = 'john' AND publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY jcr:score DESC", query.toString());
  }
}
