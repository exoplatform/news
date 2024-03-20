package org.exoplatform.news.storage.jcr;

import static org.exoplatform.news.storage.jcr.JcrNewsStorage.EXO_PRIVILEGEABLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.connector.NewsSearchConnector;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchConnector;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
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
  WCMPublicationServiceImpl  wcmPublicationServiceImpl;

  @Mock
  PublicationManagerImpl     publicationManagerImpl;

  @Mock
  PublicationServiceImpl     publicationServiceImpl;

  @Mock
  HTMLUploadImageProcessor imageProcessor;

  @Mock
  NewsSearchConnector        newsSearchConnector;

  @Mock
  NewsAttachmentsStorage     newsAttachmentsStorage;

  @AfterClass
  public static void afterRunBare() throws Exception { // NOSONAR
    COMMONS_UTILS.close();
    PORTAL_CONTAINER.close();
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
    when(property.getDate()).thenReturn(calendar1);
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
    Identity userIdentity = new Identity("organization", "user");
    userIdentity.setId("user");
    userIdentity.setRemoteId("user");
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
    when(identityManager.getOrCreateIdentity(anyString(), nullable(String.class))).thenReturn(userIdentity);
    News news = new News();
    news.setTitle("Updated title");
    news.setSummary("Updated summary");
    news.setBody("Updated body");
    news.setUploadId(null);
    news.setViewsCount((long) 10);
    news.setPublicationState(PublicationDefaultStates.DRAFT);

    // When
    jcrNewsStorage.updateNews(news, "user");

    // Then
    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body"));
    verify(newsNode, times(1)).setProperty(eq("exo:lastModifiedDate"), any(Calendar.class));
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
    verify(newsImageNode, times(1)).setPermission("any", JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
    verify(newsImageNode, times(1)).save();

    //
    ExtendedNode existingUploadedNewsImageNode = mock(ExtendedNode.class);
    String nodePath = "Groups/spaces/test/testimage";
    String currentDomainName = "https://exoplatform.com";
    String currentPortalContainerName = "portal";
    String restContextName = "rest";
    COMMONS_UTILS.when(() -> CommonsUtils.getRestContextName()).thenReturn(restContextName);
    PORTAL_CONTAINER.when(() -> PortalContainer.getCurrentPortalContainerName()).thenReturn(currentPortalContainerName);
    COMMONS_UTILS.when(() -> CommonsUtils.getCurrentDomain()).thenReturn(currentDomainName);
    news.setBody("news body with image src=\"https://exoplatform.com/portal/rest/jcr/repository/collaboration/Groups/spaces/test/testimage\"");
    when(session.getItem(nullable(String.class))).thenReturn(existingUploadedNewsImageNode);
    when(jcrNewsStorageSpy.getNodeByPath(nodePath, sessionProvider)).thenReturn(existingUploadedNewsImageNode);
    when(existingUploadedNewsImageNode.canAddMixin(EXO_PRIVILEGEABLE)).thenReturn(true);
    // When
    jcrNewsStorageSpy.publishNews(news);
    verify(existingUploadedNewsImageNode, times(1)).setPermission("any", JcrNewsStorage.SHARE_NEWS_PERMISSIONS);
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
                                                  newsAttachmentsStorage,
                                                  identityManager,
                                                  newsSearchConnector,
                                                  wcmPublicationServiceImpl);
    return jcrNewsStorage;
  }
}