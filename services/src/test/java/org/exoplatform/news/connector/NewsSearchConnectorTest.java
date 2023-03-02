package org.exoplatform.news.connector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.ecms.legacy.search.data.SearchContext;
import org.exoplatform.ecms.legacy.search.data.SearchResult;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.security.ConversationState;

@RunWith(MockitoJUnitRunner.class)
public class NewsSearchConnectorTest {

  public static final String searchType                           = "searchType";

  public static final String displayName                           = "displayName";

  @Mock
  RepositoryService repositoryService;

  @Mock
  SessionProviderService sessionProviderService;

  @Mock
  ManageableRepository repository;

  @Mock
  RepositoryEntry repositoryEntry;

  @Mock
  SessionProvider sessionProvider;

  @Mock
  Session session;

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }

  @Test
  public void shouldGetEmptyListWhenNoNewsExists() throws Exception {
    // Given
    setCurrentUser("root");
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    String searchText = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 0;

    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    NewsFilter filter  =new NewsFilter();
    filter.setSearchText(searchText);
    QueryManager qm = mock(QueryManager.class);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(workSpace.getQueryManager()).thenReturn(qm);
    Query query = mock(QueryImpl.class);
    when(qm.createQuery(anyString(),anyString())).thenReturn(query);
    QueryResult queryResult = mock(QueryResult.class);
    when(query.execute()).thenReturn(queryResult);
    NodeIterator nodeIterator = mock(NodeIterator.class);
    RowIterator rowIterator = mock(RowIterator.class);
    when(queryResult.getNodes()).thenReturn(nodeIterator);
    when(queryResult.getRows()).thenReturn(rowIterator);
    when(nodeIterator.hasNext()).thenReturn(false);
    lenient().when(rowIterator.hasNext()).thenReturn(false);

    // When
    List<SearchResult> listResult = newsSearchConnector.search(filter, offset, limit, sort, order );

    // Then
    assertNotNull(listResult);
    assertEquals(0, listResult.size());
  }

  @Test
  public void shouldGetResultWhenNewsExists() throws Exception {
    // Given
    setCurrentUser("root");
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    List<SearchResult> listResult;
    String searchText = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 10;
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    NewsFilter filter  =new NewsFilter();
    filter.setSearchText(searchText);
    QueryManager qm = mock(QueryManager.class);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(workSpace.getQueryManager()).thenReturn(qm);
    Query query = mock(QueryImpl.class);
    when(qm.createQuery(anyString(),anyString())).thenReturn(query);
    QueryResult queryResult = mock(QueryResult.class);
    when(query.execute()).thenReturn(queryResult);
    NodeIterator nodeIterator = mock(NodeIterator.class);
    RowIterator rowIterator = mock(RowIterator.class);
    Node node = mock(Node.class);
    Row row = mock(Row.class);
    when(queryResult.getNodes()).thenReturn(nodeIterator);
    when(nodeIterator.hasNext()).thenReturn(true).thenReturn(false);
    when(nodeIterator.nextNode()).thenReturn(node);
    when(queryResult.getRows()).thenReturn(rowIterator);
    lenient().when(rowIterator.hasNext()).thenReturn(true).thenReturn(false);
    when(rowIterator.nextRow()).thenReturn(row);

    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);

    // When
    listResult = newsSearchConnector.search(filter, offset, limit, sort, order );

    // Then
    assertNotNull(listResult);
    assertEquals(1, listResult.size());
    verify(nodeIterator, times(2)).hasNext();
    verify(nodeIterator, times(1)).nextNode();
    verify(rowIterator, times(1)).nextRow();
  }

  @Test
  public void shouldAddFuzzySyntaxWhitQuotedWord() throws Exception {
    // Given
    String text = "\"quoted\"";
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);

    // When
    String result = newsSearchConnector.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals(result, "\"quoted\"~0.6");
  }

  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsMultiWords() throws Exception {
    // Given
    String text = "search text";
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);

    // When
    String result = newsSearchConnector.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals(result, "search~0.6 OR text~0.6");
  }

  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsQuotedMultiWords() throws Exception {
    // Given
    String text = "\"search text\"";
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);

    // When
    String result = newsSearchConnector.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals(result, "\"search text\"~0.6");
  }

  @Test
  public void shouldAddFuzzySyntaxWhenTextContainsMultiWordsAndQuotedOne() throws Exception {
    // Given
    String text = "this is a \"search text\"";
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);

    // When
    String result = newsSearchConnector.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals(result, "this~0.6 OR is~0.6 OR a~0.6 OR \"search text\"~0.6");
  }

  @Test
  public void shouldGetResultWhenSearchingWithQuery() throws Exception {
    // Given
    setCurrentUser("root");
    InitParams initParams = new InitParams();
    PropertiesParam propertiesParam = new PropertiesParam();
    Property searchTypeParam = new Property();
    searchTypeParam.setName(searchType);
    searchTypeParam.setValue("News");
    Property displayNameParam = new Property();
    displayNameParam.setName(displayName);
    displayNameParam.setValue("News");
    propertiesParam.setName("constructor.params");
    propertiesParam.addProperty(searchTypeParam);
    propertiesParam.addProperty(displayNameParam);
    initParams.addParameter(propertiesParam);
    SearchContext context = mock(SearchContext.class);
    String searchText = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 10;
    List<String> sites = new ArrayList<>();
    sites.add("news");

    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Workspace workspace = mock(Workspace.class);
    QueryManager queryManager = mock(QueryManager.class);
    Query query = mock(QueryImpl.class);
    QueryResult queryResult = mock(QueryResult.class);

    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams, sessionProviderService, repositoryService);
    when(session.getWorkspace()).thenReturn(workspace);
    when(workspace.getQueryManager()).thenReturn(queryManager);
    when(queryManager.createQuery(anyString(), anyString())).thenReturn(query);
    when(query.execute()).thenReturn(queryResult);
    NodeIterator nodeIterator = mock(NodeIterator.class);
    RowIterator rowIterator = mock(RowIterator.class);
    Node node = mock(Node.class);
    Row row = mock(Row.class);
    when(queryResult.getNodes()).thenReturn(nodeIterator);
    when(nodeIterator.hasNext()).thenReturn(true).thenReturn(false);
    when(nodeIterator.nextNode()).thenReturn(node);
    when(node.hasNode(anyString())).thenReturn(true);
    when(node.hasProperty(anyString())).thenReturn(false);
    when(queryResult.getRows()).thenReturn(rowIterator);
    when(rowIterator.nextRow()).thenReturn(row);

    // When
    Collection<SearchResult> ret = newsSearchConnector.search(context, searchText, sites, offset, limit, sort, order);

    // Then
    assertNotNull(ret);
    assertEquals(1, ret.size());
  }

}
