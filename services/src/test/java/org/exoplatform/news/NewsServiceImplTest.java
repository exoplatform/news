package org.exoplatform.news;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.ecm.publication.impl.PublicationServiceImpl;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.version.VersionHistoryImpl;
import org.exoplatform.services.jcr.impl.core.version.VersionImpl;
import org.exoplatform.services.wcm.extensions.publication.WCMPublicationServiceImpl;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationPlugin;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.impl.LifecyclesConfig.Lifecycle;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.social.ckeditor.HTMLUploadImageProcessor;
import org.exoplatform.services.wcm.extensions.publication.impl.PublicationManagerImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.exoplatform.social.core.identity.model.Profile;


@RunWith(MockitoJUnitRunner.class)
public class NewsServiceImplTest {

  @Mock
  RepositoryService       repositoryService;

  @Mock
  SessionProviderService  sessionProviderService;

  @Mock
  ManageableRepository repository;

  @Mock
  RepositoryEntry repositoryEntry;

  @Mock
  SessionProvider sessionProvider;

  @Mock
  Session session;

  @Mock
  NodeHierarchyCreator    nodeHierarchyCreator;

  @Mock
  DataDistributionManager dataDistributionManager;

  @Mock
  SpaceService            spaceService;

  @Mock
  ActivityManager         activityManager;

  @Mock
  IdentityManager         identityManager;

  @Mock
  UploadService           uploadService;

  @Mock
  LinkManager             linkManager;

  @Mock
  WCMPublicationServiceImpl   wcmPublicationServiceImpl;

  @Mock
  PublicationManagerImpl publicationManagerImpl;

  @Mock
  PublicationServiceImpl publicationServiceImpl;

  @Mock
  AuthoringPublicationPlugin authoringPublicationPlugin;

  @Mock
  HTMLUploadImageProcessor imageProcessor;
  
  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void shouldGetNodeWhenNewsExists() throws Exception {
    // Given
    NewsService newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node node = mock(Node.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(node);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(node.getSession()).thenReturn(session);
    when(node.getProperty(anyString())).thenReturn(property);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    Space space = mock(Space.class);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getGroupId()).thenReturn("/spaces/space1");

    // When
    News news = newsService.getNewsById("1");

    // Then
    assertNotNull(news);
  }

  @Test
  public void shouldGetNullWhenNewsDoesNotExist() throws Exception {
    // Given
    NewsService newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(null);

    // When
    News news = newsService.getNewsById("1");

    // Then
    assertNull(news);
  }

