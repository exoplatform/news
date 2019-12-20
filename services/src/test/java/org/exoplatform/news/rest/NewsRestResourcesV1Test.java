package org.exoplatform.news.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.news.NewsAttachmentsService;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.filter.NewsFilter;
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
  NewsAttachmentsService newsAttachmentsService;

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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setIllustration("illustration".getBytes());
    when(newsService.getNewsById(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News fetchedNews = (News) response.getEntity();
    assertNotNull(fetchedNews);
    assertNull(fetchedNews.getIllustration());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsNotMemberOfTheSpaceButSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNewsById(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNewsById(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingNewsAndNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News existingNews = new News();
    existingNews.setTitle("Title");
    existingNews.setSummary("Summary");
    existingNews.setBody("Body");
    existingNews.setPublicationState("draft");
    when(newsService.getNewsById(anyString())).thenReturn(existingNews);
    News updatedNews = new News();
    updatedNews.setTitle("Updated Title");
    updatedNews.setSummary("Updated Summary");
    updatedNews.setBody("Updated Body");
    updatedNews.setPublicationState("published");
    when(newsService.updateNews(any())).then(returnsFirstArg());
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.updateNews(request, "1", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News returnedNews = (News) response.getEntity();
    assertNotNull(returnedNews);
    assertEquals("Updated Title", returnedNews.getTitle());
    assertEquals("Updated Summary", returnedNews.getSummary());
    assertEquals("Updated Body", returnedNews.getBody());
    assertEquals("published", returnedNews.getPublicationState());
  }

  @Test
  public void shouldGetOKWhenShareNewsAndNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
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
            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

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
    Space space = mock(Space.class);

    when(newsService.getNewsById("id123")).thenReturn(existingNews);
    when(newsService.updateNews(any())).then(returnsFirstArg());
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("space");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

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
  public void shouldGetOKWhenUpdatingAndUnpinNewsAndNewsExistsAndAndUserIsPublisher() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    Mockito.doNothing().when(newsService).unpinNews("id123");
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");

    News updatedNews = new News();
    updatedNews.setPinned(true);

    when(newsService.getNewsById("id123")).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenPinNewsAndUserIsNotAuthorized() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    when(newsService.getNewsById("id123")).thenReturn(oldnews);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    when(newsService.getNewsById("1")).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenSavingDraftsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setPublicationState("published");
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
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
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);


    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenNewsDraftNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetBadRequestWhenNewsDraftIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftListWhenNewsDraftsExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "draft", "", null);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "draft", "", null);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }
  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
    Response response = newsRestResourcesV1.getNews(request, "mike", "1", "draft", "", null);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNoTFoundWhenNoDraftsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsDrafts(anyString(), anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, "john", "1", "", "", null);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    List<News> news = (List<News>) response.getEntity();
    assertNull(news);
  }

  @Test
  public void shouldDeleteNewsWhenNewsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("john");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    verify(newsService).deleteNews("1");
  }

  @Test
  public void shouldNotDeleteNewsWhenUserIsNotDraftAuthor() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setAuthor("mary");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
    verify(newsService, never()).deleteNews("1");
  }

  @Test
  public void shouldGetNotAuthorizedWhenDeletingNewsDraftThatExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(false);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1");
  }

  @Test
  public void shouldGetNotFoundWhenDeletingNewsDraftThatNotExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1");
  }

  @Test
  public void shouldGetBadRequestWhenDeletingNewsDraftWithIdNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(newsService.getNewsById(anyString())).thenReturn(null);
    when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1");
  }

  @Test
  public void shouldGetNotFoundWhenShareNewsAndNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
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
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    News news = new News();
    news.setId("1");
    when(request.getRemoteUser()).thenReturn("john");
    Mockito.doReturn(news).when(newsService).getNewsById("1");

    // When
    Response response = newsRestResourcesV1.shareNews(request, "1", new SharedNews());

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenShareNewsAndNSpaceIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    when(newsService.getNewsById(anyString())).thenReturn(news);
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

  @Test
  public void shouldGetAllPublishedNewsWhenExist() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news1 = new News();
    News news2 = new News();
    News news3 = new News();
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.getNews(any())).thenReturn(allNews);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "published","", null);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> news = (List<News>) response.getEntity();
    assertEquals(3, news.size());
  }

  @Test
  public void shouldGetEmptyListWhenNoPublishedExists() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    NewsFilter newsFilter = new NewsFilter();
    when(newsService.getNews(newsFilter)).thenReturn(null);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "published", null, null);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> news = (List<News>) response.getEntity();
    assertEquals(0, news.size());
  }

  @Test
  public void shouldGetOKWhenViewNewsAndNewsExists() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setSpaceId("space1");
    news.setViewsCount((long) 6);
    when(newsService.getNewsById("1")).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    when(spaceService.getSpaceById("space1")).thenReturn(space1);
    when(spaceService.isMember(any(Space.class), eq("john"))).thenReturn(true);
    when(spaceService.isSuperManager(eq("john"))).thenReturn(false);
    Mockito.doNothing().when(newsService).markAsRead(news,"john");

    // When
    Response response = newsRestResourcesV1.viewNews(request, "1");

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenViewNewsAndNewsIsNull() throws Exception {
    // Given
    NewsRestResourcesV1 newsRestResourcesV1 =
                                            new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news = new News();
    news.setId("1");
    news.setViewsCount((long) 6);
    when(newsService.getNewsById("1")).thenReturn(news);
    Mockito.doNothing().when(newsService).markAsRead(news,"john");

    // When
    Response response = newsRestResourcesV1.viewNews(request, "2");

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetAllPinnedNewsWhenExist() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    News news1 = new News();
    news1.setPinned(true);
    news1.setAuthor("john");
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setPinned(true);
    news2.setAuthor("john");
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setPinned(true);
    news3.setAuthor("john");
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.getNews(any())).thenReturn(allNews);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "published","pinned", null);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals(true, newsList.get(i).isPinned());
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
    }
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.searchNews(any(), any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "published","", text);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTextInTheGivenSpace() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spaceId = "4";
    News news1 = new News();
    news1.setSpaceId(spaceId);
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setSpaceId(spaceId);
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setSpaceId(spaceId);
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.searchNews(any(), any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spaceId, "published","", text);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals("4", newsList.get(i).getSpaceId());
    }
  }

  @Test
  public void shouldGetPinnedNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setPinned(true);
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setPinned(true);
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setPinned(true);
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.searchNews(any(), any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "published","pinned", text);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals(true, newsList.get(i).isPinned());
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetUnauthorizedWhenSearchingWithTextInNonMemberSpaces() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
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
    when(newsService.getNews(any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(false);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "published","pinned", text);

    //Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetMyPostedNewsWhenExists() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.getNews(any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", null, "published", filter, null);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenFilteringWithTheGivenSpaces() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.getNews(any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "published", filter, null);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenSearchingWithTheGivenSpaces() throws Exception{
    //Given
    NewsRestResourcesV1 newsRestResourcesV1 = new NewsRestResourcesV1(newsService, newsAttachmentsService, spaceService, identityManager);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn("john");
    when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor("john");
    news1.setTitle(text);
    news1.setPublicationState("published");
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor("john");
    news2.setTitle(text);
    news2.setPublicationState("published");
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor("john");
    news3.setTitle(text);
    news3.setPublicationState("published");
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    when(newsService.searchNews(any(), any())).thenReturn(allNews);
    when(spaceService.isMember(anyString(),any())).thenReturn(true);

    //When
    Response response = newsRestResourcesV1.getNews(request, "john", spacesIds, "published", filter, text);

    //Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<News> newsList = (List<News>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i=0; i<newsList.size(); i++){
      assertEquals("published", newsList.get(i).getPublicationState());
      assertEquals("john", newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

}
