package org.exoplatform.news.rest;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.enterprise.inject.New;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.storage.NewsAttachmentsStorage;

import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagFilter;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.rest.api.RestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.security.*;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NewsRestResourcesV1Test {

  public static final String JOHN = "john";

  private static final MockedStatic<CommonsUtils> COMMONS_UTILS = mockStatic(CommonsUtils.class);

  private static final MockedStatic<RestUtils> REST_UTILS = mockStatic(RestUtils.class);

  @Mock
  NewsService            newsService;

  @Mock
  NewsAttachmentsStorage newsAttachmentsService;

  @Mock
  SpaceService           spaceService;

  @Mock
  IdentityManager        identityManager;

  @Mock
  ActivityManager        activityManager;

  @Mock
  PortalContainer        container;

  @Mock
  FavoriteService        favoriteService;

  @Mock
  ThumbnailService       thumbnailService;

  @Mock
  TagService tagService;

  private NewsRestResourcesV1 newsRestResourcesV1;
  
  @Before
  public void setup() {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    this.newsRestResourcesV1 = new NewsRestResourcesV1(newsService,
                                                       newsAttachmentsService,
                                                       spaceService,
                                                       identityManager,
                                                       container,
                                                       favoriteService,
                                                       thumbnailService);
    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     new org.exoplatform.social.core.identity.model.Identity("1",
                                                                                                                             JOHN,
                                                                                                                             OrganizationIdentityProvider.NAME,
                                                                                                                             false,
                                                                                                                             true,
                                                                                                                             null,
                                                                                                                             null,
                                                                                                                             null);
    when(identityManager.getOrCreateUserIdentity(JOHN)).thenReturn(userIdentity);
  }


  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    REST_UTILS.close();
  }


  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setIllustration("illustration".getBytes());
    lenient().when(newsService.getNewsById(nullable(String.class), any(), nullable(Boolean.class), nullable(String.class))).thenReturn(news);
    lenient().when(spaceService.getSpaceById(nullable(String.class))).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News fetchedNews = (News) response.getEntity();
    assertNotNull(fetchedNews);
    assertNull(fetchedNews.getIllustration());
  }

  @Test
  public void shouldGetNewsByGivenTargetName() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<News> newsList = new LinkedList<>();
    News news = new News();
    news.setId("1");
    List<String> targets = new LinkedList<>();
    targets.add("sliderNews");
    news.setTargets(targets);
    newsList.add(news);
    NewsFilter newsFilter = new NewsFilter();
    newsFilter.setLimit(10);
    lenient().when(newsService.getNewsByTargetName(newsFilter, "sliderNews", currentIdentity)).thenReturn(newsList);

    // When
    Response response = newsRestResourcesV1.getNewsByTarget(request, "sliderNews", 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertNotNull(response.getEntity());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsEntityNews = newsEntity.getNews();
    assertEquals(1, newsEntityNews.size());
  }

  @Test
  public void shouldReturnBadRequestWhenNoActivityId() throws Exception {
    // Given
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(null);
    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundwhenNewsNotFound() throws Exception {
    // Given
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    String activityId = "activityId";

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(activityId);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundWhenNotAccessible() throws Exception {
    // Given
    String activityId = "activityId";

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    when(newsService.getNewsByActivityId(activityId, currentIdentity)).thenThrow(IllegalAccessException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(activityId);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundWhenNewsWithActivityNotFoundException() throws Exception {
    // Given
    String activityId = "activityId";

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    when(newsService.getNewsByActivityId(activityId, currentIdentity)).thenThrow(ObjectNotFoundException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(activityId);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnServerErrorWhenNewsWithActivitythrowsException() throws Exception {
    // Given;
    String activityId = "activityId";

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    when(newsService.getNewsByActivityId(activityId, currentIdentity)).thenThrow(RuntimeException.class);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(activityId);

    // Then
    assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldReturnNewsWhenNewsIsFound() throws Exception {
    // Given
    String activityId = "activityId";

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    News news = mock(News.class);
    when(newsService.getNewsByActivityId(activityId, currentIdentity)).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.getNewsByActivityId(activityId);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(news, response.getEntity());
  }

  @Test
  public void shouldGetNewsWhenNewsExistsAndUserIsNotMemberOfTheSpaceButSuperManager() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean(), nullable(String.class))).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null,false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  // TODO to be retested
  // @Test
  // public void
  // shouldGetNotFoundWhenNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  // News news = new News();
  // news.setPublicationState(PublicationDefaultStates.PUBLISHED);
  // lenient().when(newsService.getNewsById(anyString(), any(),
  // anyBoolean())).thenReturn(news);
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new
  // Space());
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq(JOHN))).thenReturn(false);
  // lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);
  //
  // // When
  // Response response = newsRestResourcesV1.getNewsById(request, "1", null,
  // false);
  //
  // // Then
  // assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetNotFoundWhenNewsNotExists() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean(), nullable(String.class))).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsSpacesWhenNewsExistsAndUserIsMemberOfTheSpace() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
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
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean(), nullable(String.class))).thenReturn(news);
    lenient().when(spaceService.getSpaceById("1")).thenReturn(space1);
    lenient().when(spaceService.getSpaceById("2")).thenReturn(space2);
    lenient().when(spaceService.isMember(space1, JOHN)).thenReturn(true);
    lenient().when(spaceService.isMember(space2, JOHN)).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", "spaces", null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News existingNews = new News();
    existingNews.setTitle("Title");
    existingNews.setSummary("Summary");
    existingNews.setBody("Body");
    existingNews.setPublicationState(PublicationDefaultStates.DRAFT);
    existingNews.setCanEdit(true);
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean(), eq(null))).thenReturn(existingNews);
    News updatedNews = new News();
    updatedNews.setTitle("Updated Title");
    updatedNews.setSummary("Updated Summary");
    updatedNews.setBody("Updated Body");
    updatedNews.setPublicationState(PublicationDefaultStates.PUBLISHED);
    lenient().when(newsService.updateNews(existingNews, JOHN, false, updatedNews.isPublished(), null)).then(returnsFirstArg());

    // When
    Response response = newsRestResourcesV1.updateNews("1", false, null, updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News returnedNews = (News) response.getEntity();
    assertNotNull(returnedNews);
    assertEquals("Updated Title", returnedNews.getTitle());
    assertEquals("Updated Summary", returnedNews.getSummary());
    assertEquals("Updated Body", returnedNews.getBody());
    assertEquals(PublicationDefaultStates.PUBLISHED, returnedNews.getPublicationState());

    when(newsRestResourcesV1.updateNews("1", false, null, updatedNews)).thenThrow(IllegalAccessException.class);

    // When
    response = newsRestResourcesV1.updateNews("1", false, null, updatedNews);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void
  // shouldGetNotAuthorizedWhenUpdatingNewsAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // News news = new News();
  // news.setCanEdit(false);
  // lenient().when(newsService.getNewsById(anyString(), any(),
  // anyBoolean())).thenReturn(news);
  //
  // // When
  // Response response = newsRestResourcesV1.updateNews("1",false, new News());
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetNotFoundWhenUpdatingNewsAndNewsNotExists() throws Exception {
    // Given
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean(), nullable(String.class))).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.updateNews("1", false, null, new News());

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenPinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPublished(true);

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenArchiveNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(false);
    oldnews.setArchived(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setArchived(true);

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(1)).archiveNews("id123", JOHN);
    verify(newsService, times(0)).updateNews(oldnews, request.getRemoteUser(), null, oldnews.isPublished());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUnarchiveNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(false);
    oldnews.setArchived(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setAuthor(JOHN);
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setArchived(false);

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "redactor"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(1)).unarchiveNews("id123", JOHN);
    verify(newsService, times(0)).updateNews(oldnews, request.getRemoteUser(), null, oldnews.isPublished());
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetUnauthorizedWhenArchiveNewsAndNewsExistsAndUserIsNotAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unarchived");
    oldnews.setSummary("unarchived summary");
    oldnews.setBody("unarchived body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(false);
    oldnews.setArchived(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setAuthor("test");
    oldnews.setCanEdit(false);

    News updatedNews = new News();
    updatedNews.setArchived(true);

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "redactor"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);
    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    verify(newsService, times(0)).archiveNews("id123", JOHN);
    verify(newsService, times(0)).updateNews(oldnews, request.getRemoteUser(), null, oldnews.isPublished());
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsAuthorized() throws Exception {
    // Given
    News existingNews = new News();
    existingNews.setTitle("unpinned title");
    existingNews.setSummary("unpinned summary");
    existingNews.setBody("unpinned body");
    existingNews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    existingNews.setCreationDate(date1);
    existingNews.setPublished(false);
    existingNews.setId("id123");
    existingNews.setSpaceId("space");
    existingNews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPublished(true);
    updatedNews.setTitle("pinned title");
    updatedNews.setSummary("pinned summary");
    updatedNews.setBody("pinned body");

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false, null)).thenReturn(existingNews);
    lenient().when(newsService.updateNews(existingNews, JOHN, false, updatedNews.isPublished(), null)).then(returnsFirstArg());

    // When
    Response response = newsRestResourcesV1.updateNews("id123", false, null, updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    News returnedNews = (News) response.getEntity();
    assertNotNull(returnedNews);
    assertEquals("pinned title", returnedNews.getTitle());
    assertEquals("pinned summary", returnedNews.getSummary());
    assertEquals("pinned body", returnedNews.getBody());
  }

  @Test
  public void shouldGetOKWhenUpdatingAndUnpinNewsAndNewsExistsAndAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPublished(false);
    oldnews.setTitle("pinned");

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false, null)).thenReturn(oldnews);

    // When
    Response response = newsRestResourcesV1.updateNews("id123", false, null, updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void
  // shouldGetUnauthorizedWhenUpdatingAndPinNewsAndNewsExistsAndAndUserIsNotAuthorizedToPin()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  //
  // News oldnews = new News();
  // oldnews.setTitle("unpinned");
  // oldnews.setSummary("unpinned summary");
  // oldnews.setBody("unpinned body");
  // oldnews.setUploadId(null);
  // String sDate1 = "22/08/2019";
  // Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
  // oldnews.setCreationDate(date1);
  // oldnews.setPublished(false);
  // oldnews.setId("id123");
  // oldnews.setSpaceId("space");
  // oldnews.setCanEdit(true);
  //
  // News updatedNews = new News();
  // updatedNews.setPublished(true);
  // oldnews.setTitle("pinned");
  //
  // Identity currentIdentity = new Identity(JOHN);
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  //
  // lenient().when(newsService.getNewsById("id123", currentIdentity,
  // false)).thenReturn(oldnews);
  //
  // // When
  // Response response = newsRestResourcesV1.updateNews("id123",false,
  // updatedNews);
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetNotFoundWhenNewsIsNull() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News updatedNews = new News();
    updatedNews.setPublished(true);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void shouldGetUnauthorizedWhenPinNewsAndUserIsNotAuthorized() throws
  // Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  //
  // News oldnews = new News();
  // oldnews.setTitle("unpinned");
  // oldnews.setSummary("unpinned summary");
  // oldnews.setBody("unpinned body");
  // oldnews.setUploadId(null);
  // String sDate1 = "22/08/2019";
  // Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
  // oldnews.setCreationDate(date1);
  // oldnews.setPublished(false);
  // oldnews.setId("id123");
  // oldnews.setSpaceId("space");
  // oldnews.setCanEdit(false);
  //
  // News updatedNews = new News();
  // updatedNews.setPublished(true);
  //
  // Identity currentIdentity = new Identity(JOHN);
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  // Space space = mock(Space.class);
  //
  // lenient().when(newsService.getNewsById("id123", currentIdentity,
  // false)).thenReturn(oldnews);
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);
  //
  // // When
  // Response response = newsRestResourcesV1.patchNews(request, "id123",
  // updatedNews);
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetOKWhenPatchNewsAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(false);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPublished(true);
    updatedNews.setTitle("title updated");
    updatedNews.setSummary("summary updated");
    updatedNews.setBody("body updated");

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenUnpinNewsAndNewsExistsAndUserIsAuthorized() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);

    News oldnews = new News();
    oldnews.setTitle("unpinned");
    oldnews.setSummary("unpinned summary");
    oldnews.setBody("unpinned body");
    oldnews.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    oldnews.setCreationDate(date1);
    oldnews.setPublished(true);
    oldnews.setId("id123");
    oldnews.setSpaceId("space");
    oldnews.setCanEdit(true);

    News updatedNews = new News();
    updatedNews.setPublished(false);

    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    List<MembershipEntry> memberships = new LinkedList<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/web-contributors", "publisher"));
    currentIdentity.setMemberships(memberships);
    Space space = mock(Space.class);

    lenient().when(newsService.getNewsById("id123", currentIdentity, false)).thenReturn(oldnews);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "id123", updatedNews);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenUpdatingNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    // When
    Response response = newsRestResourcesV1.updateNews("1", false, null, null);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenPatchNewsAndUpdatedNewsIsNull() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

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

    HttpServletRequest request = mock(HttpServletRequest.class);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    lenient().when(newsService.getNewsById("1", currentIdentity, false)).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.patchNews(request, "1", new News());

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOKWhenSavingDraftsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");

    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    when(newsRestResourcesV1.createNews(request, news)).thenThrow(IllegalAccessException.class);

    // When
    response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOkWhenCreateNewsWithPublishedState() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetOkWhenScheduleNews() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setPublicationState(PublicationDefaultStates.STAGED);
    news.setCanEdit(true);
    news.setSpaceId("1");
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.scheduleNews(request, news);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void
  // shouldGetNotAuthorizedWhenCreatingNewsDraftAndNewsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  // org.exoplatform.services.security.Identity currentIdentity = new
  // org.exoplatform.services.security.Identity(JOHN);
  // News news = new News();
  // news.setId("1");
  // news.setSpaceId("1");
  //
  // lenient().when(newsService.getNewsById(anyString(), any(),
  // anyBoolean())).thenReturn(news);
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new
  // Space());
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq(JOHN))).thenReturn(false);
  // lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);
  //
  // // When
  // Response response = newsRestResourcesV1.createNews(request, news);
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetBadRequestWhenCreatingNewsDraftAndNewsIsNull() throws Exception {
    // Given
    News news = new News();
    news.setId("1");

    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.createNews(request, new News());

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftWhenNewsDraftExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");

    when(newsService.getNewsById(anyString(), any(), anyBoolean(), nullable(String.class))).thenReturn(news);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  // TODO to be retested
  // @Test
  // public void
  // shouldGetNotAuthorizedWhenNewsDraftExistsAndUserIsNotMemberOfTheSpaceNorSuperManager()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  // News news = new News();
  // news.setId("1");
  // news.setSpaceId("1");
  // news.setPublicationState(PublicationDefaultStates.PUBLISHED);
  // lenient().when(newsService.getNewsById(anyString(), any(),
  // anyBoolean())).thenReturn(news);
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new
  // Space());
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq(JOHN))).thenReturn(false);
  // lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);
  //
  // // When
  // Response response = newsRestResourcesV1.getNewsById(request, "1", null,
  // false);
  //
  // // Then
  // assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
  // response.getStatus());
  // }

  @Test
  public void shouldGetNotFoundWhenNewsDraftNotExists() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null, false);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenNewsDraftIsNull() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);

    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, null, null, null, false);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNewsDraftListWhenNewsDraftsExistsAndUserIsMemberOfTheSpaceAndSuperManager() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);

    lenient().when(newsService.getNews(any(NewsFilter.class), any())).thenReturn(newsDrafts);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, "1", "drafts", "", 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserIsNotMemberOfTheSpaceNorSuperManager() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    lenient().when(newsService.getNews(any(NewsFilter.class), any())).thenReturn(newsDrafts);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(false);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, "1", PublicationDefaultStates.DRAFT, null, 0, 10, false);
    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotAuthorizedWhenNewsDraftsExistsAndUserNotExists() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn("");
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("1");
    News news2 = new News();
    news2.setId("2");
    news2.setSpaceId("1");
    List<News> newsDrafts = new ArrayList<>();
    newsDrafts.add(news);
    newsDrafts.add(news2);
    lenient().when(newsService.getNews(any(NewsFilter.class), any())).thenReturn(newsDrafts);
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
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setAuthor(JOHN);
    news.setSpaceId("1");
    news.setCanDelete(true);

    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    verify(newsService).deleteNews("1", currentIdentity, false);
  }

  @Test
  public void shouldNotDeleteNewsWhenUserIsNotDraftAuthor() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setAuthor("mary");
    news.setSpaceId("1");
    news.setCanDelete(true);

    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    verify(newsService).deleteNews("1", currentIdentity, false);
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void
  // shouldGetNotAuthorizedWhenDeletingNewsDraftThatExistsAndUserIsNotMemberOfTheSpaceNorSuperManager()
  // throws Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  // Identity currentIdentity = new Identity(JOHN);
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  // News news = new News();
  // news.setId("1");
  // news.setCanDelete(false);
  // news.setSpaceId("1");
  //
  // lenient().when(newsService.getNewsById(anyString(), any(),
  // anyBoolean())).thenReturn(news);
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new
  // Space());
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq(JOHN))).thenReturn(false);
  // lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);
  //
  // // When
  // Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  // verify(newsService, never()).deleteNews("1", currentIdentity, false);
  // }

  @Test
  public void shouldGetNotFoundWhenDeletingNewsDraftThatNotExists() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, "1", false, 0L);

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1", currentIdentity, false);
  }

  @Test
  public void shouldGetBadRequestWhenDeletingNewsDraftWithIdNull() throws Exception {
    // Given

    HttpServletRequest request = mock(HttpServletRequest.class);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(null);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, null, false, 0L);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    verify(newsService, never()).deleteNews("1", currentIdentity, false);
  }

  @Test
  public void shouldGetAllPublishedNewsWhenExist() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news1 = new News();
    News news2 = new News();
    News news3 = new News();
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, null, "", null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertEquals(3, newsList.size());
  }

  @Test
  public void shouldGetEmptyListWhenNoPublishedExists() throws Exception {
    // Given

    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    NewsFilter newsFilter = new NewsFilter();
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(newsService.getNews(newsFilter, currentIdentity)).thenReturn(null);

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, null, null, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    assertNotNull(newsEntity);
    assertEquals(0, newsEntity.getNews().size());
  }

  @Test
  public void shouldGetOKWhenViewNewsAndNewsExists() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setSpaceId("space1");
    news.setViewsCount((long) 6);

    lenient().when(newsService.getNewsById("1", currentIdentity, false, null)).thenReturn(news);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    lenient().when(spaceService.getSpaceById("space1")).thenReturn(space1);
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);
    lenient().when(spaceService.isSuperManager(eq(JOHN))).thenReturn(false);

    // When
    Response response = newsRestResourcesV1.getNewsById(request, "1", null, null, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetNotFoundWhenViewNewsAndNewsIsNull() throws Exception {
    // Given

    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setViewsCount((long) 6);

    lenient().when(newsService.getNewsById("1", currentIdentity, false, null)).thenReturn(news);
    // When
    Response response = newsRestResourcesV1.getNewsById(request, "2", null, null, false);
    ;

    // Then
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetAllPinnedNewsWhenExist() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news1 = new News();
    news1.setPublished(true);
    news1.setAuthor(JOHN);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setPublished(true);
    news2.setAuthor(JOHN);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setPublished(true);
    news3.setAuthor(JOHN);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(newsService.getNewsCount(any())).thenReturn(allNews.size());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, null, "pinned", null, 0, 10, true);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(true, newsList.get(i).isPublished());
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
    }
    assertEquals(0, newsEntity.getOffset().intValue());
    assertEquals(10, newsEntity.getLimit().intValue());
    assertEquals(3, newsEntity.getSize().intValue());
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    COMMONS_UTILS.when(()-> CommonsUtils.getService(TagService.class)).thenReturn(tagService);
    REST_UTILS.when(()-> RestUtils.getCurrentUserIdentityId()).thenReturn(1L);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spacesIds, "", text, 0, 5, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
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
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spaceId = "4";
    News news1 = new News();
    news1.setSpaceId(spaceId);
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId(spaceId);
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId(spaceId);
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    COMMONS_UTILS.when(()-> CommonsUtils.getService(TagService.class)).thenReturn(tagService);
    REST_UTILS.when(()-> RestUtils.getCurrentUserIdentityId()).thenReturn(1L);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spaceId, "", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals("4", newsList.get(i).getSpaceId());
    }
  }

  @Test
  public void shouldGetAllNewsWhenSearchingWithTagTextInTheGivenSpace() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String tagText = "tagText";
    String newsBody = "body including tag text #tagText";
    String spaceId = "4";
    News news1 = new News();
    news1.setSpaceId(spaceId);
    news1.setAuthor(JOHN);
    news1.setTitle("newsTitle");
    news1.setBody(newsBody);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId(spaceId);
    news2.setAuthor(JOHN);
    news2.setTitle("newsTitle");
    news2.setBody(newsBody);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    COMMONS_UTILS.when(()-> CommonsUtils.getService(TagService.class)).thenReturn(tagService);
    REST_UTILS.when(()-> RestUtils.getCurrentUserIdentityId()).thenReturn(1L);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spaceId, "", tagText, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(2, newsList.size());
    for (News newsItem : newsList) {
      assertTrue(newsItem.getBody().contains(tagText));
    }
  }

  @Test
  public void shouldGetPinnedNewsWhenSearchingWithTextInTheGivenSpaces() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setPublished(true);
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setPublished(true);
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setPublished(true);
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    COMMONS_UTILS.when(()-> CommonsUtils.getService(TagService.class)).thenReturn(tagService);
    REST_UTILS.when(()-> RestUtils.getCurrentUserIdentityId()).thenReturn(1L);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spacesIds, "pinned", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(true, newsList.get(i).isPublished());
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetUnauthorizedWhenSearchingWithTextInNonMemberSpaces() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String text = "search";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setPublished(true);
    News news2 = new News();
    news2.setPublished(true);
    News news3 = new News();
    news3.setPublished(true);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(false);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spacesIds, "pinned", text, 0, 10, false);

    // Then
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetMyPostedNewsWhenExists() throws Exception {
    // Given

    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, null, filter, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenFilteringWithTheGivenSpaces() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spacesIds, filter, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetMyPostedNewsWhenSearchingWithTheGivenSpaces() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    String filter = "myPosted";
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    COMMONS_UTILS.when(()-> CommonsUtils.getService(TagService.class)).thenReturn(tagService);
    REST_UTILS.when(()-> RestUtils.getCurrentUserIdentityId()).thenReturn(1L);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, spacesIds, filter, text, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    for (int i = 0; i < newsList.size(); i++) {
      assertEquals(PublicationDefaultStates.PUBLISHED, newsList.get(i).getPublicationState());
      assertEquals(JOHN, newsList.get(i).getAuthor());
      assertEquals(text, newsList.get(i).getTitle());
      assertEquals(true, spacesIds.contains(newsList.get(i).getSpaceId()));
    }
  }

  @Test
  public void shouldGetStagedNewsWhenCurrentUserIsAuthor() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    lenient().when(request.getLocale()).thenReturn(new Locale("en"));
    News news = new News();
    news.setSpaceId("1");
    news.setAuthor(JOHN);
    news.setPublicationState(PublicationDefaultStates.STAGED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news);

    lenient().when(newsService.getNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());

    // When
    Response response = newsRestResourcesV1.getNews(request, JOHN, null, null, null, 0, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    NewsEntity newsEntity = (NewsEntity) response.getEntity();
    List<News> newsList = newsEntity.getNews();
    assertNotNull(newsList);
    assertEquals(1, newsList.size());
    assertEquals(PublicationDefaultStates.STAGED, newsList.get(0).getPublicationState());
    assertEquals(JOHN, newsList.get(0).getAuthor());
  }

  // TODO to be moved with newsService tests
  // @Test
  // public void shouldScheduleNewsWhenUserIsSpaceManagerOrSpaceRedactor() throws
  // Exception {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  //
  // Identity currentIdentity = new Identity(JOHN);
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  // News news = new News();
  // lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new
  // Space());
  // lenient().when(spaceService.isRedactor(any(Space.class),
  // eq(JOHN))).thenReturn(true);
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq(JOHN))).thenReturn(true);
  // lenient().when(spaceService.isManager(any(Space.class),
  // eq(JOHN))).thenReturn(true);
  //
  // // When
  // Response response = newsRestResourcesV1.canScheduleNews("1");
  //
  // // Then
  // assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  //
  // currentIdentity = new Identity("Mary");
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  // News news1 = new News();
  // lenient().when(spaceService.getSpaceById("Mary")).thenReturn(new Space());
  // lenient().when(spaceService.isRedactor(any(Space.class),
  // eq("Mary"))).thenReturn(false);
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq("Mary"))).thenReturn(true);
  // lenient().when(spaceService.isManager(any(Space.class),
  // eq("Mary"))).thenReturn(true);
  //
  // // When
  // Response response1 = newsRestResourcesV1.canScheduleNews("1");
  //
  // // Then
  // assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
  //
  // currentIdentity = new Identity("Eric");
  // ConversationState.setCurrent(new ConversationState(currentIdentity));
  // News news2 = new News();
  // lenient().when(spaceService.getSpaceById("Eric")).thenReturn(new Space());
  // lenient().when(spaceService.isRedactor(any(Space.class),
  // eq("Eric"))).thenReturn(false);
  // lenient().when(spaceService.isMember(any(Space.class),
  // eq("Eric"))).thenReturn(false);
  // lenient().when(spaceService.isManager(any(Space.class),
  // eq("Eric"))).thenReturn(false);
  //
  // // When
  // Response response2 = newsRestResourcesV1.canScheduleNews("1");
  //
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response2.getStatus());
  // }

  @Test
  public void shouldDeleteNews() throws Exception {
    // Given
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    news.setCanDelete(true);
    lenient().when(newsService.getNewsById(anyString(), any(), anyBoolean())).thenReturn(news);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    lenient().when(spaceService.isMember(any(Space.class), eq(JOHN))).thenReturn(true);

    // When
    Response response = newsRestResourcesV1.deleteNews(request, news.getId(), false, 0);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  public void openNewsAttachmentById() {
    // Given

    HttpServletRequest request = mock(HttpServletRequest.class);
    NewsAttachment attachment = new NewsAttachment("111", "111", "attachment", "png", 2);
    try {
      lenient().when(newsAttachmentsService.getNewsAttachment(attachment.getId())).thenReturn(attachment);
      lenient().when(newsAttachmentsService.getNewsAttachmentOpenUrl(attachment.getId())).thenReturn(anyString());
    } catch (Exception e) {

    }

    // When
    Response response = newsRestResourcesV1.openNewsAttachmentById(request, "111");

    // Then
    assertEquals(Response.Status.TEMPORARY_REDIRECT.getStatusCode(), response.getStatus());
  }

  @Test
  public void getNewsIllustrationTest() throws Exception {
    // Given
    Request rsRequest = mock(Request.class);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setSpaceId("1");
    news.setAuthor(JOHN);
    news.setPublished(true);
    news.setIllustrationUpdateDate(new Date());
    news.setIllustration("illustration".getBytes());
    news.setIllustrationMimeType("image");
    News news1 = new News();
    news1.setSpaceId("2");
    news1.setAuthor(JOHN);
    news1.setPublished(true);
    news1.setIllustrationUpdateDate(new Date());
    news1.setIllustration("illustration".getBytes());
    news1.setIllustrationMimeType("image/gif");

    lenient().when(newsService.getNewsById("1", currentIdentity, false, null)).thenReturn(news);
    lenient().when(newsService.getNewsById("2", currentIdentity, false, null)).thenReturn(news1);

    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);


    // When
    when(thumbnailService.createCustomThumbnail(any(), anyInt(), anyInt(),anyString())).thenReturn("illustration".getBytes());
    Response response = newsRestResourcesV1.getNewsIllustration(rsRequest, request, "1", 2316465L, null,"300x300");
    Response response1 = newsRestResourcesV1.getNewsIllustration(rsRequest, request, "2", 2316465L, null, "300x300");
    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    byte[] illustration = (byte[]) response.getEntity();
    assertNotNull(illustration);
    assertEquals("illustration", new String(illustration));
    assertEquals(Response.Status.OK.getStatusCode(), response1.getStatus());
    byte[] illustration1 = (byte[]) response1.getEntity();
    assertNotNull(illustration1);
    assertEquals("illustration", new String(illustration1));

  }

  // TODO to be moved with newsService tests
  // @Test
  // public void canCreateNewsTest() {
  // // Given
  // NewsRestResourcesV1 newsRestResourcesV1 = new
  // NewsRestResourcesV1(newsService,
  // newsAttachmentsService,
  // spaceService,
  // identityManager,
  // container);
  // HttpServletRequest request = mock(HttpServletRequest.class);
  //
  // Space space = new Space();
  // space.setId("1");
  //
  // lenient().when(request.getRemoteUser()).thenReturn(JOHN);
  // lenient().when(spaceService.getSpaceById("1")).thenReturn(space);
  //
  // // When
  // Response response = newsRestResourcesV1.canCreateNews(request, "1");
  // // Then
  // assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
  // response.getStatus());
  //
  // lenient().when(spaceService.isMember(space, JOHN)).thenReturn(true);
  // // When
  // response = newsRestResourcesV1.canCreateNews(request, "1");
  // // Then
  // assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  // String canCreateNews = (String) response.getEntity();
  // assertNotNull(canCreateNews);
  // assertEquals("true", canCreateNews);
  // }

  @Test
  public void shouldGetBadRequestWhenSearchingWithoutQueryAndFavorites() throws Exception {
    // Given
    UriInfo uriInfo = mock(UriInfo.class);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
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
    Response response = newsRestResourcesV1.search(uriInfo, httpServletRequest, "", "", 0,null, 10, false);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenSearchingWithNegativeOffset() throws Exception {
    // Given
    UriInfo uriInfo = mock(UriInfo.class);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    setCurrentUser(JOHN);

    // When
    Response response = newsRestResourcesV1.search(uriInfo, httpServletRequest, "query", "", -1,null, 10, false);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetBadRequestWhenSearchingWithNegativeLimit() throws Exception {
    // Given
    UriInfo uriInfo = mock(UriInfo.class);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String text = "text";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    setCurrentUser(JOHN);

    // When
    Response response = newsRestResourcesV1.search(uriInfo, httpServletRequest, "query", "", 0,null, -1, false);

    // Then
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  public void shouldGetnewsListWhenSearchingWithQuery() throws Exception {
    // Given
    UriInfo uriInfo = mock(UriInfo.class);
    HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
    String text = "text";
    String spacesIds = "4,1";
    News news1 = new News();
    news1.setSpaceId("4");
    news1.setAuthor(JOHN);
    news1.setTitle(text);
    news1.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news2 = new News();
    news2.setSpaceId("1");
    news2.setAuthor(JOHN);
    news2.setTitle(text);
    news2.setPublicationState(PublicationDefaultStates.PUBLISHED);
    News news3 = new News();
    news3.setSpaceId("4");
    news3.setAuthor(JOHN);
    news3.setTitle(text);
    news3.setPublicationState(PublicationDefaultStates.PUBLISHED);
    List<News> allNews = new ArrayList<>();
    allNews.add(news1);
    allNews.add(news2);
    allNews.add(news3);
    lenient().when(newsService.searchNews(any(), any())).thenReturn(allNews);
    lenient().when(spaceService.isMember(any(Space.class), any())).thenReturn(true);
    lenient().when(spaceService.getSpaceById(anyString())).thenReturn(new Space());
    setCurrentUser(JOHN);

    // When
    Response response = newsRestResourcesV1.search(uriInfo, httpServletRequest, text, "", 0,null, 10, false);

    // Then
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    List<NewsSearchResultEntity> newsList = (List<NewsSearchResultEntity>) response.getEntity();
    assertNotNull(newsList);
    assertEquals(0, newsList.size());
  }

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }

  @Test
  public void testMarkAsRead() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRemoteUser()).thenReturn(JOHN);
    Identity currentIdentity = new Identity(JOHN);
    ConversationState.setCurrent(new ConversationState(currentIdentity));
    News news = new News();
    news.setId("1");
    when(newsService.getNewsById("1",currentIdentity, false)).thenReturn(news);
    doNothing().when(newsService).markAsRead(news, JOHN);
    Response response = newsRestResourcesV1.markNewsAsRead(request, "1");
    verify(newsService, times(1)).markAsRead(news,JOHN);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }
}
