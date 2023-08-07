package org.exoplatform.news.connector;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.queryBuilder.NewsQueryBuilder;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NewsSearchConnector extends SearchServiceConnector {

  private SessionProviderService sessionProviderService;

  private RepositoryService repositoryService;

  private static final Log LOG = ExoLogger.getLogger(NewsSearchConnector.class.getName());


  public NewsSearchConnector(InitParams initParams,SessionProviderService sessionProviderService,RepositoryService repositoryService) throws Exception {
    super(initParams);
    this.sessionProviderService = sessionProviderService;
    this.repositoryService = repositoryService;
  }

  @Override
  public Collection<SearchResult> search(SearchContext context, String query, Collection<String> sites, int offset, int limit, String sort, String order) {
    NewsFilter filter = new NewsFilter();
    filter.setSearchText(query);
    List<SearchResult> ret = new ArrayList<SearchResult>();
    try {
      ret = search(filter, offset, limit, sort, order);
    } catch (RepositoryException e) {
      LOG.error("Error while searching News", e);
    }
    return ret;
  }

  public List<SearchResult> search(NewsFilter filter,
                                 int offset,
                                 int limit,
                                 String sort,
                                 String order) throws RepositoryException {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Session session = sessionProvider.getSession(repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(),
            repositoryService.getCurrentRepository());

    List<SearchResult> res = new ArrayList<>();
    NewsQueryBuilder queryBuilder = new NewsQueryBuilder();
    try {
      StringBuilder sqlQuery = queryBuilder.buildQuery(filter);
      QueryManager qm = session.getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
      ((QueryImpl) query).setOffset(filter.getOffset());
      ((QueryImpl) query).setLimit(filter.getLimit());
      QueryResult result = query.execute();
      NodeIterator nodeIterator = result.getNodes();
      RowIterator rowIterator = result.getRows();
      while (nodeIterator.hasNext()) {
        Node node = nodeIterator.nextNode();
        Row row = rowIterator.nextRow();
        String nodeIllustrationURL = "";
        if(node.hasNode("illustration")) {
          nodeIllustrationURL = "/portal/rest/v1/news/" + node.getUUID() + "/illustration";
        }
        NewsSearchResult searchResult = new NewsSearchResult("", node.getName(),
                "", "", nodeIllustrationURL,
                node.hasProperty("exo:dateCreated") ? node.getProperty("exo:dateCreated").getDate().getTimeInMillis() : 0,
                row.getValue("jcr:score") != null ? row.getValue("jcr:score").getLong() : 0,
                node);
        res.add(searchResult);
      }
    } catch(Exception e) {
      LOG.error("Error while searching News", e);
    }
    return res;
  }
}
