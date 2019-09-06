package org.exoplatform.news;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.services.cms.link.LinkManager;
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
import org.exoplatform.social.ckeditor.HTMLUploadImageProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

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
  HTMLUploadImageProcessor imageProcessor;

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
                                                  linkManager);
    Node node = mock(Node.class);
    Property property = mock(Property.class);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(node);
    when(node.getProperty(anyString())).thenReturn(property);
    when(property.getDate()).thenReturn(Calendar.getInstance());

    // When
    News news = newsService.getNews("1");

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
                                                  linkManager);
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    when(session.getNodeByUUID(anyString())).thenReturn(null);

    // When
    News news = newsService.getNews("1");

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
            linkManager);
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
            linkManager);
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
            linkManager);
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
                                                      linkManager);

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
    Mockito.doReturn(news).when(newsServiceSpy).getNews("id123");
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

}
