package org.exoplatform.news.storage.jcr;

import static org.exoplatform.news.storage.jcr.JcrNewsStorage.EXO_PRIVILEGEABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.social.rest.api.RestUtils;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.connector.NewsSearchConnector;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchConnector;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.ecm.publication.impl.PublicationServiceImpl;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.services.wcm.extensions.publication.WCMPublicationServiceImpl;
import org.exoplatform.services.wcm.extensions.publication.impl.PublicationManagerImpl;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationPlugin;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.common.service.HTMLUploadImageProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JcrNewsStorageTest {

  private static final MockedStatic<CommonsUtils>    COMMONS_UTILS    = mockStatic(CommonsUtils.class);

  private static final MockedStatic<PortalContainer> PORTAL_CONTAINER = mockStatic(PortalContainer.class);

  private static final MockedStatic<RestUtils> REST_UTILS = mockStatic(RestUtils.class);

  @Mock
  RepositoryService          repositoryService;

  @Mock
  SessionProviderService     sessionProviderService;

  @Mock
  ManageableRepository       repository;

  @Mock
  RepositoryEntry            repositoryEntry;

  @Mock
  SessionProvider            sessionProvider;

  @Mock
  Session                    session;

  @Mock
  NodeHierarchyCreator       nodeHierarchyCreator;

  @Mock
  DataDistributionManager    dataDistributionManager;

  @Mock
  SpaceService               spaceService;

  @Mock
  ActivityManager            activityManager;

  @Mock
  IdentityManager            identityManager;

  @Mock
  UploadService              uploadService;

  @Mock
  LinkManager                linkManager;

  @Mock
  WCMPublicationServiceImpl  wcmPublicationServiceImpl;

  @Mock
  PublicationManagerImpl     publicationManagerImpl;

  @Mock
  PublicationServiceImpl     publicationServiceImpl;

  @Mock
  AuthoringPublicationPlugin authoringPublicationPlugin;

  @Mock
  HTMLUploadImageProcessor imageProcessor;

  @Mock
  NewsSearchConnector        newsSearchConnector;

  @Mock
  NewsAttachmentsStorage     newsAttachmentsService;
  
  @Mock
  IndexingService indexingService;
  
  @Mock
  NewsESSearchConnector    newsESSearchConnector;
  
  @Mock
  UserACL                    userACL;

  @Rule
  public ExpectedException   exceptionRule = ExpectedException.none();

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    PORTAL_CONTAINER.close();
    REST_UTILS.close();
  }

  @Test
  public void shouldGetNodeWhenNewsExists() throws Exception {
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    Node node = mock(Node.class);
    Property property = mock(Property.class);
    when(property.getString()).thenReturn("");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(node);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(node.getSession()).thenReturn(session);
    when(node.getProperty(nullable(String.class))).thenReturn(property);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(property.getLong()).thenReturn((long) 10);
    Space space = mock(Space.class);
    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
    when(space.getGroupId()).thenReturn("/spaces/space1");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("test");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
    when(spaceService.isSuperManager("user")).thenReturn(false);
    when(space.getVisibility()).thenReturn("private");
    when(spaceService.isSuperManager(nullable(String.class))).thenReturn(false);

    // When
    News news = jcrNewsStorage.getNewsById("1", false);

    // Then
    assertNotNull(news);
  }

  @Test
  public void shouldGetNullWhenNewsDoesNotExist() throws Exception {
    // Given
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(null);

    // When
    News news = jcrNewsStorage.getNewsById("1", false);

    // Then
    assertNull(news);
  }

  @Test
  public void shouldGetLastNewsVersionWhenNewsExistsAndHasVersions() throws Exception {
    // Given
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    Node node = mock(Node.class);
    Property property = mock(Property.class);
    when(property.getString()).thenReturn("");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(node);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(node.getSession()).thenReturn(session);
    when(node.hasProperty(AdditionalMatchers.not(eq("exo:dateModified")))).thenReturn(true);
    when(node.getProperty(AdditionalMatchers.not(eq("exo:dateModified")))).thenReturn(property);
    Calendar calendarUpdated = Calendar.getInstance();
    calendarUpdated.set(2019, 11, 20, 12, 00);
    Property updatedProperty = mock(Property.class);
    when(updatedProperty.getDate()).thenReturn(calendarUpdated);
    when(node.hasProperty(eq("exo:dateModified"))).thenReturn(true);
    when(node.getProperty(eq("exo:dateModified"))).thenReturn(updatedProperty);
    when(node.isNodeType(eq("mix:versionable"))).thenReturn(true);
    VersionHistory versionHistory = mock(VersionHistory.class);
    when(node.getVersionHistory()).thenReturn(versionHistory);
    Version version1 = mock(Version.class);
    when(version1.getUUID()).thenReturn("1");
    when(version1.getName()).thenReturn("1");
    when(version1.getSession()).thenReturn(session);
    Property version1AuthorProperty = mock(Property.class);
    when(version1AuthorProperty.getString()).thenReturn("john");
    when(version1.hasProperty(eq("exo:lastModifier"))).thenReturn(true);
    when(version1.getProperty(eq("exo:lastModifier"))).thenReturn(version1AuthorProperty);
    Calendar calendar1 = Calendar.getInstance();
    calendar1.set(2019, 11, 20, 10, 00);
    Property version1CreatedProperty = mock(Property.class);
    when(version1CreatedProperty.getDate()).thenReturn(calendar1);
    when(version1.hasProperty(eq("exo:lastModifiedDate"))).thenReturn(true);
    when(version1.getProperty(eq("exo:lastModifiedDate"))).thenReturn(version1CreatedProperty);
    when(version1.getContainingHistory()).thenReturn(mock(VersionHistory.class));
    Version version2 = mock(Version.class);
    when(version2.getUUID()).thenReturn("2");
    when(version2.getName()).thenReturn("2");
    when(version2.getSession()).thenReturn(session);
    Property version2AuthorProperty = mock(Property.class);
    when(version2AuthorProperty.getString()).thenReturn("mary");
    when(version2.hasProperty(eq("exo:lastModifier"))).thenReturn(true);
    when(version2.getProperty(eq("exo:lastModifier"))).thenReturn(version2AuthorProperty);
    Calendar calendar2 = Calendar.getInstance();
    Property version2CreatedProperty = mock(Property.class);
    when(version2CreatedProperty.getDate()).thenReturn(calendar2);
    when(version2.hasProperty(eq("exo:lastModifiedDate"))).thenReturn(true);
    when(version2.getProperty(eq("exo:lastModifiedDate"))).thenReturn(version2CreatedProperty);
    calendar2.set(2019, 11, 20, 11, 00);
    when(version2.getCreated()).thenReturn(calendar2);
    when(version2.getContainingHistory()).thenReturn(mock(VersionHistory.class));
    VersionIterator versionIterator = mock(VersionIterator.class);
    when(versionIterator.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false)
            .thenReturn(true).thenReturn(true).thenReturn(false)
            .thenReturn(true).thenReturn(true).thenReturn(false);
    when(versionIterator.nextVersion()).thenReturn(version1).thenReturn(version2)
            .thenReturn(version1).thenReturn(version2)
            .thenReturn(version1).thenReturn(version2);
    when(versionHistory.getAllVersions()).thenReturn(versionIterator);
    when(versionHistory.getRootVersion()).thenReturn(mock(Version.class));
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(property.getLong()).thenReturn((long) 10);
    Space space = mock(Space.class);
    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
    when(space.getGroupId()).thenReturn("/spaces/space1");
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentPortalOwner()).thenReturn("intranet");
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn("portal");
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
    memberships.add(membershipentry);
    currentIdentity.setMemberships(memberships);
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId("2");
    activity.isHidden(false);
    activityManager.saveActivityNoReturn(activity);
    Property actProperty = mock(Property.class);
    when(actProperty.getString()).thenReturn("1:2;1:3");
    when(node.getProperty(eq("exo:activities"))).thenReturn(actProperty);
    when(space.getVisibility()).thenReturn("private");
    when(spaceService.isMember(nullable(String.class), nullable(String.class))).thenReturn(false);
    when(spaceService.isSuperManager(nullable(String.class))).thenReturn(false);
    when(activityManager.getActivity("2")).thenReturn(activity);

    // When
    News news = jcrNewsStorage.getNewsById("1", false);

    // Then
    assertNotNull(news);
    assertEquals(calendar1.getTime(), news.getPublicationDate());
    assertEquals(calendar2.getTime(), news.getUpdateDate());
    assertEquals("mary", news.getUpdater());
  }