  @Test
  public void shouldUpdateNodeAndKeepIllustrationWhenUpdatingNewsWithNullUploadId() throws Exception {
    // Given
    NewsService newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node newsNode = mock(Node.class);
    Node illustrationNode = mock(Node.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(newsNode.getProperty(anyString())).thenReturn(property);
    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(imageProcessor.processImages(anyString(), any(), anyString())).thenAnswer(i -> i.getArguments()[0]);

    News news = new News();
    news.setTitle("Updated title");
    news.setSummary("Updated summary");
    news.setBody("Updated body");
    news.setUploadId(null);

    // When
    newsService.updateNews(news);

    // Then
    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body"));
    verify(newsNode, times(1)).setProperty(eq("exo:dateModified"), any(Calendar.class));
    verify(illustrationNode, times(0)).remove();
  }

  @Test
  public void shouldUpdateNodeAndRemoveIllustrationWhenUpdatingNewsWithEmptyUploadId() throws Exception {
    // Given
    NewsService newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node newsNode = mock(Node.class);
    Node illustrationNode = mock(Node.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(newsNode.getProperty(anyString())).thenReturn(property);
    when(newsNode.getNode(eq("illustration"))).thenReturn(illustrationNode);
    when(newsNode.hasNode(eq("illustration"))).thenReturn(true);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(imageProcessor.processImages(anyString(), any(), anyString())).thenAnswer(i -> i.getArguments()[0]);

    News news = new News();
    news.setTitle("Updated title");
    news.setSummary("Updated summary");
    news.setBody("Updated body");
    news.setUploadId("");

    // When
    newsService.updateNews(news);

    // Then
    verify(newsNode, times(1)).setProperty(eq("exo:title"), eq("Updated title"));
    verify(newsNode, times(1)).setProperty(eq("exo:summary"), eq("Updated summary"));
    verify(newsNode, times(1)).setProperty(eq("exo:body"), eq("Updated body"));
    verify(newsNode, times(1)).setProperty(eq("exo:dateModified"), any(Calendar.class));
    verify(illustrationNode, times(1)).remove();
  }

  @Test
  public void shouldShareNewsWhenNewsExists() throws Exception {
    // Given
    NewsService newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(newsNode.getProperty(anyString())).thenReturn(property);
    when(activityManager.getActivity(anyString())).thenReturn(null);
    Identity spaceIdentity =  new Identity(SpaceIdentityProvider.NAME, "space1");
    when(identityManager.getOrCreateIdentity(eq(SpaceIdentityProvider.NAME), eq("space1"), anyBoolean())).thenReturn(spaceIdentity);
    Identity posterIdentity =  new Identity(OrganizationIdentityProvider.NAME, "john");
    when(identityManager.getOrCreateIdentity(eq(OrganizationIdentityProvider.NAME), eq("john"), anyBoolean())).thenReturn(posterIdentity);
    Space space1 = new Space();
    space1.setPrettyName("space1");
    SharedNews sharedNews = new SharedNews();
    sharedNews.setPoster("john");
    sharedNews.setDescription("Description of shared news");
    sharedNews.setSpacesNames(Arrays.asList("space1"));
    sharedNews.setActivityId("2");
    sharedNews.setNewsId("1");

    // When
    newsService.shareNews(sharedNews, Arrays.asList(space1));

    // Then
    ArgumentCaptor<Identity> identityCaptor = ArgumentCaptor.forClass(Identity.class);
    ArgumentCaptor<ExoSocialActivity> activityCaptor = ArgumentCaptor.forClass(ExoSocialActivity.class);
    verify(activityManager, times(1)).saveActivityNoReturn(identityCaptor.capture(), activityCaptor.capture());
    Identity identityCaptorValue = identityCaptor.getValue();
    assertEquals(SpaceIdentityProvider.NAME, identityCaptorValue.getProviderId());
    assertEquals("space1", identityCaptorValue.getRemoteId());
    ExoSocialActivity activityCaptorValue = activityCaptor.getValue();
    assertEquals("shared_news", activityCaptorValue.getType());
    assertEquals("Description of shared news", activityCaptorValue.getTitle());
    assertEquals(2, activityCaptorValue.getTemplateParams().size());
    assertEquals("1", activityCaptorValue.getTemplateParams().get("newsId"));
    assertEquals("2", activityCaptorValue.getTemplateParams().get("sharedActivityId"));
  }

  @Test
  public void shouldPinNews() throws Exception {
    // Given

    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node pinnedRootNode = mock(Node.class);
    Node newsFolderNode = mock(Node.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);

    News news = new News();
    news.setTitle("pinned title");
    news.setSummary("pinned summary");
    news.setBody("pinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(newsFolderNode);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(pinnedRootNode);

    // When
    newsServiceSpy.pinNews("id123");

    // Then
    verify(newsNode, times(1)).save();
    verify(newsNode, times(1)).setProperty(eq("exo:pinned"), eq(true));
    verify(newsNode, times(1)).addMixin(eq("exo:privilegeable"));
    verify(linkManager, times(1)).createLink(newsFolderNode, "exo:symlink", newsNode, null);

  }

  @Test
  public void shouldCreateNewsAndPinIt() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    News news = new News();
    news.setTitle("new pinned news title");
    news.setSummary("new pinned news summary");
    news.setBody("new pinned news body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setSpaceId("spaceTest");
    news.setAuthor("root");
    News createdNewsDraft = new News();
    createdNewsDraft.setId("id123");
    createdNewsDraft.setPinned(true);

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
    when(session.getNodeByUUID(anyString())).thenReturn(node);
    when(spaceService.getSpaceById("spaceTest")).thenReturn(space);
    when(nodeHierarchyCreator.getJcrPath("groupsPath")).thenReturn("spaces");
    when(space.getGroupId()).thenReturn("spaceTest");
    when(session.getItem("spacesspaceTest")).thenReturn(spaceRootNode);
    when(spaceRootNode.hasNode("News")).thenReturn(true);
    when(spaceRootNode.getNode("News")).thenReturn(spaceNewsRootNode);
    when(dataDistributionType.getOrCreateDataNode(spaceNewsRootNode, "2019/8/22")).thenReturn(newsFolderNode);
    when(newsFolderNode.addNode(anyString(), anyString())).thenReturn(newsNode);
    when(newsNode.getUUID()).thenReturn("id123");
    when(node.getProperty(anyString())).thenReturn(property);
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(identityManager.getOrCreateIdentity("organization", "root", false)).thenReturn(poster);
    when(space.getPrettyName()).thenReturn("spaceTest");
    when(identityManager.getOrCreateIdentity("space", "spaceTest", false)).thenReturn(spaceIdentity);
    when(poster.getId()).thenReturn("root");
    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);

    Mockito.doNothing().when(newsServiceSpy).pinNews("id123");
    Mockito.doReturn(createdNewsDraft).when(newsServiceSpy).createNewsDraft(news);
    Mockito.doNothing().when(newsServiceSpy).postNewsActivity(createdNewsDraft);
    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, "published",new HashMap<>());
    // When
    News createdNews = newsServiceSpy.createNews(news);

    // Then
    assertNotNull(createdNews);
    verify(newsServiceSpy, times(1)).pinNews("id123");
  }

  @Test
  public void shouldUnPinNews() throws Exception {
    // Given

    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node pinnedRootNode = mock(Node.class);
    Node newsFolderNode = mock(Node.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);
    Node pinnedNode = mock(Node.class);

    News news = new News();
    news.setTitle("unpinned");
    news.setSummary("unpinned summary");
    news.setBody("unpinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(newsFolderNode);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(pinnedRootNode);
    when(newsFolderNode.getNode("unpinned")).thenReturn(pinnedNode);
    when(newsNode.getName()).thenReturn("unpinned");
    // When
    newsServiceSpy.unpinNews("id123");

    // Then
    verify(newsNode, times(1)).save();
    verify(newsNode, times(1)).setProperty(eq("exo:pinned"), eq(false));
    verify(pinnedNode, times(1)).remove();
    verify(newsFolderNode, times(1)).save();

  }

  @Test
  public void shouldNotUnPinNewsAsNewsNodeIsNull() throws Exception {
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    News news = new News();
    news.setTitle("unpinned");
    news.setSummary("unpinned summary");
    news.setBody("unpinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(null);
    exceptionRule.expect(Exception.class);
    exceptionRule.expectMessage("Unable to find a node with an UUID equal to: id123");

    newsServiceSpy.unpinNews("id123");

  }

  @Test
  public void shouldNotUnPinNewsAsNewsIsNull() throws Exception {
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    Mockito.doReturn(null).when(newsServiceSpy).getNewsById("id123");
    exceptionRule.expect(Exception.class);
    exceptionRule.expectMessage("Unable to find a news with an id equal to: id123");

    newsServiceSpy.unpinNews("id123");

  }

  @Test
  public void shouldNotUnPinNewsAsPinnedRootNodeIsNull() throws Exception {
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node newsFolderNode = mock(Node.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);

    News news = new News();
    news.setTitle("unpinned");
    news.setSummary("unpinned summary");
    news.setBody("unpinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(newsFolderNode);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(null);
    exceptionRule.expect(Exception.class);
    exceptionRule.expectMessage("Unable to find the root pinned folder: /Application Data/News/pinned");

    newsServiceSpy.unpinNews("id123");

  }

  @Test
  public void shouldNotUnPinNewsAsNewsFolderNodeIsNull() throws Exception {
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);
    Node pinnedRootNode = mock(Node.class);

    News news = new News();
    news.setTitle("unpinned");
    news.setSummary("unpinned summary");
    news.setBody("unpinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(null);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(pinnedRootNode);
    exceptionRule.expect(Exception.class);
    exceptionRule.expectMessage("Unable to find the parent node of the current pinned node");

    newsServiceSpy.unpinNews("id123");

  }

  @Test
  public void shouldNotUnPinNewsAsPinnedNodeIsNull() throws Exception {
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);

    NewsService newsServiceSpy = Mockito.spy(newsService);
    ExtendedNode newsNode = mock(ExtendedNode.class);
    Node applicationDataNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);
    Node pinnedRootNode = mock(Node.class);
    Node newsFolderNode = mock(Node.class);

    News news = new News();
    news.setTitle("unpinned");
    news.setSummary("unpinned summary");
    news.setBody("unpinned body");
    news.setUploadId(null);
    String sDate1 = "22/08/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setPinned(true);
    news.setId("id123");
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(newsFolderNode);
    when(newsNode.canAddMixin(eq("exo:privilegeable"))).thenReturn(true);
    Mockito.doReturn(news).when(newsServiceSpy).getNewsById("id123");
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(newsRootNode.hasNode(eq("Pinned"))).thenReturn(true);
    when(newsRootNode.getNode(eq("Pinned"))).thenReturn(pinnedRootNode);
    when(newsFolderNode.getNode("unpinned")).thenReturn(null);
    exceptionRule.expect(Exception.class);
    exceptionRule.expectMessage("Unable to find the current pinned node");

    newsServiceSpy.unpinNews("id123");

  }

  @Test
  public void shouldCreateNewsDraft() throws Exception {
    // Given

    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProvider.getSession(any(), any())).thenReturn(session);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node newsFolderNode = mock(Node.class);
    Node newsRootNode = mock(Node.class);
    Node applicationDataNode = mock(Node.class);
    Node draftNode = mock(Node.class);
    draftNode.setProperty("id", "1");

    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");

    News news = new News();
    news.setTitle("title");
    news.setSummary("summary");
    news.setBody("body");
    news.setAuthor("john");
    String sDate1 = "10/09/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setId("id123");
    news.setSpaceId("1");
    Lifecycle newsLifecycle = new Lifecycle();
    newsLifecycle.setName("newsLifecycle");
    newsLifecycle.setPublicationPlugin("Authoring publication");
    //authoringPublicationPlugin.setName("Authoring publication");
    Map<String, WebpagePublicationPlugin> publicationPlugins = new HashMap<>();
    publicationPlugins.put("Authoring publication", authoringPublicationPlugin);

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(dataDistributionType.getOrCreateDataNode(any(Node.class), anyString())).thenReturn(newsFolderNode);
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(applicationDataNode.hasNode(eq("News"))).thenReturn(true);
    when(applicationDataNode.getNode(eq("News"))).thenReturn(newsRootNode);
    when(nodeHierarchyCreator.getJcrPath(anyString())).thenReturn("/Groups/");
    when(spaceService.getSpaceById(anyString())).thenReturn(space1);
    when(newsFolderNode.addNode(anyString(),anyString())).thenReturn(draftNode);
    when(publicationManagerImpl.getLifecycle(anyString())).thenReturn(newsLifecycle);
    when(wcmPublicationServiceImpl.getWebpagePublicationPlugins()).thenReturn(publicationPlugins);
    when(imageProcessor.processImages(news.getBody(), draftNode, "images")).thenReturn("");
    when(draftNode.canAddMixin(anyString())).thenReturn(true);

    // When
    newsService.createNewsDraft(news);

    // Then
    verify(newsRootNode, times(1)).save();
    verify(draftNode, times(1)).setProperty(eq("exo:title"), eq(news.getTitle()));
    verify(draftNode, times(1)).setProperty(eq("exo:summary"), eq(news.getSummary()));
    verify(draftNode, times(1)).setProperty(eq("exo:body"), eq(news.getBody()));
    verify(draftNode, times(1)).setProperty(eq("exo:author"), eq(news.getAuthor()));
    verify(draftNode, times(1)).setProperty(eq("exo:spaceId"), eq(news.getSpaceId()));
    verify(draftNode, times(1)).addMixin(eq("exo:datetime"));
    //
    verify(draftNode, times(1)).setProperty(eq("exo:body"), eq(""));
    verify(draftNode, times(1)).setProperty(eq("id"), eq("1"));
  }

  @Test
  public void shouldDeleteNewsWithId() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProvider.getSession(any(), any())).thenReturn(session);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node applicationDataNode = mock(Node.class);
    Node newsNode = mock(Node.class);
    newsNode.setProperty("id", "1");

    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");

