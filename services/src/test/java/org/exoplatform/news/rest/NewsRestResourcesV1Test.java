package org.exoplatform.news.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NewsRestResourcesV1Test {

  @Mock
  NewsService newsService;

  @Mock
  SpaceService spaceService;

  @Mock
  IdentityManager identityManager;

  @Mock
  ActivityManager activityManager;

  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setIllustration("illustration".getBytes());
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News fetchedNews = (News) response.getEntity();
    assertNotNull(fetchedNews);
    assertNull(fetchedNews.getIllustration());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsNotMemberOfTheSpaceButSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingNewsAndNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenShareNewsAndNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    when(spaceService.getSpaceByPrettyName(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    SharedNews sharedNews = new SharedNews();
    sharedNews.setDescription("Description of shared news");
    sharedNews.setSpacesNames(Arrays.asList("space1"));
    sharedNews.setActivityId("2");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "1", sharedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    ArgumentCaptor<SharedNews> sharedNewsCaptor = ArgumentCaptor.forClass(SharedNews.class);
    ArgumentCaptor<List<Space>> spacesCaptor = ArgumentCaptor.forClass((Class) List.class);
    verify(newsService, times(1)).shareNews(sharedNewsCaptor.capture(), spacesCaptor.capture());
    SharedNews sharedNewsCaptorValue = sharedNewsCaptor.getValue();
    assertEquals("john", sharedNewsCaptorValue.getPoster());
    assertEquals("Description of shared news", sharedNewsCaptorValue.getDescription());
    assertEquals("2", sharedNewsCaptorValue.getActivityId());
    assertEquals("1", sharedNewsCaptorValue.getNewsId());
    List<Space> spacesCaptorValue = spacesCaptor.getValue();
    assertEquals(1, spacesCaptorValue.size());
  }

  @Test
  public void shouldGetNotAuthorizedWhenUpdatingNewsAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenUpdatingNewsAndNewsNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetOKWhenPinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    News updatedNews = new News();
    updatedNews.setPinned(true);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).pinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsPublisher() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    News updatedNews = new News();
    updatedNews.setPinned(true);
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).pinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    // When
    Response response = newsRestResourcesV1.updateNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsNotAuthorizedToPin() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    News updatedNews = new News();
    updatedNews.setPinned(true);
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).pinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    // When
    Response response = newsRestResourcesV1.updateNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  /*
   * @Test public void shouldGetBadRequestWhenPinNewsAndUpdatedNewsIsNull()
   * throws Exception { // Given NewsRestResourcesV1 newsRestResourcesV1 = new
   * NewsRestResourcesV1(newsService, spaceService, identityManager,
   * activityManager); HttpServletRequest request =
   * mock(HttpServletRequest.class);
   * when(request.getRemoteUser()).thenReturn("john"); // When Response response
   * = newsRestResourcesV1.patchNews(request, "id123", null); // Then
   * assertEquals(Response.Status.BAD_REQUEST.getStatusCode(),
   * response.getStatus()); }
   */

  @Test
  public void shouldGetNotFoundWhenNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

    News updatedNews = new News();
    updatedNews.setPinned(true);

    when(newsService.getNews("id123")).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenPinNewsAndUserIsNotAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    News updatedNews = new News();
    updatedNews.setPinned(true);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).pinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenPatchNewsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    Mockito.doNothing().when(newsService).pinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUnpinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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

    News updatedNews = new News();
    updatedNews.setPinned(false);

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).unpinNews("id123");
    Mockito.doNothing().when(newsService).updateNews(oldnews);
    when(newsService.getNews("id123")).thenReturn(oldnews);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);
    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetBadRequestWhenUpdatingNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }
  
  @Test
  public void shouldGetBadRequestWhenPatchNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    when(newsService.getNews("1")).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenSavingDraftsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOkWhenCreateNewsWithPublishedState() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setPublicationState("published");
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenCreatingNewsDraftAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, new News());

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftWhenNewsDraftExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsDraftNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetBadRequestWhenNewsDraftIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftListWhenNewsDraftsExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    when(newsService.getNewsDrafts(anyString(), anyString())).thenReturn(newsDrafts);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "draft");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    when(newsService.getNewsDrafts(anyString(), anyString())).thenReturn(newsDrafts);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "draft");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    when(newsService.getNewsDrafts(anyString(), anyString())).thenReturn(newsDrafts);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, "", "1", "draft");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNoTFoundWhenNoDraftsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsDrafts(anyString(), anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    List<News> news = (List<News>) response.getEntity();
    assertNull(news);
  }

  @Test
  public void shouldDeleteNewsWhenNewsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("john");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldNotDeleteNewsWhenUserIsNotDraftAuthor() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("mary");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenDeletingNewsDraftThatExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenDeletingNewsDraftThatNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenDeletingNewsDraftWithIdNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, spaceService, identityManager,activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNews(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenShareNewsAndNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    SharedNews sharedNews = new SharedNews();
    sharedNews.setDescription("Description of shared news");
    sharedNews.setSpacesNames(Arrays.asList("space1"));
    sharedNews.setActivityId("2");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "2", sharedNews);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenShareNewsAndSpacesNamesOfSharedNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    News news = new News();
    news.setId("1");
    when(request.getRemoteUser()).thenReturn("john");
    Mockito.doReturn(news).when(newsService).getNews("1");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "1", new SharedNews());

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenShareNewsAndNSpaceIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    when(spaceService.getSpaceByPrettyName(anyString())).thenReturn(null);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);
    SharedNews sharedNews = new SharedNews();
    sharedNews.setDescription("Description of shared news");
    sharedNews.setSpacesNames(Arrays.asList("space1"));
    sharedNews.setActivityId("2");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "1", sharedNews);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenShareNewsAndNewsExistsAndUserIsNotMemberOfTheSpaceAndUserIsNOtSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, spaceService, identityManager, activityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNews(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    when(spaceService.getSpaceByPrettyName(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    SharedNews sharedNews = new SharedNews();
    sharedNews.setDescription("Description of shared news");
    sharedNews.setSpacesNames(Arrays.asList("space1"));
    sharedNews.setActivityId("2");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "1", sharedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

}