//TODO to be moved with newsService tests
//  @Test
//  public void shouldCantViewNewsWhenSpaceIsNotFound() throws Exception {
//    NewsService newsService = buildNewsService();
//    News news = new News();
//    news.setSpaceId("space1");
//    assertFalse(newsService.canViewNews(news, "root"));
//  }
//  @Test
//  public void shouldCantViewPublishedNewsWhenNotSpaceMember() throws Exception {
//    NewsService newsService = buildNewsService();
//    
//    String spaceId = "space1";
//    
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
//    
//    when(spaceService.getSpaceById(spaceId)).thenReturn(new Space());
//    assertFalse(newsService.canViewNews(news, "mary"));
//  }
//  @Test
//  public void shouldCanViewPublishedNewsWhenNotSpaceMember() throws Exception {
//    NewsService newsService = buildNewsService();
//    
//    String spaceId = "space1";
//    
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
//    news.setPublished(true);
//
//    when(spaceService.getSpaceById(spaceId)).thenReturn(new Space());
//    assertTrue(newsService.canViewNews(news, "mary"));
//  }
//  @Test
//  public void shouldCanViewPublishedStagedNewsWhenNotSpaceMember() throws Exception {
//    NewsService newsService = buildNewsService();
//
//    String spaceId = "space1";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    news.setPublished(true);
//    when(spaceService.getSpaceById(spaceId)).thenReturn(new Space());
//    assertFalse(newsService.canViewNews(news, "mary"));
//  }
//  @Test
//  public void shouldCanViewNotPublishedAndPostedNewsWhenSuperManager() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String superUser = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
//    news.setPublished(true);
//    when(spaceService.getSpaceById(spaceId)).thenReturn(new Space());
//    when(spaceService.isSuperManager(spaceId)).thenReturn(true);
//    assertTrue(newsService.canViewNews(news, superUser));
//  }
//  @Test
//  public void shouldCantViewNotPublishedAndStagedNewsWhenSuperManager() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String superUser = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    news.setPublished(true);
//    when(spaceService.getSpaceById(spaceId)).thenReturn(new Space());
//    when(spaceService.isSuperManager(spaceId)).thenReturn(true);
//    // FIXME How is this ? the super user can't see a staged news but can edit it !
//    assertFalse(newsService.canViewNews(news, superUser));
//  }
//  @Test
//  public void shouldCantViewNotPublishedAndStagedNewsWhenIsMember() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    news.setPublished(true);
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isMember(space, spaceId)).thenReturn(true);
//    assertFalse(newsService.canViewNews(news, username));
//  }
//  @Test
//  public void shouldCanViewNotPublishedAndSpaceMember() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
//    news.setPublished(true);
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isMember(space, spaceId)).thenReturn(true);
//    assertTrue(newsService.canViewNews(news, username));
//  }
//  @Test
//  public void shouldCanViewNotPublishedAndStagedNewsAndSpaceManager() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isManager(space, username)).thenReturn(true);
//    assertTrue(newsService.canViewNews(news, username));
//  }
//
//  @Test
//  public void shouldCanViewNotPublishedAndStagedNewsAndSpaceRedactor() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    news.setPublished(true);
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isRedactor(space, username)).thenReturn(true);
//    assertTrue(newsService.canViewNews(news, username));
//  }
//  @Test
//  public void shouldCanViewNotPublishedAndStagedNewsAndIsAuthor() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor(username);
//    news.setPublicationState(PublicationDefaultStates.STAGED);
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    assertTrue(newsService.canViewNews(news, username));
//  }
//  @Test
//  public void shouldCanEditNewsWhenIsAuthor() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor(username);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//
//    assertTrue(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCantEditNewsWhenNoIdentity() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor(username);
//    
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//
//    assertFalse(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCantEditNewsWhenNoSpace() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor(username);
//
//    assertFalse(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCanEditNewsWhenSuperSpacesManager() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor(username);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isSuperManager(username)).thenReturn(true);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//
//    assertTrue(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCanEditNewsWhenIsRedactorAndSharedDraft() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//    news.setAuthor("john");
//    news.setDraftVisible(true);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isRedactor(space, username)).thenReturn(true);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//
//    assertTrue(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCanEditNewsWhenIsSpaceManager() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//    
//    News news = new News();
//    news.setSpaceId(spaceId);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isManager(space, username)).thenReturn(true);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//    
//    assertTrue(newsService.canEditNews(news, username));
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCantEditNewsWhenIsSpaceMember() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isMember(space, username)).thenReturn(true);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//    when(identity.isMemberOf("/platform/web-contributors", "publisher")).thenReturn(false);
//
//    assertFalse(newsService.canEditNews(news, username));
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCantEditNewsWhenIsSpaceRedactor() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//    when(spaceService.isRedactor(space, username)).thenReturn(true);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//    when(identity.isMemberOf("/platform/web-contributors", "publisher")).thenReturn(false);
//
//    assertFalse(newsService.canEditNews(news, username));
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCanEditNewsIsGeneralRedactor() throws Exception {
//    NewsService newsService = buildNewsService();
//    String spaceId = "space1";
//    String username = "mary";
//
//    News news = new News();
//    news.setSpaceId(spaceId);
//
//    Space space = new Space();
//    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
//
//    Identity userIdentity = new Identity();
//    userIdentity.setRemoteId(username);
//    when(identityManager.getOrCreateUserIdentity(username)).thenReturn(userIdentity);
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//    when(identity.isMemberOf("/platform/web-contributors", "publisher")).thenReturn(true);
//
//    assertTrue(newsService.canEditNews(news, username));
//  }
//  @Test
//  public void shouldCantViewNewsWhenNotHavingAccess() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    when(newsService.getNewsById(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class), nullable(Boolean.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String newsId = "fakeId";
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, false)).thenReturn(news);
//    when(newsService.canViewNews(news, username)).thenReturn(false);
//
//    try {
//      newsService.getNewsById(newsId, false);
//      fail("should throw an exception when user can't access news");
//    } catch (IllegalAccessException e) {
//      // Expected
//    }
//  }
//  @Test
//  public void shouldCanViewNewsWhenHavingAccess() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    when(newsService.getNewsById(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class), nullable(Boolean.class))).thenCallRealMethod();
//    
//    String username = "mary";
//    String newsId = "fakeId";
//    
//    News news = new News();
//    when(newsService.getNewsById(newsId, false)).thenReturn(news);
//    when(newsService.canViewNews(news, username)).thenReturn(true);
//
//    assertEquals(news, newsService.getNewsById(newsId, false));
//  }
//  @Test
//  public void shouldCantEditNewsWhenNotHavingEditRignt() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    when(newsService.getNewsById(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class), nullable(Boolean.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String newsId = "fakeId";
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, true)).thenReturn(news);
//    when(newsService.canViewNews(news, username)).thenReturn(true);
//    when(newsService.canEditNews(news, username)).thenReturn(false);
//
//    try {
//      newsService.getNewsById(newsId, true);
//      fail("should throw an exception when user can't edit news");
//    } catch (IllegalAccessException e) {
//      // Expected
//    }
//  }
//  @Test
//  public void shouldCanEditNewsWhenHavingEditRignt() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    when(newsService.getNewsById(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class), nullable(Boolean.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String newsId = "fakeId";
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, true)).thenReturn(news);
//    when(newsService.canEditNews(news, username)).thenReturn(true);
//
//    assertEquals(news, newsService.getNewsById(newsId, true));
//  }
//  @Test
//  public void shouldCantAccessNewsWhenActivityNotFound() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//
//    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("mary");
//    ConversationState.setCurrent(new ConversationState(currentIdentity));
//    try {
//      newsService.getNewsByActivityId("1", currentIdentity);
//      fail("should throw an exception when activity isn't found");
//    } catch (ObjectNotFoundException e) {
//      // Expected
//    }
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCantAccessNewsWhenInaccessibleActivity() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class),nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String activityId = "fakeId";
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//
//    ExoSocialActivity activity = mock(ExoSocialActivity.class);
//
//    when(activityManager.getActivity(activityId)).thenReturn(activity);
//    when(activityManager.isActivityViewable(activity, identity)).thenReturn(false);
//    try {
//      newsService.getNewsByActivityId(activityId, identity);
//      fail("should throw an exception when activity isn't accessible for user");
//    } catch (IllegalAccessException e) {
//      // Expected
//    }
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCantAccessNewsWhenActivityIsntOfTypeNews() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//    
//    String username = "mary";
//    String activityId = "fakeId";
//    
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//    
//    ExoSocialActivity activity = mock(ExoSocialActivity.class);
//
//    when(activityManager.getActivity(activityId)).thenReturn(activity);
//    when(activityManager.isActivityViewable(activity, identity)).thenReturn(true);
//    try {
//      newsService.getNewsByActivityId(activityId, identity);
//      fail("should throw an exception when activity isn't of type news");
//    } catch (ObjectNotFoundException e) {
//      // Expected
//    }
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCanAccessNewsWhenActivityIsOfTypeNews() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String activityId = "fakeActivityId";
//    String newsId = "fakeNewsId";
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//
//    ExoSocialActivity activity = mock(ExoSocialActivity.class);
//
//    when(activityManager.getActivity(activityId)).thenReturn(activity);
//    when(activityManager.isActivityViewable(activity, identity)).thenReturn(true);
//
//    @SuppressWarnings("unchecked")
//    Map<String, String> templateParams = mock(Map.class);
//    when(activity.getTemplateParams()).thenReturn(templateParams);
//    when(templateParams.get("newsId")).thenReturn(newsId);
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, identity, false)).thenReturn(news);
//
//    assertEquals(news, newsService.getNewsByActivityId(activityId, identity));
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCanAccessNewsWhenSharedActivityIsOfTypeNews() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    FieldUtils.writeField(newsService, "identityManager", identityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String posterId = "3";
//    String poster = "james";
//    String activityId = "fakeActivityId";
//    String sharedActivityId = "fakeSharedActivityId";
//    String newsId = "fakeNewsId";
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//
//    ExoSocialActivity activity = mock(ExoSocialActivity.class);
//
//    when(activityManager.getActivity(activityId)).thenReturn(activity);
//    when(activityManager.isActivityViewable(activity, identity)).thenReturn(true);
//
//    @SuppressWarnings("unchecked")
//    Map<String, String> templateParams = mock(Map.class);
//    when(activity.getTemplateParams()).thenReturn(templateParams);
//    when(templateParams.get("originalActivityId")).thenReturn(sharedActivityId);
//
//    ExoSocialActivity sharedActivity = mock(ExoSocialActivity.class);
//    when(activityManager.getActivity(sharedActivityId)).thenReturn(sharedActivity);
//
//    when(activity.getPosterId()).thenReturn(posterId);
//    Identity posterIdentity = mock(Identity.class);
//    when(identityManager.getIdentity(posterId)).thenReturn(posterIdentity);
//    when(posterIdentity.getRemoteId()).thenReturn(poster);
//
//    org.exoplatform.services.security.Identity posterSecurityIdentity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(poster)).thenReturn(posterSecurityIdentity);
//
//    when(activityManager.isActivityViewable(sharedActivity, posterSecurityIdentity)).thenReturn(true);
//
//    @SuppressWarnings("unchecked")
//    Map<String, String> sharedTemplateParams = mock(Map.class);
//    when(sharedActivity.getTemplateParams()).thenReturn(sharedTemplateParams);
//    when(sharedTemplateParams.get("newsId")).thenReturn(newsId);
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, posterSecurityIdentity, false)).thenReturn(news);
//
//    assertEquals(news, newsService.getNewsByActivityId(activityId, identity));
//  }
//  @Test
//  @PrepareForTest({ ExoContainerContext.class })
//  public void shouldCantAccessNewsWhenSharedActivityIsntAccessibleForPoster() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    FieldUtils.writeField(newsService, "activityManager", activityManager, true);
//    FieldUtils.writeField(newsService, "identityManager", identityManager, true);
//    when(newsService.getNewsByActivityId(nullable(String.class), nullable(org.exoplatform.services.security.Identity.class))).thenCallRealMethod();
//
//    String username = "mary";
//    String posterId = "3";
//    String poster = "james";
//    String activityId = "fakeActivityId";
//    String sharedActivityId = "fakeSharedActivityId";
//    String newsId = "fakeNewsId";
//
//    IdentityRegistry identityRegistry = mock(IdentityRegistry.class);
//    PowerMockito.mockStatic(ExoContainerContext.class);
//    when(ExoContainerContext.getService(IdentityRegistry.class)).thenReturn(identityRegistry);
//    org.exoplatform.services.security.Identity identity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(username)).thenReturn(identity);
//
//    ExoSocialActivity activity = mock(ExoSocialActivity.class);
//
//    when(activityManager.getActivity(activityId)).thenReturn(activity);
//    when(activityManager.isActivityViewable(activity, identity)).thenReturn(true);
//
//    @SuppressWarnings("unchecked")
//    Map<String, String> templateParams = mock(Map.class);
//    when(activity.getTemplateParams()).thenReturn(templateParams);
//    when(templateParams.get("originalActivityId")).thenReturn(sharedActivityId);
//
//    ExoSocialActivity sharedActivity = mock(ExoSocialActivity.class);
//    when(activityManager.getActivity(sharedActivityId)).thenReturn(sharedActivity);
//
//    when(activity.getPosterId()).thenReturn(posterId);
//    Identity posterIdentity = mock(Identity.class);
//    when(identityManager.getIdentity(posterId)).thenReturn(posterIdentity);
//    when(posterIdentity.getRemoteId()).thenReturn(poster);
//
//    org.exoplatform.services.security.Identity posterSecurityIdentity = mock(org.exoplatform.services.security.Identity.class);
//    when(identityRegistry.getIdentity(poster)).thenReturn(posterSecurityIdentity);
//
//    // Poster doesn't have access anymore
//    when(activityManager.isActivityViewable(sharedActivity, posterSecurityIdentity)).thenReturn(false);
//
//    @SuppressWarnings("unchecked")
//    Map<String, String> sharedTemplateParams = mock(Map.class);
//    when(sharedActivity.getTemplateParams()).thenReturn(sharedTemplateParams);
//    when(sharedTemplateParams.get("newsId")).thenReturn(newsId);
//
//    News news = new News();
//    when(newsService.getNewsById(newsId, posterSecurityIdentity, false)).thenReturn(news);
//
//    try {
//      newsService.getNewsByActivityId(activityId, identity);
//      fail("User shouldn't be able to access news when shared activity poster can't access the news anymore");
//    } catch (IllegalAccessException e) {
//      // Expected
//    }
//  }
//  @Test
//  public void shouldCantShareNewsWhenNoAccess() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    doCallRealMethod().when(newsService).shareNews(nullable(News.class), nullable(Space.class), nullable(Identity.class), nullable(String.class));
//
//    String username = "mary";
//
//    News news = new News();
//    Identity identity = mock(Identity.class);
//    Space space = new Space();
//
//    when(identity.getRemoteId()).thenReturn(username);
//    when(newsService.canViewNews(news, username)).thenReturn(false);
//
//    try {
//      newsService.shareNews(news, space, identity, "activityId");
//      fail("Should throw an exception when user doesn't have access to news");
//    } catch (IllegalAccessException e) {
//      // Expected
//    }
//  }
//  @Test
//  @PrepareForTest({ SessionProvider.class })
//  public void shouldCantShareNewsWhenNotFound() throws Exception {
//    NewsServiceImpl newsService = mock(NewsServiceImpl.class);
//    doCallRealMethod().when(newsService).shareNews(nullable(News.class), nullable(Space.class), nullable(Identity.class), nullable(String.class));
//    PowerMockito.mockStatic(SessionProvider.class);
//    SessionProvider sessionProvider = mock(SessionProvider.class);
//    when(SessionProvider.createSystemProvider()).thenReturn(sessionProvider);
//
//    String username = "mary";
//    
//    News news = new News();
//    Identity identity = mock(Identity.class);
//    Space space = new Space();
//    
//    when(identity.getRemoteId()).thenReturn(username);
//    when(newsService.canViewNews(news, username)).thenReturn(true);
//
//    try {
//      newsService.shareNews(news, space, identity, "activityId");
//      fail("Should throw an exception when user doesn't have access to news");
//    } catch (ObjectNotFoundException e) {
//      // Expected
//    }
//  }

  @Test
  public void shouldSetPermissionForSharedSpaceWhenFound() throws Exception {
    JcrNewsStorage jcrNewsStorage = mock(JcrNewsStorage.class);
    doCallRealMethod().when(jcrNewsStorage).shareNews(nullable(News.class), nullable(Space.class), nullable(Identity.class), nullable(String.class));

    ExtendedNode newsNode = mock(ExtendedNode.class);
    ExtendedNode newsImageNode = mock(ExtendedNode.class);
    when(newsNode.canAddMixin("exo:privilegeable")).thenReturn(true);

    String newsId = "newsId";
    when(jcrNewsStorage.getNodeById(eq(newsId), nullable(SessionProvider.class))).thenReturn(newsNode);

//    String username = "mary";
    String spaceGroup = "spaceGroup";

    News news = mock(News.class);
    Identity identity = mock(Identity.class);
    Space space = mock(Space.class);

//    when(identity.getRemoteId()).thenReturn(username);
    when(space.getGroupId()).thenReturn(spaceGroup);
    when(news.getId()).thenReturn(newsId);
    String newsImageId = "newsImageId";
    when(jcrNewsStorage.getNodeById(eq(newsImageId), nullable(SessionProvider.class))).thenReturn(newsImageNode);

    String newsBody = "news body <img src=\"/portal/rest/images/session/" + newsImageId + "\" />";

    when(news.getBody()).thenReturn(newsBody);
    //when(newsService.canViewNews(news, username)).thenReturn(true);

    jcrNewsStorage.shareNews(news, space, identity, "activityId");
    verify(newsNode, atLeastOnce()).setPermission("*:" + spaceGroup, JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
    verify(newsImageNode, atLeastOnce()).setPermission("*:" + spaceGroup, JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
  }

  @Test
  public void shouldUpdateNodeAndKeepIllustrationWhenUpdatingNewsWithNullUploadId() throws Exception {
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    Node newsNode = mock(Node.class);
    Node illustrationNode = mock(Node.class);
    Property property = mock(Property.class);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NewsESSearchConnector.class)).thenReturn(null);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
    when(newsNode.getProperty(nullable(String.class))).thenReturn(property);
    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(imageProcessor.processImages(nullable(String.class), any(), nullable(String.class))).thenAnswer(i -> i.getArguments()[0]);
    when(newsNode.getName()).thenReturn("Updated title");

    News news = new News();
    news.setTitle("Updated title");
    news.setSummary("Updated summary");
    news.setBody("Updated body");
    news.setUploadId(null);
    news.setViewsCount((long) 10);

    // When
    jcrNewsStorage.updateNews(news, "user");

    // Then
    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body"));
    verify(newsNode, times(1)).setProperty(eq("exo:dateModified"), any(Calendar.class));
    verify(newsNode, times(1)).setProperty("exo:newsLastModifier", "user");

    verify(illustrationNode, times(0)).remove();
  }

  @Test
  public void shouldUpdateNodeAndRemoveIllustrationWhenUpdatingNewsWithEmptyUploadId() throws Exception {
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    Node newsNode = mock(Node.class);
    Node illustrationNode = mock(Node.class);
    Property property = mock(Property.class);
    COMMONS_UTILS.when(() -> CommonsUtils.getService(NewsESSearchConnector.class)).thenReturn(null);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
    when(newsNode.getProperty(nullable(String.class))).thenReturn(property);
    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(imageProcessor.processImages(nullable(String.class), any(), nullable(String.class))).thenAnswer(i -> i.getArguments()[0]);
    when(newsNode.getName()).thenReturn("Updated title");

    News news = new News();
    news.setTitle("Updated title");
    news.setSummary("Updated summary");
    news.setBody("Updated body");
    news.setUploadId("");
    news.setViewsCount((long) 10);

    // When
    jcrNewsStorage.updateNews(news, "user");

    // Then
    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body"));
    verify(newsNode, times(1)).setProperty(eq("exo:dateModified"), any(Calendar.class));
    verify(newsNode, times(1)).setProperty("exo:newsLastModifier", "user");
    verify(illustrationNode, times(1)).remove();
  }

  @Test
  public void shouldPublishNews() throws Exception {
    // Given

    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();

    JcrNewsStorage jcrNewsStorageSpy = Mockito.spy(jcrNewsStorage);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node publishedRootNode = mock(Node.class);
    Node newsFolderNode = mock(Node.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);

    News news = new News();
    news.setTitle("published title");
    news.setAudience("all");
    news.setSummary("published summary");
    news.setBody("published body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPublished(true);
    news.setId("id123");

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(jcrNewsStorageSpy).getNewsById("id123", false);
    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(publishedRootNode);
//TODO to be moved with newsService tests
//    Mockito.doNothing()
//           .when(jcrNewsStorageSpy)
//           .sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.PUBLISH_IN_NEWS, session);

    // When
    jcrNewsStorageSpy.publishNews(news);

    // Then
    verify(newsNode, times(1)).save();
    verify(newsNode, times(1)).setProperty(eq("exo:pinned"), eq(true));

    //
    ExtendedNode newsImageNode = mock(ExtendedNode.class);
    news.setBody("news body with image src=\"/portal/rest/images/repository/collaboration/123\"");
    when(jcrNewsStorageSpy.getNodeById("123", sessionProvider)).thenReturn(newsImageNode);
    when(newsImageNode.canAddMixin(EXO_PRIVILEGEABLE)).thenReturn(true);
    // When
    jcrNewsStorageSpy.publishNews(news);
    verify(newsImageNode, times(1)).setPermission("*:/platform/users", JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
    verify(newsImageNode, times(1)).save();

    //
    ExtendedNode existingUploadedNewsImageNode = mock(ExtendedNode.class);
    String nodePath = "Groups/spaces/test/testimage";
    String baseRestUrl = "https://exoplatform.com/portal/rest";
    REST_UTILS.when(() -> RestUtils.getBaseRestUrl()).thenReturn(baseRestUrl);
    news.setBody("news body with image src=\"https://exoplatform.com/portal/rest/jcr/repository/collaboration/Groups/spaces/test/testimage\"");
    when(session.getItem(nullable(String.class))).thenReturn(existingUploadedNewsImageNode);
    when(jcrNewsStorageSpy.getNodeByPath(nodePath, sessionProvider)).thenReturn(existingUploadedNewsImageNode);
    when(existingUploadedNewsImageNode.canAddMixin(EXO_PRIVILEGEABLE)).thenReturn(true);
    // When
    jcrNewsStorageSpy.publishNews(news);
    verify(existingUploadedNewsImageNode, times(1)).setPermission("*:/platform/users", JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
    verify(existingUploadedNewsImageNode, times(1)).save();
  }

  @Test
  public void shouldCreateNewsDraftAndPublishIt() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    JcrNewsStorage jcrNewsStorage = buildJcrNewsStorage();
    News news = new News();
    news.setTitle("new published news title");
    news.setSummary("new published news summary");
    news.setBody("new published news body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPublished(true);
    news.setSpaceId("spaceTest");
    news.setAuthor("root");
    News createdNewsDraft = new News();
    createdNewsDraft.setId("id123");
    createdNewsDraft.setPublished(true);

    Node node = mock(Node.class);
    Node spaceRootNode = mock(Node.class);
    Node spaceNewsRootNode = mock(Node.class);
    Node newsFolderNode = mock(Node.class);
    Node newsNode = mock(Node.class);
    Space space = mock(Space.class);
    Identity poster = mock(Identity.class);
    Identity spaceIdentity = mock(Identity.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(nullable(String.class))).thenReturn(node);
    when(spaceService.getSpaceById("spaceTest")).thenReturn(space);
    when(nodeHierarchyCreator.getJcrPath("groupsPath")).thenReturn("spaces");
    when(space.getGroupId()).thenReturn("spaceTest");
    when(session.getItem("spacesspaceTest")).thenReturn(spaceRootNode);
    when(spaceRootNode.hasNode("News")).thenReturn(true);
    when(spaceRootNode.getNode("News")).thenReturn(spaceNewsRootNode);
    when(dataDistributionType.getOrCreateDataNode(spaceNewsRootNode, "2019/8/22")).thenReturn(newsFolderNode);
    when(newsFolderNode.addNode(nullable(String.class), nullable(String.class))).thenReturn(newsNode);
    when(newsNode.getUUID()).thenReturn("id123");
    when(node.getProperty(nullable(String.class))).thenReturn(property);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(identityManager.getOrCreateIdentity("organization", "root", false)).thenReturn(poster);
    when(space.getPrettyName()).thenReturn("spaceTest");
    when(identityManager.getOrCreateIdentity("space", "spaceTest", false)).thenReturn(spaceIdentity);
    when(poster.getId()).thenReturn("root");
    JcrNewsStorage jcrNewsStorageSpy = Mockito.spy(jcrNewsStorage);

    Mockito.doNothing().when(jcrNewsStorageSpy).publishNews(createdNewsDraft);
    Mockito.doReturn(createdNewsDraft).when(jcrNewsStorageSpy).createNews(news);
    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, PublicationDefaultStates.PUBLISHED, new HashMap<>());
//TODO to be moved with newsService tests
//    Mockito.doNothing()
//           .when(newsServiceSpy)
//           .sendNotification(createdNewsDraft, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
    // When
    News createdNews = jcrNewsStorageSpy.createNews(news);

    // Then
    setRootAsCurrentIdentity();
    assertNotNull(createdNews);
  //TODO to be moved with newsService tests
//    verify(newsServiceSpy, times(1)).pinNews("id123");
  }
//  
//  @Test
//  public void shouldScheduleOrCancelNews() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setTitle("new scheduled news title");
//    news.setSummary("new scheduled news summary");
//    news.setBody("new scheduled news body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setSpaceId("spaceTest");
//    news.setAuthor("root");
//
//    News scheduledNews = new News();
//    String datePostScheduled = "06/18/2021 15:01:16 +0100";
//    scheduledNews.setId("id123");
//    scheduledNews.setSchedulePostDate(datePostScheduled);
//    scheduledNews.setPublicationState("staged");
//
//    News draftNews = new News();
//    draftNews.setPublicationState("draft");
//    draftNews.setSchedulePostDate(null);
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);
//    Mockito.doReturn(scheduledNews).when(newsServiceSpy).scheduleNews(news);
//    Mockito.doReturn(draftNews).when(newsServiceSpy).cancelScheduleNews(news);
//
//    // When
//    News scheduleNews = newsServiceSpy.scheduleNews(news);
//
//    // Then
//    assertNotNull(scheduleNews);
//    assertEquals("staged", scheduleNews.getPublicationState());
//    assertEquals("06/18/2021 15:01:16 +0100", scheduleNews.getSchedulePostDate());
//
//    // When
//    News cancelScheduleNews =  newsServiceSpy.cancelScheduleNews(news);
//    // Then
//    assertNotNull(cancelScheduleNews);
//    assertEquals("draft", cancelScheduleNews.getPublicationState());
//    assertNull(cancelScheduleNews.getSchedulePostDate());
//  }
//
//  @Test
//  public void shouldUnPublishNews() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//    Node publishRootNode = mock(Node.class);
//    Node newsFolderNode = mock(Node.class);
//    Node applicationDataNode = mock(Node.class);
//    Node newsRootNode = mock(Node.class);
//    Node publishedNode = mock(Node.class);
//    Property property = mock(Property.class);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setSummary("unpublished summary");
//    news.setBody("unpublished body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(property.getString()).thenReturn("");
//    when(newsNode.getProperty(anyString())).thenReturn(property);
//    when(dataDistributionType.getOrCreateDataNode(any(Node.class), nullable(String.class))).thenReturn(newsFolderNode);
//    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
//    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
//    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
//    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(publishRootNode);
//    when(newsFolderNode.getNode("unpinned")).thenReturn(publishedNode);
//    when(newsNode.getName()).thenReturn("unpinned");
//    // When
//    newsServiceSpy.unpinNews("id123");
//
//    // Then
//    verify(newsNode, times(1)).save();
//    verify(newsNode, times(1)).setProperty(eq("exo:pinned"), eq(false));
//    verify(publishedNode, times(1)).remove();
//    verify(newsFolderNode, times(1)).save();
//
//  }
//
//  @Test
//  public void shouldNotUnPublishNewsAsNewsNodeIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setSummary("unpublished summary");
//    news.setBody("unpublished body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(null);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find a node with an UUID equal to: id123");
//
//    newsServiceSpy.unpinNews("id123");
//
//  }
//
//  @Test
//  public void shouldNotUnPublishNewsAsNewsIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    Mockito.doReturn(null).when(newsServiceSpy).getNewsById("id123", false);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find a news with an id equal to: id123");
//
//    newsServiceSpy.unpinNews("id123");
//
//  }
//
//  @Test
//  public void shouldNotUnPublishNewsAsPublishedRootNodeIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//    Node newsFolderNode = mock(Node.class);
//    Node applicationDataNode = mock(Node.class);
//    Node newsRootNode = mock(Node.class);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setSummary("unpublished summary");
//    news.setBody("unpublished body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(dataDistributionType.getOrCreateDataNode(any(Node.class), nullable(String.class))).thenReturn(newsFolderNode);
//    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
//    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
//    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
//    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(null);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find the root pinned folder: /Application Data/News/pinned");
//
//    newsServiceSpy.unpinNews("id123");
//
//  }
//
//  @Test
//  public void shouldNotUnPublishNewsAsNewsFolderNodeIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//    Node applicationDataNode = mock(Node.class);
//    Node newsRootNode = mock(Node.class);
//    Node publishedRootNode = mock(Node.class);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setSummary("unpublished summary");
//    news.setBody("unpublished body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(dataDistributionType.getOrCreateDataNode(any(Node.class), nullable(String.class))).thenReturn(null);
//    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
//    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
//    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
//    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(publishedRootNode);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find the parent node of the current pinned node");
//
//    newsServiceSpy.unpinNews("id123");
//
//  }
//
//  @Test
//  public void shouldNotUnPublishedNewsAsPublishedNodeIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    NewsService newsServiceSpy = Mockito.spy(newsService);
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//    Node applicationDataNode = mock(Node.class);
//    Node newsRootNode = mock(Node.class);
//    Node publishedRootNode = mock(Node.class);
//    Node newsFolderNode = mock(Node.class);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setSummary("unpublished summary");
//    news.setBody("unpublished body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(dataDistributionType.getOrCreateDataNode(any(Node.class), nullable(String.class))).thenReturn(newsFolderNode);
//    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
//    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123", false);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
//    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
//    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
//    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(publishedRootNode);
//    when(newsFolderNode.getNode("unpinned")).thenReturn(null);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find the current pinned node");
//
//    newsServiceSpy.unpinNews("id123");
//
//  }
//
//  @Test
//  public void shouldCreateNewsDraft() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    Node newsFolderNode = mock(Node.class);
//    Node newsRootNode = mock(Node.class);
//    Node applicationDataNode = mock(Node.class);
//    Node draftNode = mock(Node.class);
//    draftNode.setProperty("id", "1");
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setPrettyName("space1");
//    space1.setGroupId("space1");
//
//    News news = new News();
//    news.setTitle("title");
//    news.setSummary("summary");
//    news.setBody("body");
//    news.setAuthor("john");
//    String sDate1 = "10/09/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setId("id123");
//    news.setSpaceId("1");
//    Lifecycle newsLifecycle = new Lifecycle();
//    newsLifecycle.setName("newsLifecycle");
//    newsLifecycle.setPublicationPlugin("Authoring publication");
//    // authoringPublicationPlugin.setName("Authoring publication");
//    Map<String, WebpagePublicationPlugin> publicationPlugins = new HashMap<>();
//    publicationPlugins.put("Authoring publication", authoringPublicationPlugin);
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(dataDistributionType.getOrCreateDataNode(any(Node.class), nullable(String.class))).thenReturn(newsFolderNode);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
//    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
//    when(nodeHierarchyCreator.getJcrPath(nullable(String.class))).thenReturn("/Groups/");
//    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space1);
//    when(newsFolderNode.addNode(nullable(String.class), nullable(String.class))).thenReturn(draftNode);
//    when(publicationManagerImpl.getLifecycle(nullable(String.class))).thenReturn(newsLifecycle);
//    when(wcmPublicationServiceImpl.getWebpagePublicationPlugins()).thenReturn(publicationPlugins);
//    when(imageProcessor.processImages(news.getBody(), draftNode.getUUID(), "images")).thenReturn("");
//    when(draftNode.canAddMixin(nullable(String.class))).thenReturn(true);
//
//    // When
//    newsService.createNewsDraft(news);
//
//    // Then
//    verify(newsRootNode, times(1)).save();
//    verify(draftNode, times(1)).setProperty(eq("exo:title"), eq(news.getTitle()));
//    verify(draftNode, times(1)).setProperty(eq("exo:summary"), eq(news.getSummary()));
//    verify(draftNode, times(1)).setProperty(eq("exo:body"), eq(news.getBody()));
//    verify(draftNode, times(1)).setProperty(eq("exo:author"), eq(news.getAuthor()));
//    verify(draftNode, times(1)).setProperty(eq("exo:spaceId"), eq(news.getSpaceId()));
//    verify(draftNode, times(1)).addMixin(eq("exo:datetime"));
//    //
//    verify(draftNode, times(1)).setProperty(eq("exo:body"), eq(""));
//    verify(draftNode, times(1)).setProperty(eq("id"), eq("1"));
//  }
//
//  @PrepareForTest(WCMCoreUtils.class)
//  @Test
//  public void shouldDeleteNewsWithId() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    Node applicationDataNode = mock(Node.class);
//    Node newsNode = mock(Node.class);
//    newsNode.setProperty("id", "1");
//    Property exoActivitiesProperty = mock(Property.class);
//    
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setPrettyName("space1");
//    space1.setGroupId("space1");
//
//    News news = new News();
//    news.setTitle("title");
//    news.setSummary("summary");
//    news.setBody("body");
//    news.setAuthor("john");
//    String sDate1 = "10/09/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setId("id123");
//    news.setSpaceId("1");
//
//    PowerMockito.mockStatic(WCMCoreUtils.class);
//    TrashService trashService = mock(TrashService.class);
//    when(trashService.isInTrash(newsNode)).thenReturn(false);
//    when(WCMCoreUtils.getService(TrashService.class)).thenReturn(trashService);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    Property prop = mock(Property.class);
//    when(prop.getString()).thenReturn("");
//    when(newsNode.getProperty("publication:currentState")).thenReturn(prop);
//    ExoSocialActivity activity = new ExoSocialActivityImpl();
//    activity.setId("1");
//    activity.isHidden(false);
//    activityManager.saveActivityNoReturn(activity);
//
//    when(newsNode.hasProperty("exo:activities")).thenReturn(true);
//    when(newsNode.getProperty("exo:activities")).thenReturn(exoActivitiesProperty);
//    when(exoActivitiesProperty.getString()).thenReturn("1:1;2:2;2:3");
//    when(activityManager.getActivity("1")).thenReturn(activity);
//
//    // When
//    newsService.deleteNews("1", "root", false);
//
//    // Then
//    verify(session, times(1)).getNodeByUUID(nullable(String.class));
//    verify(session, times(1)).save();
//    verify(newsNode, times(1)).remove();
//    verify(activityManager, times(3)).deleteActivity(nullable(String.class));
//  }
//
//  @Test
//  public void shouldCreateNewsDraftWhenNewsHasNoId() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    Node applicationDataNode = mock(Node.class);
//    Node newsNode = mock(Node.class);
//    newsNode.setProperty("id", "1");
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setPrettyName("space1");
//    space1.setGroupId("space1");
//
//    News news = new News();
//    news.setTitle("title");
//    news.setSummary("summary");
//    news.setBody("body");
//    news.setAuthor("john");
//    String sDate1 = "10/09/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setSpaceId("1");
//    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    Mockito.doReturn(news).when(newsServiceSpy).createNewsDraft(news);
//    Mockito.doNothing().when(newsServiceSpy).postNewsActivity(news, session);
//    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, PublicationDefaultStates.PUBLISHED, new HashMap<>());
//    Mockito.doNothing().when(newsServiceSpy).sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//
//    // When
//    newsServiceSpy.createNews(news);
//
//    // Then
//    verify(newsServiceSpy, times(1)).createNews(news);
//    verify(newsServiceSpy, times(0)).updateNews(news);
//  }
//
//  @Test
//  public void shouldUpdateNewsDraftWhenNewsHasId() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    Node applicationDataNode = mock(Node.class);
//    Node newsNode = mock(Node.class);
//    newsNode.setProperty("id", "1");
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setPrettyName("space1");
//    space1.setGroupId("space1");
//
//    News news = new News();
//    news.setId("1");
//    news.setTitle("title");
//    news.setSummary("summary");
//    news.setBody("body");
//    news.setActivities("1:38");
//    news.setAuthor("john");
//    String sDate1 = "10/09/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setSpaceId("1");
//    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getItem(nullable(String.class))).thenReturn(applicationDataNode);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(spaceService.getSpaceById("1")).thenReturn(space1);
//    Mockito.doReturn(news).when(newsServiceSpy).updateNews(news);
//    Mockito.doNothing().when(newsServiceSpy).postNewsActivity(news, session);
//    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, PublicationDefaultStates.PUBLISHED, new HashMap<>());
//    Mockito.doNothing().when(newsServiceSpy).sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//
//    // When
//    newsServiceSpy.createNews(news);
//
//    // Then
//    verify(newsServiceSpy, times(1)).updateNews(news);
//    verify(newsServiceSpy, times(1)).sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//  }
//
//  @Test
//  public void shouldGetAllNewsNodesWhenExistsForPublisherUser() throws Exception {
//    // Given
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    QueryManager qm = mock(QueryManager.class);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//    when(workSpace.getQueryManager()).thenReturn(qm);
//    QueryImpl query = mock(QueryImpl.class);
//    when(qm.createQuery(nullable(String.class), nullable(String.class))).thenReturn(query);
//    QueryResult queryResult = mock(QueryResult.class);
//    when(query.execute()).thenReturn(queryResult);
//    NodeIterator it = mock(NodeIterator.class);
//    when(queryResult.getNodes()).thenReturn(it);
//    when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
//
//    Node node1 = mock(Node.class);
//    Node node2 = mock(Node.class);
//    Node node3 = mock(Node.class);
//
//    when(node1.getSession()).thenReturn(session);
//    when(node2.getSession()).thenReturn(session);
//    when(node3.getSession()).thenReturn(session);
//    when(it.nextNode()).thenReturn(node1).thenReturn(node2).thenReturn(node3);
//    Property property = mock(Property.class);
//    when(node1.getProperty(nullable(String.class))).thenReturn(property);
//    when(node2.getProperty(nullable(String.class))).thenReturn(property);
//    when(node3.getProperty(nullable(String.class))).thenReturn(property);
//    when(property.toString()).thenReturn("news ");
//    when(property.getString()).thenReturn("");
//    when(property.getDate()).thenReturn(Calendar.getInstance());
//    when(property.getBoolean()).thenReturn(true);
//    when(property.getLong()).thenReturn((long) 10);
//    when(node1.hasNode("illustration")).thenReturn(false);
//    when(node2.hasNode("illustration")).thenReturn(false);
//    when(node3.hasNode("illustration")).thenReturn(false);
//    Space space = mock(Space.class);
//    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
//    when(space.getDisplayName()).thenReturn("Test news space");
//    when(space.getGroupId()).thenReturn("/spaces/test_news_space");
//    Identity poster = mock(Identity.class);
//    when(identityManager.getOrCreateIdentity(nullable(String.class), nullable(String.class), anyBoolean())).thenReturn(poster);
//    setCurrentIdentity();
//    when(spaceService.isSuperManager("user")).thenReturn(false);
//    when(space.getVisibility()).thenReturn("private");
//    Profile p1 = new Profile(poster);
//    p1.setProperty("fullName", "test test");
//    NewsFilter newsFilter = new NewsFilter();
//
//    when(poster.getProfile()).thenReturn(p1);
//
//    // When
//    List<News> newsList = newsService.getNews(newsFilter);
//
//    // Then
//    assertNotNull(newsList);
//    assertEquals(3, newsList.size());
//    verify(it, times(4)).hasNext();
//    verify(it, times(3)).nextNode();
//  }
//
//  @Test
//  public void shouldGetNoNewsNodesWhenDoesNotExists() throws Exception {
//    // Given
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    QueryManager qm = mock(QueryManager.class);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//    when(workSpace.getQueryManager()).thenReturn(qm);
//    QueryImpl query = mock(QueryImpl.class);
//    when(qm.createQuery(nullable(String.class), nullable(String.class))).thenReturn(query);
//    QueryResult queryResult = mock(QueryResult.class);
//    when(query.execute()).thenReturn(queryResult);
//    NodeIterator it = mock(NodeIterator.class);
//    when(queryResult.getNodes()).thenReturn(it);
//    when(it.hasNext()).thenReturn(false);
//    NewsFilter newsFilter = new NewsFilter();
//
//    // When
//    List<News> newsList = newsService.getNews(newsFilter);
//
//    // Then
//    assertNotNull(newsList);
//    assertEquals(0, newsList.size());
//    verify(it, times(1)).hasNext();
//    verify(it, times(0)).nextNode();
//  }
//
//  @Test
//  public void shouldIncrementViewsCountWhenUserIsNotInNewsViewers() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setId("id123");
//    news.setViewsCount((long) 5);
//    Node newsNode = mock(Node.class);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    Property property = mock(Property.class);
//    when(newsNode.hasNode("exo:viewers")).thenReturn(true);
//    when(newsNode.getProperty("exo:viewers")).thenReturn(property);
//    when(property.getString()).thenReturn("david,test,hedi");
//
//    // When
//    newsService.markAsRead(news, "root");
//
//    // Then
//    assertEquals(Long.valueOf(6), news.getViewsCount());
//    verify(newsNode, times(1)).setProperty("exo:viewsCount", (long) 6);
//    verify(newsNode, times(1)).setProperty("exo:viewers", "david,test,hedi,root");
//    verify(newsNode, times(1)).save();
//  }
//
//  @Test
//  public void shouldGetExceptionWhenNewsDoesNotExists() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setId("id123");
//    news.setViewsCount((long) 5);
//    when(session.getNodeByUUID("id123")).thenReturn(null);
//    exceptionRule.expect(Exception.class);
//    exceptionRule.expectMessage("Unable to find a node with an UUID equal to: id123");
//
//    // When
//    newsService.markAsRead(news, "root");
//  }
//
//  @Test
//  public void shouldNotIncrementViewsCountWhenUserIsInNewsViewers() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//            indexingService, newsESSearchConnector, userACL);
//    News news = new News();
//    news.setId("id123");
//    news.setViewsCount((long) 5);
//    Node newsNode = mock(Node.class);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    Property property = mock(Property.class);
//    when(newsNode.hasNode("exo:viewers")).thenReturn(true);
//    when(newsNode.getProperty("exo:viewers")).thenReturn(property);
//    when(property.getString()).thenReturn("david,test,hedi");
//
//    // When
//    newsService.markAsRead(news, "hedi");
//
//    // Then
//    assertEquals(Long.valueOf(5), news.getViewsCount());
//  }
//
//  @Test
//  public void shouldIncrementViewsCountWhenNewsViewersIsEmpty() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setId("id123");
//    news.setViewsCount((long) 5);
//    Node newsNode = mock(Node.class);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    Property property = mock(Property.class);
//    when(newsNode.hasNode("exo:viewers")).thenReturn(true);
//    when(newsNode.getProperty("exo:viewers")).thenReturn(property);
//    when(property.getString()).thenReturn("");
//
//    // When
//    newsService.markAsRead(news, "root");
//
//    // Then
//    assertEquals(Long.valueOf(6), news.getViewsCount());
//    verify(newsNode, times(1)).setProperty("exo:viewsCount", (long) 6);
//    verify(newsNode, times(1)).setProperty("exo:viewers", "root");
//    verify(newsNode, times(1)).save();
//  }
//
//  @Test
//  public void shouldIncrementViewsCountWhenNewsViewersIsEmptyAndViewsCountIsNull() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setId("id123");
//    Node newsNode = mock(Node.class);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    Property property = mock(Property.class);
//    when(newsNode.hasNode("exo:viewers")).thenReturn(true);
//    when(newsNode.getProperty("exo:viewers")).thenReturn(property);
//    when(property.getString()).thenReturn("");
//
//    // When
//    newsService.markAsRead(news, "root");
//
//    // Then
//    assertEquals(Long.valueOf(1), news.getViewsCount());
//    verify(newsNode, times(1)).setProperty("exo:viewsCount", (long) 1);
//    verify(newsNode, times(1)).setProperty("exo:viewers", "root");
//    verify(newsNode, times(1)).save();
//  }
//
//  @Test
//  public void shouldNotAuthorizeTheCurrentUserToPublishNews() {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("user");
//    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "member");
//    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
//    memberships.add(membershipentry);
//    currentIdentity.setMemberships(memberships);
//    ConversationState state = new ConversationState(currentIdentity);
//    ConversationState.setCurrent(state);
//
//    // When
//    boolean canPublishNews = newsService.canPublishNews();
//
//    // Then
//    assertEquals(false, canPublishNews);
//
//  }
//
//  @Test
//  public void shouldAuthorizeTheCurrentUserToPublishNews() {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    setCurrentIdentity();
//
//    // When
//    boolean canPublishNews = newsService.canPublishNews();
//
//    // Then
//    assertEquals(true, canPublishNews);
//
//  }
//
//  @Test
//  public void shouldPostNewsActivity() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setAuthor("root");
//    news.setSpaceId("1");
//    news.setId("id123");
//    news.setActivityPosted(true);
//    Identity poster = new Identity("root");
//    Identity spaceIdentity = new Identity("1");
//    spaceIdentity.setRemoteId("space1");
//    spaceIdentity.setProviderId("space");
//    Space space = new Space();
//    space.setId("1");
//    space.setPrettyName("space1");
//    when(identityManager.getOrCreateIdentity("organization", "root", false)).thenReturn(poster);
//    when(identityManager.getOrCreateIdentity("space", "space1", false)).thenReturn(spaceIdentity);
//    when(spaceService.getSpaceById("1")).thenReturn(space);
//
//    // When
//    newsService.postNewsActivity(news, session);
//
//    // Then
//    ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);
//    ArgumentCaptor<ExoSocialActivity> activityCaptor = ArgumentCaptor.forClass(ExoSocialActivity.class);
//    verify(activityManager, times(1)).saveActivityNoReturn(identityCaptor.capture(), activityCaptor.capture());
//    Identity identityCaptorValue = identityCaptor.getValue();
//    assertEquals(SpaceIdentityProvider.NAME, identityCaptorValue.getProviderId());
//    assertEquals("space1", identityCaptorValue.getRemoteId());
//    ExoSocialActivity activityCaptorValue = activityCaptor.getValue();
//    assertEquals("news", activityCaptorValue.getType());
//    assertEquals(1, activityCaptorValue.getTemplateParams().size());
//    assertEquals("id123", activityCaptorValue.getTemplateParams().get("newsId"));
//  }
//
//  @Test
//  public void shouldUpdateNewsActivities() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    News news = new News();
//    news.setAuthor("root");
//    news.setSpaceId("1");
//    news.setId("id123");
//    ExoSocialActivity activity = new ExoSocialActivityImpl();
//    activity.setId("38");
//    Identity poster = new Identity("root");
//    Identity spaceIdentity = new Identity("1");
//    spaceIdentity.setRemoteId("space1");
//    spaceIdentity.setProviderId("space");
//    Space space = new Space();
//    space.setId("1");
//    space.setPrettyName("space1");
//    Node newsNode = mock(Node.class);
//    when(identityManager.getOrCreateIdentity("organization", "root", false)).thenReturn(poster);
//    when(identityManager.getOrCreateIdentity("space", "space1", false)).thenReturn(spaceIdentity);
//    when(spaceService.getSpaceById("1")).thenReturn(space);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    when(newsNode.hasProperty("exo:activities")).thenReturn(true);
//    // When
//    newsService.updateNewsActivities(activity, news, session);
//
//    // Then
//    assertEquals("1:38", news.getActivities());
//    verify(newsNode, times(1)).setProperty("exo:activities", "1:38");
//    verify(newsNode, times(1)).save();
//  }
//
//  @Test
//  public void shouldGetAllNewsDraftNodesWhenExists() throws Exception {
//    // Given
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    QueryManager qm = mock(QueryManager.class);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//    when(workSpace.getQueryManager()).thenReturn(qm);
//    QueryImpl query = mock(QueryImpl.class);
//    when(qm.createQuery(nullable(String.class), nullable(String.class))).thenReturn(query);
//    QueryResult queryResult = mock(QueryResult.class);
//    when(query.execute()).thenReturn(queryResult);
//    NodeIterator it = mock(NodeIterator.class);
//    when(queryResult.getNodes()).thenReturn(it);
//    when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
//
//    Node node1 = mock(Node.class);
//    Node node2 = mock(Node.class);
//    Node node3 = mock(Node.class);
//
//    when(node1.getSession()).thenReturn(session);
//    when(node2.getSession()).thenReturn(session);
//    when(node3.getSession()).thenReturn(session);
//    when(it.nextNode()).thenReturn(node1).thenReturn(node2).thenReturn(node3);
//    Property property1 = mock(Property.class);
//    Property property2 = mock(Property.class);
//    Property property3 = mock(Property.class);
//    Property property4 = mock(Property.class);
//    when(node1.getProperty("exo:title")).thenReturn(property1);
//    when(node1.hasProperty("exo:title")).thenReturn(true);
//    when(node2.getProperty("exo:title")).thenReturn(property2);
//    when(node2.hasProperty("exo:title")).thenReturn(true);
//    when(node3.getProperty("exo:title")).thenReturn(property3);
//    when(node3.hasProperty("exo:title")).thenReturn(true);
//    when(node1.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//    when(node2.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//    when(node3.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//    when(property1.getString()).thenReturn("title1");
//    when(property2.getString()).thenReturn("title2");
//    when(property3.getString()).thenReturn("title3");
//    when(property4.getString()).thenReturn("");
//    when(property1.getDate()).thenReturn(Calendar.getInstance());
//    when(property2.getDate()).thenReturn(Calendar.getInstance());
//    when(property3.getDate()).thenReturn(Calendar.getInstance());
//    when(property4.getString()).thenReturn("");
//    when(node1.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//    when(node2.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//    when(node3.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)).thenReturn(property4);
//
//    when(node1.getProperty("exo:pinned")).thenReturn(property1);
//    when(node2.getProperty("exo:pinned")).thenReturn(property2);
//    when(node3.getProperty("exo:pinned")).thenReturn(property3);
//    when(node1.getProperty("exo:spaceId")).thenReturn(property1);
//    when(node2.getProperty("exo:spaceId")).thenReturn(property2);
//    when(node3.getProperty("exo:spaceId")).thenReturn(property3);
//    when(node1.hasProperty("exo:activities")).thenReturn(false);
//    when(node2.hasProperty("exo:activities")).thenReturn(false);
//    when(node3.hasProperty("exo:activities")).thenReturn(false);
//    when(node1.hasProperty("exo:viewsCount")).thenReturn(false);
//    when(node2.hasProperty("exo:viewsCount")).thenReturn(false);
//    when(node3.hasProperty("exo:viewsCount")).thenReturn(false);
//    when(property1.getBoolean()).thenReturn(true);
//    when(property2.getBoolean()).thenReturn(true);
//    when(property3.getBoolean()).thenReturn(true);
//    when(property1.getLong()).thenReturn((long) 10);
//    when(property2.getLong()).thenReturn((long) 10);
//    when(property3.getLong()).thenReturn((long) 10);
//    when(node1.hasNode("illustration")).thenReturn(false);
//    when(node2.hasNode("illustration")).thenReturn(false);
//    when(node3.hasNode("illustration")).thenReturn(false);
//    Space space = mock(Space.class);
//    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
//    when(space.getDisplayName()).thenReturn("Test news space");
//    when(space.getGroupId()).thenReturn("/spaces/test_news_space");
//    Identity poster = mock(Identity.class);
//    when(identityManager.getOrCreateIdentity(nullable(String.class), nullable(String.class), anyBoolean())).thenReturn(poster);
//    when(space.getVisibility()).thenReturn("private");
//    when(spaceService.isMember(nullable(String.class), nullable(String.class))).thenReturn(false);
//    when(spaceService.isSuperManager(nullable(String.class))).thenReturn(false);
//    Profile p1 = new Profile(poster);
//    p1.setProperty("fullName", "root root");
//
//    when(poster.getProfile()).thenReturn(p1);
//    // When
//    List<String> spaces = new ArrayList<>();
//    spaces.add("1");
//    NewsFilter newsFilter = new NewsFilter();
//    newsFilter.setAuthor("root");
//    newsFilter.setDraftNews(true);
//    newsFilter.setSpaces(spaces);
//    setRootAsCurrentIdentity();
//    List<News> newsList = newsService.getNews(newsFilter);
//
//    // Then
//    assertNotNull(newsList);
//    assertEquals(3, newsList.size());
//    verify(it, times(4)).hasNext();
//    verify(it, times(3)).nextNode();
//    assertEquals("title1", newsList.get(0).getTitle());
//    assertEquals("title2", newsList.get(1).getTitle());
//    assertEquals("title3", newsList.get(2).getTitle());
//  }
//
//  @Test
//  public void shouldNotSendNotificationAsContentSpaceIsNull() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setAuthor("root");
//    news.setSpaceId("1");
//    news.setActivities("1:38");
//    when(spaceService.getSpaceById("1")).thenReturn(null);
//    exceptionRule.expect(NullPointerException.class);
//    exceptionRule.expectMessage("Cannot find a space with id 1, it may not exist");
//
//    newsService.sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//
//  }
//
//  @PrepareForTest({ CommonsUtils.class })
//  @Test
//  public void shouldNotSendNotificationAsNewsNodeDoesNotExist() throws Exception {
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//    Identity rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
//    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(rootIdentity);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    News news = new News();
//    news.setTitle("unpublished");
//    news.setAuthor("root");
//    news.setId("id123");
//    news.setSpaceId("1");
//    news.setActivities("1:38");
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setPrettyName("space1");
//    space1.setGroupId("space1");
//    space1.setVisibility("private");
//    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("jean");
//    ConversationState state = new ConversationState(currentIdentity);
//    ConversationState.setCurrent(state);
//
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(spaceService.getSpaceById("1")).thenReturn(space1);
//    when(spaceService.isMember(space1, "root")).thenReturn(true);
//    when(session.getNodeByUUID("id123")).thenReturn(null);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    when(CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
//    when(CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
//    when(CommonsUtils.getCurrentDomain()).thenReturn("http://localhost:8080");
//    exceptionRule.expect(ItemNotFoundException.class);
//    exceptionRule.expectMessage("Cannot find a node with UUID equals to id123, it may not exist");
//
//    newsService.sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//
//  }
//
//  @PrepareForTest({ LinkProvider.class, NotificationContextImpl.class, PluginKey.class, PropertyManager.class,
//      CommonsUtils.class })
//  @Test
//  public void shouldSendNotificationForPostNewsContext() throws Exception {
//    // Given
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    News news = new News();
//    news.setTitle("title");
//    news.setBody("body");
//    news.setAuthor("root");
//    news.setId("id123");
//    news.setSpaceId("1");
//    news.setActivities("1:38");
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setDisplayName("space1");
//    space1.setGroupId("space1");
//    space1.setVisibility("private");
//
//    Identity rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
//    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(rootIdentity);
//
//    Node newsNode = mock(Node.class);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(spaceService.getSpaceById("1")).thenReturn(space1);
//    when(spaceService.isMember(space1, "root")).thenReturn(true);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    when(CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
//    when(CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
//    when(CommonsUtils.getCurrentDomain()).thenReturn("http://localhost:8080");
//    when(newsNode.hasNode("illustration")).thenReturn(true);
//
//    PowerMockito.mockStatic(LinkProvider.class);
//    when(LinkProvider.getSingleActivityUrl("38")).thenReturn("portal/intranet/activity?id=38");
//    PowerMockito.mockStatic(NotificationContextImpl.class);
//    NotificationContext ctx = mock(NotificationContext.class);
//    NotificationExecutor executor = mock(NotificationExecutor.class);
//    ArgumentLiteral<String> CONTENT_TITLE = new ArgumentLiteral<String>(String.class, "CONTENT_TITLE");
//
//    ArgumentLiteral<String> CONTENT_AUTHOR = new ArgumentLiteral<String>(String.class, "CONTENT_AUTHOR");
//
//    ArgumentLiteral<String> CONTENT_SPACE = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE");
//
//    ArgumentLiteral<String> CONTENT_SPACE_ID = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE_ID");
//
//    ArgumentLiteral<String> ILLUSTRATION_URL = new ArgumentLiteral<String>(String.class, "ILLUSTRATION_URL");
//
//    ArgumentLiteral<String> AUTHOR_AVATAR_URL = new ArgumentLiteral<String>(String.class, "AUTHOR_AVATAR_URL");
//
//    ArgumentLiteral<String> ACTIVITY_LINK = new ArgumentLiteral<String>(String.class, "ACTIVITY_LINK");
//
//    ArgumentLiteral<String> NEWS_ID = new ArgumentLiteral<String>(String.class, "NEWS_ID");
//
//    ArgumentLiteral<NotificationConstants.NOTIFICATION_CONTEXT> CONTEXT =
//                                                                        new ArgumentLiteral<NotificationConstants.NOTIFICATION_CONTEXT>(NotificationConstants.NOTIFICATION_CONTEXT.class,
//                                                                                                                                        "CONTEXT");
//
//    when(NotificationContextImpl.cloneInstance()).thenReturn(ctx);
//    when(ctx.append(CONTENT_TITLE, "title")).thenReturn(ctx);
//    when(ctx.append(CONTENT_AUTHOR, "root")).thenReturn(ctx);
//    when(ctx.append(CONTENT_SPACE_ID, "1")).thenReturn(ctx);
//    when(ctx.append(CONTENT_SPACE, "space1")).thenReturn(ctx);
//    when(ctx.append(ILLUSTRATION_URL, "http://localhost:8080/portal/rest/v1/news/id123/illustration")).thenReturn(ctx);
//    when(ctx.append(AUTHOR_AVATAR_URL, "http://localhost:8080/eXoSkin/skin/images/avatar/DefaultUserAvatar.png")).thenReturn(ctx);
//    when(ctx.append(eq(ACTIVITY_LINK), any(String.class))).thenReturn(ctx);
//    when(ctx.append(NEWS_ID, "id123")).thenReturn(ctx);
//    when(ctx.append(CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS)).thenReturn(ctx);
//
//    when(ctx.getNotificationExecutor()).thenReturn(executor);
//    PowerMockito.mockStatic(PluginKey.class);
//    PluginKey plugin = mock(PluginKey.class);
//    when(PluginKey.key("PostNewsNotificationPlugin")).thenReturn(plugin);
//    NotificationCommand notificationCommand = mock(NotificationCommand.class);
//    when(ctx.makeCommand(plugin)).thenReturn(notificationCommand);
//    NotificationExecutor notificationExecutor = mock(NotificationExecutor.class);
//    when(executor.with(notificationCommand)).thenReturn(notificationExecutor);
//    when(notificationExecutor.execute(ctx)).thenReturn(true);
//    setRootAsCurrentIdentity();
//
//    // When
//    newsService.sendNotification(news, NotificationConstants.NOTIFICATION_CONTEXT.POST_NEWS, session);
//
//    // Then
//    verify(notificationExecutor, times(1)).execute(ctx);
//
//  }
//
//  @Test
//  public void shouldGetAllPubishedNewsNodesWhenExists() throws Exception {
//    // Given
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    QueryManager qm = mock(QueryManager.class);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//    when(workSpace.getQueryManager()).thenReturn(qm);
//    QueryImpl query = mock(QueryImpl.class);
//    when(qm.createQuery(nullable(String.class), nullable(String.class))).thenReturn(query);
//    QueryResult queryResult = mock(QueryResult.class);
//    when(query.execute()).thenReturn(queryResult);
//    NodeIterator it = mock(NodeIterator.class);
//    when(queryResult.getNodes()).thenReturn(it);
//    when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
//
//    Node node1 = mock(Node.class);
//    Node node2 = mock(Node.class);
//    Node node3 = mock(Node.class);
//
//    when(node1.getSession()).thenReturn(session);
//    when(node2.getSession()).thenReturn(session);
//    when(node3.getSession()).thenReturn(session);
//    when(it.nextNode()).thenReturn(node1).thenReturn(node2).thenReturn(node3);
//    Property property = mock(Property.class);
//    when(node1.getProperty(nullable(String.class))).thenReturn(property);
//    when(node2.getProperty(nullable(String.class))).thenReturn(property);
//    when(node3.getProperty(nullable(String.class))).thenReturn(property);
//    when(property.toString()).thenReturn("news ");
//    when(property.getString()).thenReturn("");
//    when(property.getDate()).thenReturn(Calendar.getInstance());
//    when(property.getBoolean()).thenReturn(true);
//    when(property.getLong()).thenReturn((long) 10);
//    when(node1.hasNode("illustration")).thenReturn(false);
//    when(node2.hasNode("illustration")).thenReturn(false);
//    when(node3.hasNode("illustration")).thenReturn(false);
//    Space space = mock(Space.class);
//    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
//    when(spaceService.isMember(nullable(String.class), nullable(String.class))).thenReturn(false);
//    when(spaceService.isSuperManager(nullable(String.class))).thenReturn(false);
//    when(space.getVisibility()).thenReturn("private");
//    when(space.getDisplayName()).thenReturn("Test news space");
//    when(space.getGroupId()).thenReturn("/spaces/test_news_space");
//    Identity poster = mock(Identity.class);
//    when(identityManager.getOrCreateIdentity(nullable(String.class), nullable(String.class))).thenReturn(poster);
//
//    Profile p1 = new Profile(poster);
//    p1.setProperty("fullName", "Sara Boutej");
//    NewsFilter newsFilter = new NewsFilter();
//    newsFilter.setPinnedNews(true);
//
//    when(poster.getProfile()).thenReturn(p1);
//    // When
//    setRootAsCurrentIdentity();
//    List<News> newsList = newsService.getNews(newsFilter);
//
//    // Then
//    assertNotNull(newsList);
//    assertEquals(3, newsList.size());
//    verify(it, times(4)).hasNext();
//    verify(it, times(3)).nextNode();
//  }
//
//  @Test
//  public void shouldGetNewsListsWhenSearching() throws Exception {
//    // Given
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//
//    String lang = "en";
//    String searchText = "Test";
//    NewsFilter filter = new NewsFilter();
//    filter.setSearchText(searchText);
//    List<SearchResult> ret = new ArrayList<>();
//    NewsSearchResult searchResult = mock(NewsSearchResult.class);
//    ret.add(searchResult);
//    Mockito.doReturn(ret).when(newsSearchConnector).search(filter, 0, 0, "relevancy", "desc");
//
//    // When
//    List<News> newsList = newsService.searchNews(filter, lang);
//
//    // Then
//    assertNotNull(newsList);
//    assertEquals(1, newsList.size());
//  }
//
//  @Test
//  public void shouldArchiveNews() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//
//    News news = new News();
//    news.setTitle("archived title");
//    news.setSummary("archived summary");
//    news.setBody("archived body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//
//    Property property = mock(Property.class);
//    when(newsNode.getProperty("exo:pinned")).thenReturn(property);
//    when(property.getBoolean()).thenReturn(true);
//    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);
//    Mockito.doNothing().when(newsServiceSpy).unpinNews("id123");
//
//    // When
//    newsServiceSpy.archiveNews("id123");
//
//    // Then
//    verify(newsNode, times(1)).save();
//    verify(newsNode, times(1)).setProperty(eq("exo:archived"), eq(true));
//
//  }
//
//  @Test
//  public void shouldNotArchiveNewsAsNewsNodeIsNull() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//
//    News news = new News();
//    news.setTitle("archived title");
//    news.setSummary("archived summary");
//    news.setBody("archived body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(null);
//
//    exceptionRule.expect(ItemNotFoundException.class);
//    exceptionRule.expectMessage("Unable to find a node with an UUID equal to: id123");
//
//    newsService.archiveNews("id123");
//
//  }
//
//  @Test
//  public void shouldNotUnarchiveNewsAsNewsNodeIsNull() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//
//    News news = new News();
//    news.setTitle("archived title");
//    news.setSummary("archived summary");
//    news.setBody("archived body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(null);
//
//    exceptionRule.expect(ItemNotFoundException.class);
//    exceptionRule.expectMessage("Unable to find a node with an UUID equal to: id123");
//
//    newsService.unarchiveNews("id123");
//
//  }
//
//  @Test
//  public void shouldUnarchiveNews() throws Exception {
//    // Given
//
//    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
//    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
//
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//
//    ExtendedNode newsNode = mock(ExtendedNode.class);
//
//    News news = new News();
//    news.setTitle("archived title");
//    news.setSummary("archived summary");
//    news.setBody("archived body");
//    news.setUploadId(null);
//    String sDate1 = "22/08/2019";
//    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
//    news.setCreationDate(date1);
//    news.setPinned(true);
//    news.setId("id123");
//
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(newsNode.getName()).thenReturn("archived title");
//
//    // When
//    newsService.unarchiveNews("id123");
//
//    // Then
//    verify(newsNode, times(1)).save();
//    verify(newsNode, times(1)).setProperty(eq("exo:archived"), eq(false));
//
//  }
//
//  @Test
//  public void shouldUpdateNewsNodePathWhenNewsNameIsUpdated() throws Exception {
//    NewsService newsService = buildNewsService();
//
//    Node newsNode = mock(Node.class);
//    Node parentNode = mock(Node.class);
//    Node illustrationNode = mock(Node.class);
//    Property property = mock(Property.class);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(newsNode.getProperty(nullable(String.class))).thenReturn(property);
//    when(newsNode.getName()).thenReturn("Untitled");
//    when(newsNode.getPath()).thenReturn("/Groups/spaces/space_test/News/2020/1/22/Untitled");
//    when(newsNode.getParent()).thenReturn(parentNode);
//    when(parentNode.getPath()).thenReturn("/Groups/spaces/space_test/News/2020/1/22");
//    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
//    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
//    when(property.getDate()).thenReturn(Calendar.getInstance());
//    when(imageProcessor.processImages(nullable(String.class), any(), nullable(String.class))).thenAnswer(i -> i.getArguments()[0]);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//    Identity johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
//    Profile profile = johnIdentity.getProfile();
//    profile.setUrl("/profile/john");
//    profile.setProperty("fullName", "john john");
//    Set<String> mentionedIds = new HashSet(Collections.singleton("john"));
//    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("john"))).thenReturn(johnIdentity);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    when(CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
//
//    News news = new News();
//    news.setTitle("title");
//    news.setSummary("summary");
//    news.setBody("body <img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/composer/image/thumbnail?uploadId=88b5af9\">");
//    news.setUploadId(null);
//    news.setViewsCount((long) 10);
//    // when
//    news.setBody("Updated body @john <img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/composer/image/thumbnail?uploadId=88b5af9\">");
//    News updatedNews = newsService.updateNews(news);
//    // then
//    verify(workSpace, times(1)).move(any(), any());
//    verify(newsNode, times(1)).save();
//  }
//
//  @PrepareForTest({ LinkProvider.class, NotificationContextImpl.class, PluginKey.class, PropertyManager.class, CommonsUtils.class })
//  @Test
//  public void shouldUpdateNewsMentionedIdsWhenNewsBodyIsUpdated() throws Exception {
//    NewsService newsService = buildNewsService();
//
//    Node newsNode = mock(Node.class);
//    Node parentNode = mock(Node.class);
//    Node illustrationNode = mock(Node.class);
//    Property property = mock(Property.class);
//    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(session.getNodeByUUID(nullable(String.class))).thenReturn(newsNode);
//    when(newsNode.getProperty(nullable(String.class))).thenReturn(property);
//    when(newsNode.getName()).thenReturn("Untitled");
//    when(newsNode.getPath()).thenReturn("/Groups/spaces/space_test/News/2020/1/22/Untitled");
//    when(newsNode.getParent()).thenReturn(parentNode);
//    when(parentNode.getPath()).thenReturn("/Groups/spaces/space_test/News/2020/1/22");
//    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
//    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
//    when(property.getDate()).thenReturn(Calendar.getInstance());
//    when(imageProcessor.processImages(nullable(String.class), any(), nullable(String.class))).thenAnswer(i -> i.getArguments()[0]);
//    Workspace workSpace = mock(Workspace.class);
//    when(session.getWorkspace()).thenReturn(workSpace);
//
//    Space space1 = new Space();
//    space1.setId("1");
//    space1.setDisplayName("space1");
//    space1.setGroupId("space1");
//    space1.setPrettyName("space1");
//    space1.setVisibility("private");
//    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
//    when(repositoryService.getCurrentRepository()).thenReturn(repository);
//    when(repository.getConfiguration()).thenReturn(repositoryEntry);
//    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
//    when(sessionProvider.getSession(any(), any())).thenReturn(session);
//    when(spaceService.getSpaceById("1")).thenReturn(space1);
//    when(spaceService.isMember(space1, "root")).thenReturn(true);
//    when(session.getNodeByUUID("id123")).thenReturn(newsNode);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    when(CommonsUtils.getService(SessionProviderService.class)).thenReturn(sessionProviderService);
//    when(CommonsUtils.getService(RepositoryService.class)).thenReturn(repositoryService);
//    when(CommonsUtils.getService(IdentityManager.class)).thenReturn(identityManager);
//    when(CommonsUtils.getCurrentDomain()).thenReturn("http://localhost:8080");
//
//    when(newsNode.hasNode("illustration")).thenReturn(true);
//    PowerMockito.mockStatic(PropertyManager.class);
//    when(PropertyManager.getProperty("gatein.email.domain.url")).thenReturn("http://localhost:8080/");
//
//    PowerMockito.mockStatic(LinkProvider.class);
//    when(LinkProvider.getSingleActivityUrl("1")).thenReturn("portal/intranet/activity?id=1");
//    PowerMockito.mockStatic(NotificationContextImpl.class);
//    NotificationContext ctx = mock(NotificationContext.class);
//    NotificationExecutor executor = mock(NotificationExecutor.class);
//
//    ArgumentLiteral<String> CONTENT_TITLE = new ArgumentLiteral<String>(String.class, "CONTENT_TITLE");
//
//    ArgumentLiteral<String> CONTENT_AUTHOR = new ArgumentLiteral<String>(String.class, "CONTENT_AUTHOR");
//
//    ArgumentLiteral<String> CONTENT_SPACE = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE");
//
//    ArgumentLiteral<String> CONTENT_SPACE_ID = new ArgumentLiteral<String>(String.class, "CONTENT_SPACE_ID");
//
//    ArgumentLiteral<String> ILLUSTRATION_URL = new ArgumentLiteral<String>(String.class, "ILLUSTRATION_URL");
//
//    ArgumentLiteral<String> AUTHOR_AVATAR_URL = new ArgumentLiteral<String>(String.class, "AUTHOR_AVATAR_URL");
//
//    ArgumentLiteral<String> ACTIVITY_LINK = new ArgumentLiteral<String>(String.class, "ACTIVITY_LINK");
//
//    ArgumentLiteral<Set> MENTIONED_IDS = new ArgumentLiteral<Set>(Set.class, "MENTIONED_IDS");
//
//    ArgumentLiteral<NotificationConstants.NOTIFICATION_CONTEXT> CONTEXT =
//            new ArgumentLiteral<NotificationConstants.NOTIFICATION_CONTEXT>(NotificationConstants.NOTIFICATION_CONTEXT.class,
//                    "CONTEXT");
//
//    when(NotificationContextImpl.cloneInstance()).thenReturn(ctx);
//    when(ctx.append(CONTENT_TITLE, "Updated title")).thenReturn(ctx);
//    when(ctx.append(CONTENT_AUTHOR, "root")).thenReturn(ctx);
//    when(ctx.append(CONTENT_SPACE_ID, "1")).thenReturn(ctx);
//    when(ctx.append(CONTENT_SPACE, "space1")).thenReturn(ctx);
//    when(ctx.append(ILLUSTRATION_URL, "http://localhost:8080/portal/rest/v1/news/1234/illustration")).thenReturn(ctx);
//    when(ctx.append(AUTHOR_AVATAR_URL, "http://localhost:8080/eXoSkin/skin/images/avatar/DefaultUserAvatar.png")).thenReturn(ctx);
//    when(ctx.append(ACTIVITY_LINK, "http://localhost:8080/portal/intranet/activity?id=1")).thenReturn(ctx);
//    when(ctx.append(CONTEXT, NotificationConstants.NOTIFICATION_CONTEXT.MENTION_IN_NEWS)).thenReturn(ctx);
//
//    when(ctx.getNotificationExecutor()).thenReturn(executor);
//    PowerMockito.mockStatic(PluginKey.class);
//    PluginKey plugin = mock(PluginKey.class);
//    when(PluginKey.key("PostNewsNotificationPlugin")).thenReturn(plugin);
//    when(PluginKey.key("MentionInNewsNotificationPlugin")).thenReturn(plugin);
//    NotificationCommand notificationCommand = mock(NotificationCommand.class);
//    when(ctx.makeCommand(plugin)).thenReturn(notificationCommand);
//    NotificationExecutor notificationExecutor = mock(NotificationExecutor.class);
//    when(executor.with(notificationCommand)).thenReturn(notificationExecutor);
//    when(notificationExecutor.execute(ctx)).thenReturn(true);
//    setRootAsCurrentIdentity();
//
//    Identity johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
//    Identity rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
//    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("root"))).thenReturn(rootIdentity);
//
//    Profile profile = johnIdentity.getProfile();
//    profile.setUrl("/profile/john");
//    profile.setProperty("fullName", "john john");
//    Set<String> mentionedIds = new HashSet(Collections.singleton("john"));
//    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("john"))).thenReturn(johnIdentity);
//
//    when(ctx.append(MENTIONED_IDS, mentionedIds)).thenReturn(ctx);
//
//    News news = new News();
//    news.setTitle("Updated title");
//    news.setId("1234");
//    news.setAuthor("root");
//    news.setSummary("Updated summary");
//    news.setActivities("1:1;");
//    news.setBody("Updated body @john");
//    news.setCreationDate(new Date());
//    news.setUploadId(null);
//    news.setViewsCount((long) 10);
//    news.setPublicationState(PublicationDefaultStates.PUBLISHED);
//    news.setPublicationDate(Calendar.getInstance().getTime());
//
//    // when
//    newsService.updateNews(news);
//
//    // then
//    verify(workSpace, times(1)).move(any(), any());
//    verify(newsNode, times(1)).save();
//    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
//    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
//    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body @john"));
//    verify(newsNode, times(1)).setProperty(eq("exo:dateModified"), any(Calendar.class));
//
//  }
//
//  private void setCurrentIdentity() {
//    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("user");
//    MembershipEntry membershipentry = new MembershipEntry("/platform/web-contributors", "publisher");
//    List<MembershipEntry> memberships = new ArrayList<MembershipEntry>();
//    memberships.add(membershipentry);
//    currentIdentity.setMemberships(memberships);
//    ConversationState state = new ConversationState(currentIdentity);
//    ConversationState.setCurrent(state);
//  }
//
//  @Test
//  @PrepareForTest({ LinkProvider.class, CommonsUtils.class })
//  public void testFormatMention() throws Exception {
//    //Given
//    NewsServiceImpl newsServiceImpl = new NewsServiceImpl(repositoryService,
//                                                          sessionProviderService,
//                                                          nodeHierarchyCreator,
//                                                          dataDistributionManager,
//                                                          spaceService,
//                                                          activityManager,
//                                                          identityManager,
//                                                          uploadService,
//                                                          imageProcessor,
//                                                          linkManager,
//                                                          publicationServiceImpl,
//                                                          publicationManagerImpl,
//                                                          wcmPublicationServiceImpl,
//                                                          newsSearchConnector,
//                                                          newsAttachmentsService,
//            indexingService, newsESSearchConnector, userACL);
//    Identity posterIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
//    Profile profile = posterIdentity.getProfile();
//    profile.setUrl("/profile/john");
//    profile.setProperty("fullName", "john john");
//    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("john"))).thenReturn(posterIdentity);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    PowerMockito.when(CommonsUtils.getCurrentDomain()).thenReturn("http://exoplatfom.com");
//    PowerMockito.mockStatic(LinkProvider.class);
//    PowerMockito.when(LinkProvider.getProfileLink(eq("john"), eq("dw"))).thenReturn("<a href=\"http://exoplatfom.com/portal/dw/profile/john\">john john</a>");
//
//    //When
//    String article = newsServiceImpl.substituteUsernames("dw", "test mention user in news <p>@john</p>");
//
//    //Then
//    assertEquals("test mention user in news <p><a href=\"http://exoplatfom.com/portal/dw/profile/john\">john john</a></p>", article);
//  }
//  @Test
//  public void testWrongFormatMention() throws Exception {
//    // Given
//    NewsServiceImpl newsServiceImpl = new NewsServiceImpl(repositoryService,
//                                                          sessionProviderService,
//                                                          nodeHierarchyCreator,
//                                                          dataDistributionManager,
//                                                          spaceService,
//                                                          activityManager,
//                                                          identityManager,
//                                                          uploadService,
//                                                          imageProcessor,
//                                                          linkManager,
//                                                          publicationServiceImpl,
//                                                          publicationManagerImpl,
//                                                          wcmPublicationServiceImpl,
//                                                          newsSearchConnector,
//                                                          newsAttachmentsService,
//                                                          indexingService,
//                                                          newsESSearchConnector,
//                                                          userACL);
//    Identity posterIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
//    Profile profile = posterIdentity.getProfile();
//    profile.setUrl("/profile/john");
//    profile.setProperty("fullName", "john john");
//    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("john"))).thenReturn(posterIdentity);
//    PowerMockito.mockStatic(CommonsUtils.class);
//    PowerMockito.when(CommonsUtils.getCurrentDomain()).thenReturn("http://exoplatfom.com");
//
//    //When
//    String article = newsServiceImpl.substituteUsernames("dw", "test mention user in news @ john");
//    //Then
//    assertEquals("test mention user in news @ john", article);
//  }
//  @Test
//  public void testHTMLSanitization() throws Exception {
//    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
//                                                      sessionProviderService,
//                                                      nodeHierarchyCreator,
//                                                      dataDistributionManager,
//                                                      spaceService,
//                                                      activityManager,
//                                                      identityManager,
//                                                      uploadService,
//                                                      imageProcessor,
//                                                      linkManager,
//                                                      publicationServiceImpl,
//                                                      publicationManagerImpl,
//                                                      wcmPublicationServiceImpl,
//                                                      newsSearchConnector,
//                                                      newsAttachmentsService,
//                                                      indexingService,
//                                                      newsESSearchConnector,
//                                                      userACL);
//  
//    Node newsNode = mock(Node.class);
//    when(newsNode.hasProperty(nullable(String.class))).thenReturn(true);
//    when(newsNode.hasProperty(eq("jcr:frozenUuid"))).thenReturn(false);
//    when(newsNode.hasProperty(eq("exo:dateCreated"))).thenReturn(false);
//    when(newsNode.hasProperty(eq("exo:dateModified"))).thenReturn(false);
//    when(newsNode.hasProperty(eq("exo:activities"))).thenReturn(false);
//  
//    Property property = mock(Property.class);
//    when(property.getString()).thenReturn("propertyValue");
//  
//    Property propertyBody = mock(Property.class);
//    when(propertyBody.getString()).thenReturn("body <img='#' onerror=alert('test')/>");
//    when(newsNode.getProperty(nullable(String.class))).thenReturn(property);
//    when(newsNode.getProperty(eq("exo:body"))).thenReturn(propertyBody);
//
//    Space space = mock(Space.class);
//    when(spaceService.getSpaceById(nullable(String.class))).thenReturn(space);
//    when(space.getGroupId()).thenReturn("/spaces/space1");
//    when(space.getVisibility()).thenReturn("private");
//
//    setRootAsCurrentIdentity();
//    assertFalse(newsService.convertNodeToNews(newsNode, false).getBody().contains("<img"));
//  }
//
  private void setRootAsCurrentIdentity() {
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("root");
    ConversationState state = new ConversationState(currentIdentity);
    ConversationState.setCurrent(state);
  }

  private JcrNewsStorage buildJcrNewsStorage() {
    JcrNewsStorage jcrNewsStorage = new JcrNewsStorage(repositoryService,
                                                  sessionProviderService,
                                                  nodeHierarchyCreator,
                                                  dataDistributionManager,
                                                  activityManager,
                                                  spaceService,
                                                  uploadService,
                                                  publicationServiceImpl,
                                                  publicationManagerImpl,
                                                  newsAttachmentsService,
                                                  identityManager,
                                                  linkManager,
                                                  newsSearchConnector,
                                                  wcmPublicationServiceImpl);
    return jcrNewsStorage;
  }
}