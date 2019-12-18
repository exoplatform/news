package org.exoplatform.news.queryBuilder;

import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.List;

public class NewsQueryBuilder {
  private static final Log LOG = ExoLogger.getLogger(NewsQueryBuilder.class);

  /**
   * builds query for news actions
   * @param filter news filter to customize the query
   * @return built query
   */
  public StringBuilder buildQuery(NewsFilter filter) throws Exception{

      StringBuilder sqlQuery = new StringBuilder("SELECT * FROM exo:news WHERE ");
      try {
        if (filter != null) {
          if (filter.getSearchText() != null && !filter.getSearchText().equals("")) {
            sqlQuery.append("CONTAINS(.,'").append(filter.getSearchText()).append("') AND ");
          }
          if (filter.isPinnedNews()) {
            sqlQuery.append("exo:pinned = 'true' AND ");
          }
          List<String> spaces = filter.getSpaces();
          if (spaces != null && spaces.size() != 0){
            sqlQuery.append("( ");
            for(int i =0 ; i< spaces.size()-1; i++){
              sqlQuery.append("exo:spaceId = '").append(spaces.get(i)).append("' OR ");
            }
            sqlQuery.append("exo:spaceId = '").append(spaces.get(spaces.size()-1)).append("') AND ");
          }
          sqlQuery.append("publication:currentState = 'published' AND jcr:path LIKE '/Groups/spaces/%' ORDER BY ").append(filter.getOrder()).append(" DESC");
        } else {
          throw new Exception("Unable to build query, filter is null");
        }
      } catch (Exception e) {
        LOG.error("Error while creating query", e);
      }
    return sqlQuery;
    }
}
