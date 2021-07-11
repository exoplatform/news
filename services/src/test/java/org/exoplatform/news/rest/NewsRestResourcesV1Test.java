package org.exoplatform.news.rest;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.NewsAttachmentsService;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.*;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

@RunWith(MockitoJUnitRunner.class)
public class NewsRestResourcesV1Test {

  @Mock
  NewsService            newsService;

  @Mock
  NewsAttachmentsService newsAttachmentsService;

  @Mock
  SpaceService           spaceService;

  @Mock
  IdentityManager        identityManager;

  @Mock
  ActivityManager        activityManager;

  @Mock
  PortalContainer        container;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setIllustration("illustration".getBytes());
    lenient().when(newsService.getNewsById(nullable(String.class), eq("john"), nullable(Boolean.class))).thenReturn(news);
    lenient().when(spaceService.getSpaceById(nullable(String.class))).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News fetchedNews = (News) response.getEntity();
    assertNotNull(fetchedNews);
    assertNull(fetchedNews.getIllustration());
  }

  @Test
  public void shouldReturnBadRequestWhenNoActivityId() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(null, null);
    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundwhenNewsNotFound() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    String username = "mary";
    String activityId = "activityId";

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(username);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(request, activityId);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundWhenNotAccessible() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    String username = "mary";
    String activityId = "activityId";
    
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(username);
    when(newsService.getNewsByActivityId(activityId, username)).thenThrow(IllegalAccessException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(request, activityId);
    
    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundWhenNewsWithActivityNotFoundException() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    String username = "mary";
    String activityId = "activityId";
    
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(username);
    when(newsService.getNewsByActivityId(activityId, username)).thenThrow(ObjectNotFoundException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(request, activityId);
    
    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnServerErrorWhenNewsWithActivitythrowsException() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    String username = "mary";
    String activityId = "activityId";
    
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(username);
    when(newsService.getNewsByActivityId(activityId, username)).thenThrow(RuntimeException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(request, activityId);
    
    // Then
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNewsWhenNewsIsFound() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    String username = "mary";
    String activityId = "activityId";

    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(username);

    News news = mock(News.class);
    when(newsService.getNewsByActivityId(activityId, username)).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(request, activityId);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(news, response.getEntity());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsNotMemberOfTheSpaceButSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    lenient().when(newsService.getNewsById(anyString(), eq("john"), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsSpacesWhenNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager,container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setIllustration("illustration".getBytes());
    news.setActivities("1:1;2:2");
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    Space space2 = new Space();
    space1.setId("2");
    space1.setPrettyName("space2");
    lenient().when(newsService.getNewsById(anyString(), eq("john"), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById("1")).thenReturn(space1);
    lenient().when(spaceService.getSpaceById("2")).thenReturn(space2);
    lenient().when(spaceService.isMember(space1, "john")).thenReturn(true);
    lenient().when(spaceService.isMember(space2, "john")).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", "spaces", false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }


  @Test
  public void shouldGetOKWhenUpdatingNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News existingNews = new News();
    existingNews.setTitle("Title");
    existingNews.setSummary("Summary");
    existingNews.setBody("Body");
    existingNews.setPublicationState(PublicationDefaultStates.DRAFT);
    existingNews.setCanEdit(true);
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(existingNews);
    News updatedNews = new News();
    updatedNews.setTitle("Updated Title");
    updatedNews.setSummary("Updated Summary");
    updatedNews.setBody("Updated Body");
    updatedNews.setPublicationState(PublicationDefaultStates.PUBLISHED);
    lenient().when(newsService.updateNews(any())).then(returnsFirstArg());

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News returnedNews = (News) response.getEntity();
    assertNotNull(returnedNews);
    assertEquals("Updated Title", returnedNews.getTitle());
    assertEquals("Updated Summary", returnedNews.getSummary());
    assertEquals("Updated Body", returnedNews.getBody());
    assertEquals(PublicationDefaultStates.PUBLISHED, returnedNews.getPublicationState());
  }

  @Test
  public void shouldGetNotAuthorizedWhenUpdatingNewsAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setCanEdit(false);
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenUpdatingNewsAndNewsNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenPinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);
    
    News updatedNews = new News();
    updatedNews.setPinned(true);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenArchiveNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setArchived(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setArchived(true);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(1)).archiveNews("id123");
    verify(newsService, times(0)).updateNews(oldnews);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUnarchiveNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setArchived(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setAuthor("john");
    oldnews.setCanEdit(true);
    
    News updatedNews = new News();
    updatedNews.setArchived(false);
    
    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "redactor"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(1)).unarchiveNews("id123");
    verify(newsService, times(0)).updateNews(oldnews);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenArchiveNewsAndNewsExistsAndUserIsNotAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setArchived(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setAuthor("test");
    oldnews.setCanEdit(false);
    
    News updatedNews = new News();
    updatedNews.setArchived(true);
    
    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "redactor"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);
    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(0)).archiveNews("id123");
    verify(newsService, times(0)).updateNews(oldnews);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News existingNews = new News();
    existingNews.setTitle("unpinned title");
    existingNews.setSummary("unpinned summary");
    existingNews.setBody("unpinned body");
    existingNews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    existingNews.setCreationDate(date1);
    existingNews.setPinned(false);
    existingNews.setId("id123");
    existingNews.setSpaceId("space");
    existingNews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPinned(true);
    updatedNews.setTitle("pinned title");
    updatedNews.setSummary("pinned summary");
    updatedNews.setBody("pinned body");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);

    lenient().when(newsService.getNewsById("id123", false)).thenReturn(existingNews);
    lenient().when(newsService.updateNews(any())).then(returnsFirstArg());

    // When
    Response response = newsRestResourcesV1.updateNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News returnedNews = (News) response.getEntity();
    assertNotNull(returnedNews);
    assertEquals("pinned title", returnedNews.getTitle());
    assertEquals("pinned summary", returnedNews.getSummary());
    assertEquals("pinned body", returnedNews.getBody());
    verify(newsService).pinNews("id123");
  }

  @Test
  public void shouldGetOKWhenUpdatingAndUnpinNewsAndNewsExistsAndAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPinned(false);
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);

    lenient().doNothing().when(newsService).unpinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    
    // When
    Response response = newsRestResourcesV1.updateNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsNotAuthorizedToPin() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPinned(true);
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News updatedNews = new News();
    updatedNews.setPinned(true);

    lenient().when(newsService.getNewsById("id123", false)).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenPinNewsAndUserIsNotAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(false);
    
    News updatedNews = new News();
    updatedNews.setPinned(true);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenPatchNewsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);
    
    News updatedNews = new News();
    updatedNews.setPinned(true);
    updatedNews.setTitle("title updated");
    updatedNews.setSummary("summary updated");
    updatedNews.setBody("body updated");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).pinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUnpinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPinned(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);
    
    News updatedNews = new News();
    updatedNews.setPinned(false);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().doNothing().when(newsService).unpinNews("id123");
    lenient().when(newsService.getNewsById("id123", false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenUpdatingNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenPatchNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");

    // When
    Response response = newsRestResourcesV1.patchNews(request, "1", null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenPatchNewsAndUSpaceIsNull() throws Exception {
    // Given
    News news = new News();
    news.setId("1");
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById("1", false)).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenSavingDraftsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOkWhenCreateNewsWithPublishedState() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOkWhenScheduleNews() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setPublicationState(PublicationDefaultStates.STAGED);
    news.setCanEdit(true);
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.scheduleNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenCreatingNewsDraftAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");

    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenCreatingNewsDraftAndNewsIsNull() throws Exception {
    // Given
    News news = new News();
    news.setId("1");
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, new News());

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftWhenNewsDraftExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");

    when(newsService.getNewsById(anyString(), eq("john"), anyBoolean())).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsDraftNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenNewsDraftIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, null, null, false);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftListWhenNewsDraftsExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);

    lenient().when(newsService.getNews(any(NewsFilter.class))).thenReturn(newsDrafts);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "drafts", "", 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    lenient().when(newsService.getNews(any(NewsFilter.class))).thenReturn(newsDrafts);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", PublicationDefaultStates.DRAFT, null, 0, 10, false);
    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    lenient().when(newsService.getNews(any(NewsFilter.class))).thenReturn(newsDrafts);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "mike", "1", PublicationDefaultStates.DRAFT, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldDeleteNewsWhenNewsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("john");
    news.setSpaceId("1");
    news.setCanDelete(true);

    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    verify(newsService).deleteNews("1", false);
  }

  @Test
  public void shouldNotDeleteNewsWhenUserIsNotDraftAuthor() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("mary");
    news.setSpaceId("1");
    news.setCanDelete(true);

    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    verify(newsService).deleteNews("1", false);
  }

  @Test
  public void shouldGetNotAuthorizedWhenDeletingNewsDraftThatExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setCanDelete(false);
    news.setSpaceId("1");

    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1", false);
  }

  @Test
  public void shouldGetNotFoundWhenDeletingNewsDraftThatNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1", false);
  }

  @Test
  public void shouldGetBadRequestWhenDeletingNewsDraftWithIdNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(newsService.getNewsById(anyString(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, null, false, 0L);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1", false);
  }

  @Test
  public void shouldGetAllPublishedNewsWhenExist() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news1 = new News();
    News news2 = new News();
    News news3 = new News();
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "", null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertEquals(3, newsList.size());
  }

  @Test
  public void shouldGetEmptyListWhenNoPublishedExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    NewsFilter newsFilter = new NewsFilter();
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(newsService.getNews(newsFilter)).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", null, null, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    assertNotNull(newsEntity);
    assertEquals(0, newsEntity.getNews().size());
  }

  @Test
  public void shouldGetOKWhenViewNewsAndNewsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("space1");
    news.setViewsCount((long) 6);

    lenient().when(newsService.getNewsById("1", "john", false)).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById("space1")).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    lenient().doNothing().when(newsService).markAsRead(news, "john");

    // When
    Response response = newsRestResourcesV1.viewNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenViewNewsAndNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setViewsCount((long) 6);

    lenient().when(newsService.getNewsById("1", "john", false)).thenReturn(news);
    lenient().doNothing().when(newsService).markAsRead(news, "john");

    // When
    Response response = newsRestResourcesV1.viewNews(request, "2");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetAllPinnedNewsWhenExist() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    News news1 = new News();
    news1.setPinned(true);
    news1.setAuthor("john");
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setPinned(true);
    news2.setAuthor("john");
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setPinned(true);
    news3.setAuthor("john");
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(newsService.getNewsCount(any())).thenReturn(allNews.size());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "pinned", null, 0, 10, true);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(true, newsList.get(i).isPinned());
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
    }
    assertEquals(0, newsEntity.getOffset().intValue());
    assertEquals(10, newsEntity.getLimit().intValue());
    assertEquals(3, newsEntity.getSize().intValue());
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "", text, 0, 5, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
    assertEquals(0, newsEntity.getOffset().intValue());
    assertEquals(5, newsEntity.getLimit().intValue());
    assertNull(newsEntity.getSize());
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTextInTheGivenSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spaceId = "4";
    News news1 = new News();
    news1.setSpaceId(spaceId);
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId(spaceId);
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId(spaceId);
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spaceId, "", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals("4", newsList.get(i).getSpaceId());
    }
  }

  @Test
  public void shouldGetPinnedNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setPinned(true);
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setPinned(true);
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setPinned(true);
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "pinned", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(true, newsList.get(i).isPinned());
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetUnauthorizedWhenSearchingWithTextInNonMemberSpaces() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setPinned(true);
    News news2 = new News();
    news2.setPinned(true);
    News news3 = new News();
    news3.setPinned(true);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(false);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "pinned", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetMyPostedNewsWhenExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", null, filter, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenFilteringWithTheGivenSpaces() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, filter, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenSearchingWithTheGivenSpaces() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, filter, text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetStagedNewsWhenCurrentUserIsAuthor() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                                      newsAttachmentsService,
                                                                      spaceService,
                                                                      identityManager,
                                                                      container);
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("john");
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    News news = new News();
    news.setSpaceId("1");
    news.setAuthor("john");
    news.setPublicationState(PublicationDefaultStates.STAGED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news);

    lenient().when(newsService.getNews(any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", null, null, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(1, newsList.size());
    assertEquals(PublicationDefaultStates.STAGED, newsList.get(0).getPublicationState());
    assertEquals("john", newsList.get(0).getAuthor());
  }

}
