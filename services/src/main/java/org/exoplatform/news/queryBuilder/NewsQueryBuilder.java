package org.exoplatform.news.queryBuilder;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.utils.NewsUtils;
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

  private static final String EXO_SPACE_ID      = "exo:spaceId";

  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private static final String FUZZY_SEARCH_SYNTAX = "~0.6";


  /**
   * builds query for news actions
   * 
   * @param filter news filter to customize the query
   * @return built query
   * @throws Exception for error when creating query
   */
  public StringBuilder buildQuery(NewsFilter filter) throws Exception {
    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM exo:news WHERE ");
    //TODO Check if can be retrieved from higher layer
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
          String fuzzyText = this.addFuzzySyntaxAndOR(filter.getSearchText().trim().toLowerCase());
          String escapedQuoteFuzzyText = fuzzyText.replace("'", "''").replace("\"", "\"\"");
          String escapedQuoteSearchText = filter.getSearchText().replace("'", "''").replace("\"", "\"\"");
          sqlQuery.append("(CONTAINS(.,'").append(escapedQuoteFuzzyText).append("') OR (exo:body LIKE '%").append(escapedQuoteSearchText).append("%'))");
          sqlQuery.append("AND ");
        } else {
          if (filter.getTagNames() != null && !filter.getTagNames().isEmpty()) {
            for (String tagName : filter.getTagNames()) {
              sqlQuery.append(" exo:body LIKE '%#").append(tagName).append("%'");
              if (filter.getTagNames().indexOf(tagName) != filter.getTagNames().size() - 1) {
                sqlQuery.append(" OR");
              }
            }
            sqlQuery.append(" AND ");
          }
        }
        if (filter.isPublishedNews()) {
          sqlQuery.append("exo:pinned = 'true' AND ");
        }

        List<String> spaces = filter.getSpaces();
        if (spaces != null && spaces.size() != 0) {
          sqlQuery.append("( ");
          for (int i = 0; i < spaces.size() - 1; i++) {
            sqlQuery.append(EXO_SPACE_ID).append(" = '").append(spaces.get(i)).append("' OR ");
          }
          sqlQuery.append(EXO_SPACE_ID).append(" = '").append(spaces.get(spaces.size() - 1)).append("') AND ");
        }
        if (filter.isDraftNews()) {
          IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
          String currentIdentityId = identity.getId();
          sqlQuery.append("publication:currentState = 'draft'");
          sqlQuery.append(" AND (('");
          sqlQuery.append(currentIdentityId).append("' IN exo:newsModifiersIds AND exo:activities <> '')");
          sqlQuery.append(" OR ");
          sqlQuery.append("( exo:author = '").append(filter.getAuthor()).append("' AND exo:activities = '')");
          List<Space> allowedDraftNewsSpaces = NewsUtils.getAllowedDraftNewsSpaces(currentIdentity.getUserId());
          int allowedDraftNewsSpacesSize = allowedDraftNewsSpaces.size();
          if(allowedDraftNewsSpacesSize > 0) {
            sqlQuery.append("OR (");
            for (int i= 0; i < allowedDraftNewsSpacesSize; i++) {
              sqlQuery.append(EXO_SPACE_ID).append(" = '").append(allowedDraftNewsSpaces.get(i).getId()).append("'");
              if( i != allowedDraftNewsSpacesSize - 1) {
                sqlQuery.append(" OR ");
              }
            }
            sqlQuery.append(")");
            sqlQuery.append(" AND exo:draftVisible = 'true')");
          } else {
            sqlQuery.append(")");
          }
        } else if (filter.isScheduledNews()) {
          List<Space> allowedScheduledNewsSpaces = NewsUtils.getAllowedScheduledNewsSpaces(currentIdentity);
          sqlQuery.append("publication:currentState = 'staged'");
          sqlQuery.append(" AND (exo:author = '").append(filter.getAuthor()).append("'");
          for (Space space : allowedScheduledNewsSpaces ) {
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

  protected String addFuzzySyntaxAndOR(String text) {
    StringBuilder fuzzyText = new StringBuilder();
    boolean quote = false;
    for (int i =0; i < text.length(); i++) {
      if (text.charAt(i) == ' ' && text.charAt(i-1) != '"' && !quote) {
        fuzzyText = fuzzyText.append(FUZZY_SEARCH_SYNTAX);
        if(i != text.length()-1) {
          fuzzyText = fuzzyText.append(" OR ");
        }
      } else if (text.charAt(i) == '"' && !quote){
        fuzzyText = fuzzyText.append("\"");
        quote = true;
      } else if (text.charAt(i) == '"' && quote){
        if (i != text.length()-1) {
          quote = false;
          fuzzyText = fuzzyText.append("\"").append(FUZZY_SEARCH_SYNTAX);
          if(i != text.length()-1) {
            fuzzyText = fuzzyText.append(" OR ");
          }
        } else {
          quote = false;
          fuzzyText = fuzzyText.append("\"");
        }
      }
      else{
        fuzzyText  = fuzzyText.append(text.charAt(i));
      }
    }
    return fuzzyText.append(FUZZY_SEARCH_SYNTAX).toString();
  }
}