    News news = new News();
    news.setTitle("title");
    news.setSummary("summary");
    news.setBody("body");
    news.setAuthor("john");
    String sDate1 = "10/09/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setId("id123");
    news.setSpaceId("1");

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);


    // When
    newsService.deleteNews("1");

    // Then
    verify(session, times(1)).getNodeByUUID(anyString());
    verify(session, times(1)).save();
    verify(newsNode, times(1)).remove();
  }

  @Test
  public void shouldCreateNewsDraftWhenNewsHasNoId() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProvider.getSession(any(), any())).thenReturn(session);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node applicationDataNode = mock(Node.class);
    Node newsNode = mock(Node.class);
    newsNode.setProperty("id", "1");

    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");

    News news = new News();
    news.setTitle("title");
    news.setSummary("summary");
    news.setBody("body");
    news.setAuthor("john");
    String sDate1 = "10/09/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setSpaceId("1");
    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    Mockito.doReturn(news).when(newsServiceSpy).createNewsDraft(news);
    Mockito.doNothing().when(newsServiceSpy).postNewsActivity(news);
    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, "published",new HashMap<>());


    // When
    newsServiceSpy.createNews(news);

    // Then
    verify(session, times(1)).getNodeByUUID(news.getId());
    verify(newsServiceSpy, times(1)).createNews(news);
    verify(newsServiceSpy, times(0)).updateNews(news);
  }

  @Test
  public void shouldUpdateNewsDraftWhenNewsHasId() throws Exception {
    // Given
    DataDistributionType dataDistributionType = mock(DataDistributionType.class);
    when(dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE)).thenReturn(dataDistributionType);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProvider.getSession(any(), any())).thenReturn(session);

    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    Node applicationDataNode = mock(Node.class);
    Node newsNode = mock(Node.class);
    newsNode.setProperty("id", "1");

    Space space1 = new Space();
    space1.setId("1");
    space1.setPrettyName("space1");
    space1.setGroupId("space1");

    News news = new News();
    news.setId("1");
    news.setTitle("title");
    news.setSummary("summary");
    news.setBody("body");
    news.setAuthor("john");
    String sDate1 = "10/09/2019";
    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(sDate1);
    news.setCreationDate(date1);
    news.setSpaceId("1");
    NewsServiceImpl newsServiceSpy = Mockito.spy(newsService);

    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getItem(anyString())).thenReturn(applicationDataNode);
    when(session.getNodeByUUID(anyString())).thenReturn(newsNode);
    Mockito.doNothing().when(newsServiceSpy).updateNews(news);
    Mockito.doNothing().when(newsServiceSpy).postNewsActivity(news);
    Mockito.doNothing().when(publicationServiceImpl).changeState(newsNode, "published",new HashMap<>());


    // When
    newsServiceSpy.createNews(news);

    // Then
    verify(session, times(1)).getNodeByUUID(news.getId());
    verify(newsServiceSpy, times(1)).updateNews(news);
  }

  @Test
  public void shouldGetAllNewsNodesWhenExists() throws Exception {
    // Given
    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    QueryManager qm = mock(QueryManager.class);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(workSpace.getQueryManager()).thenReturn(qm);
    Query query = mock(Query.class);
    when(qm.createQuery("select * from exo:news WHERE publication:currentState = 'published' and jcr:path like '/Groups/spaces/%'","sql")).thenReturn(query);
    QueryResult queryResult = mock(QueryResult.class);
    when(query.execute()).thenReturn(queryResult);
    NodeIterator it = mock(NodeIterator.class);
    when(queryResult.getNodes()).thenReturn(it);
    when(it.hasNext()).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);

    Node node1 = mock(Node.class);
    Node node2 = mock(Node.class);
    Node node3 = mock(Node.class);

    when(node1.getSession()).thenReturn(session);
    when(node2.getSession()).thenReturn(session);
    when(node3.getSession()).thenReturn(session);
    when(it.nextNode()).thenReturn(node1).thenReturn(node2).thenReturn(node3);
    Property property = mock(Property.class);
    when(node1.getProperty(anyString())).thenReturn(property);
    when(node2.getProperty(anyString())).thenReturn(property);
    when(node3.getProperty(anyString())).thenReturn(property);
    when(property.toString()).thenReturn("news ");
    when(property.getDate()).thenReturn(Calendar.getInstance());
    when(property.getBoolean()).thenReturn(true);
    when(node1.hasNode("illustration")).thenReturn(false);
    when(node2.hasNode("illustration")).thenReturn(false);
    when(node3.hasNode("illustration")).thenReturn(false);
    Space space = mock(Space.class);
    when(spaceService.getSpaceById(anyString())).thenReturn(space);
    when(space.getDisplayName()).thenReturn("Test news space");
    when(space.getGroupId()).thenReturn("/spaces/test_news_space");
    Identity poster = mock(Identity.class);
    when(identityManager.getOrCreateIdentity(anyString(),anyString(),anyBoolean())).thenReturn(poster);

    Profile p1 = new Profile(poster);
    p1.setProperty("fullName","Sara Boutej");

    when(poster.getProfile()).thenReturn(p1);
    // When
    List<News> newsList =newsService.getNews();

    // Then
    assertNotNull(newsList);
    assertEquals(3, newsList.size());
    verify(it, times(4)).hasNext();
    verify(it, times(3)).nextNode();
  }

  @Test
  public void shouldGetAllNewsNodesWhenDoesNotExists() throws Exception {
    // Given
    NewsServiceImpl newsService = new NewsServiceImpl(repositoryService,
            sessionProviderService,
            nodeHierarchyCreator,
            dataDistributionManager,
            spaceService,
            activityManager,
            identityManager,
            uploadService,
            imageProcessor,
            linkManager,
            publicationServiceImpl,
            publicationManagerImpl,
            wcmPublicationServiceImpl);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    QueryManager qm = mock(QueryManager.class);
    Workspace workSpace = mock(Workspace.class);
    when(session.getWorkspace()).thenReturn(workSpace);
    when(workSpace.getQueryManager()).thenReturn(qm);
    Query query = mock(Query.class);
    when(qm.createQuery("select * from exo:news WHERE publication:currentState = 'published' and jcr:path like '/Groups/spaces/%'","sql")).thenReturn(query);
    QueryResult queryResult = mock(QueryResult.class);
    when(query.execute()).thenReturn(queryResult);
    NodeIterator it = mock(NodeIterator.class);
    when(queryResult.getNodes()).thenReturn(it);
    when(it.hasNext()).thenReturn(false);

    // When
    List<News> newsList =newsService.getNews();

    // Then
    assertNotNull(newsList);
    assertEquals(0,newsList.size());
    verify(it, times(1)).hasNext();
    verify(it, times(0)).nextNode();
  }

}
