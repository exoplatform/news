package org.exoplatform.news.connector;

import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.search.QueryCriteria;
import org.exoplatform.services.wcm.search.ResultNode;
import org.exoplatform.services.wcm.search.base.AbstractPageList;
import org.exoplatform.services.wcm.search.base.ArrayNodePageList;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class NewsSearchConnectorTest {
  public static final String searchType                           = "searchType";
  public static final String displayName                           = "displayName";

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
    SearchContext context = mock(SearchContext.class);
    String query = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 0;
    AbstractPageList<ResultNode> ret = new ArrayNodePageList<>(0);

    QueryCriteria criteria = mock(QueryCriteria.class);
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    NewsSearchConnector newsSearchConnectorSpy = Mockito.spy(newsSearchConnector);

    Mockito.doReturn(criteria).when(newsSearchConnectorSpy).createQueryCriteria(query, offset, limit, sort, order);
    Mockito.doReturn(ret).when(newsSearchConnectorSpy).searchNodes(any(), any());


    // when
    List<ResultNode> listResult = newsSearchConnectorSpy.search(context, query, offset, limit, sort, order );

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
    List<ResultNode> listResult;
    SearchContext context = mock(SearchContext.class);
    ResultNode resultNode = mock(ResultNode.class);
    String query = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 0;
    List<ResultNode> list = new ArrayList<>();
    list.add(resultNode);
    AbstractPageList<ResultNode> ret = new ArrayNodePageList<>(list, 1);

    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    NewsSearchConnector newsSearchConnectorSpy = Mockito.spy(newsSearchConnector);

    Mockito.doReturn(ret).when(newsSearchConnectorSpy).searchNodes(any(), any());


    // when
    listResult = newsSearchConnectorSpy.search(context, query, offset, limit, sort, order );

    // Then
    assertNotNull(listResult);
    assertEquals(1, listResult.size());
    assertEquals(list, listResult);
  }

  @Test
  public void shouldCreateCriteria() throws Exception {
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
    String query = "Test";
    String sort = "relevancy";
    String order = "desc";
    int offset = 0;
    int limit = 0;

    QueryCriteria criteria;
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);

    // when
    criteria = newsSearchConnector.createQueryCriteria(query, offset, limit, sort, order);

    // Then
    assertNotNull(criteria);
    assertEquals(offset, criteria.getOffset());
    assertEquals(limit, criteria.getLimit());
    assertEquals(sort, criteria.getSortBy());
    assertEquals(order, criteria.getOrderBy());
    assertEquals(query, criteria.getKeyword());
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
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    // when
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
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    // when
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
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    // when
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
    NewsSearchConnector newsSearchConnector = new NewsSearchConnector(initParams);
    // when
    String result = newsSearchConnector.addFuzzySyntaxAndOR(text);

    // Then
    assertNotNull(result);
    assertEquals(result, "this~0.6 OR is~0.6 OR a~0.6 OR \"search text\"~0.6");
  }
  

}
