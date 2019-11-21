package org.exoplatform.news.connector;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.ResultNode;
import org.exoplatform.services.wcm.search.base.AbstractPageList;
import org.exoplatform.services.wcm.search.connector.DocumentSearchServiceConnector;

import java.util.ArrayList;
import java.util.List;

public class NewsSearchConnector extends DocumentSearchServiceConnector {

  private static final Log LOG = ExoLogger.getLogger(NewsSearchConnector.class.getName());

  private static final String FUZZY_SEARCH_SYNTAX = "~0.6";

  public NewsSearchConnector(InitParams initParams) throws Exception {
    super(initParams);
  }

  public List<ResultNode> search(SearchContext context,
                                 String query,
                                 int offset,
                                 int limit,
                                 String sort,
                                 String order) {
    AbstractPageList<ResultNode> ret;
    List<ResultNode> listResult = new ArrayList<>();
    //prepare input parameters for search
    if (query != null) {
      query = this.addFuzzySyntaxAndOR(query.trim().toLowerCase());
    }
    QueryCriteria criteria = createQueryCriteria(query, offset, limit, sort, order);

    //query search result
    try {
      ret = searchNodes(criteria, context);
      for (int i = 1; i <= ret.getAvailablePage(); i++) {
        List<ResultNode> list = ret.getPageWithOffsetCare(i);
        for (ResultNode res : list) {
          listResult.add(res);
        }
      }
    } catch (Exception e) {
      LOG.error("Error while searching News", e);
    }
    return listResult;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected QueryCriteria createQueryCriteria(String query, long offset, long limit, String sort, String order) {
    QueryCriteria criteria = new QueryCriteria();
    //set content types
    criteria.setContentTypes(getNodeTypes());
    criteria.setNodeTypes(getNodeTypes());
    criteria.setKeyword(removeAccents(query));
    criteria.setSearchWebpage(false);
    criteria.setSearchDocument(true);
    criteria.setFuzzySearch(true);
    criteria.setLiveMode(true);
    criteria.setOffset(offset);
    criteria.setLimit(limit);
    criteria.setSortBy(sort);
    criteria.setOrderBy(order);
    if (ConversationState.getCurrent().getIdentity().getUserId() != null) {
      criteria.setSearchPath("");
    }
    return criteria;
  }
  protected String[] getNodeTypes() {
    String[] types = {"exo:news"};
    return types;
  }

  public AbstractPageList<ResultNode> searchNodes(QueryCriteria criteria, SearchContext context) throws Exception {
    return super.searchNodes(criteria, context);
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
