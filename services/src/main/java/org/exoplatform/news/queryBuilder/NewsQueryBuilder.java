package org.exoplatform.news.queryBuilder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsUtils;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;

public class NewsQueryBuilder {
  private static final Log    LOG                             = ExoLogger.getLogger(NewsQueryBuilder.class);

  private static final String PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  /**
   * builds query for news actions
   * 
   * @param filter news filter to customize the query
   * @return built query
   * @throws Exception for error when creating query
   */
  public StringBuilder buildQuery(NewsFilter filter) throws Exception {
    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM exo:news WHERE ");
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    boolean isPublisher = currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
    String username = currentIdentity.getUserId();
    try {
      if (filter != null) {
        if (filter.isArchivedNews()) {
          if (isPublisher) {
            sqlQuery.append("exo:archived = 'true' AND ");
          } else {
            sqlQuery.append("( exo:archived = 'true' AND exo:author = '").append(username).append("') AND ");
          }
        } else {
          if (!isPublisher) {
            sqlQuery.append("( exo:archived IS NULL OR exo:archived = 'false' OR ( exo:archived = 'true' AND  exo:author = '")
                    .append(username)
                    .append("')) AND ");
          }
        }

        if (filter.getSearchText() != null && !filter.getSearchText().equals("")) {
          sqlQuery.append("CONTAINS(.,'").append(filter.getSearchText()).append("') AND ");
        }
        if (filter.isPinnedNews()) {
          sqlQuery.append("exo:pinned = 'true' AND ");
        }

        List<String> spaces = filter.getSpaces();
        if (spaces != null && spaces.size() != 0) {
          sqlQuery.append("( ");
          for (int i = 0; i < spaces.size() - 1; i++) {
            sqlQuery.append("exo:spaceId = '").append(spaces.get(i)).append("' OR ");
          }
          sqlQuery.append("exo:spaceId = '").append(spaces.get(spaces.size() - 1)).append("') AND ");
        }
        List<Space> redactorOrManagersSpaces = NewsUtils.getRedactorOrManagerSpaces(currentIdentity.getUserId());
        if (filter.isDraftNews()) {
          IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
          String currentIdentityId = identity.getId();
          sqlQuery.append("publication:currentState = 'draft'");
          sqlQuery.append(" AND (('");
          sqlQuery.append(currentIdentityId).append("' IN exo:newsModifiersIds AND exo:activities <> '')");
          sqlQuery.append(" OR ");
          sqlQuery.append("( exo:author = '").append(filter.getAuthor()).append("' AND exo:activities = '')");
          int redactorOrManagersSpacesSize = redactorOrManagersSpaces.size();
          if(redactorOrManagersSpacesSize > 0) {
            sqlQuery.append("OR (");
            for (int i= 0; i < redactorOrManagersSpacesSize ; i++) {
              sqlQuery.append("exo:spaceId = '").append(redactorOrManagersSpaces.get(i).getId()).append("'");
              if( i != redactorOrManagersSpacesSize-1) {
                sqlQuery.append(" OR ");
              }
            }
            sqlQuery.append(")");
            sqlQuery.append(" AND exo:draftVisibilityStatus = 'SHARED')");
          }
        } else if (filter.isScheduledNews()) {
          sqlQuery.append("publication:currentState = 'staged'");
          sqlQuery.append(" AND (exo:author = '").append(filter.getAuthor()).append("'");
          for (Space space : redactorOrManagersSpaces ) {
            sqlQuery.append(" OR exo:spaceId = '").append(space.getId()).append("'");
          }
          sqlQuery.append(" )");
        } else {
          if (StringUtils.isNotEmpty(filter.getAuthor())) {
            sqlQuery.append("exo:author = '").append(filter.getAuthor()).append("' AND ");
          }
          sqlQuery.append("(publication:currentState = 'published' OR (publication:currentState = 'draft' AND exo:activities <> '' ))");
        }
        sqlQuery.append(" AND jcr:path LIKE '/Groups/spaces/%' ORDER BY ").append(filter.getOrder()).append(" DESC");
      } else {
        throw new Exception("Unable to build query, filter is null");
      }

    } catch (Exception e) {
      LOG.error("Error while creating query", e);
    }
    return sqlQuery;
  }
}
