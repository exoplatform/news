package org.exoplatform.news.service.impl;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.*;

import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.Utils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.upload.UploadService;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NewsServiceImplTest {

    private static final MockedStatic<NewsUtils> NEWS_UTILS = mockStatic(NewsUtils.class);
    private static final MockedStatic<Utils> SOCIAL_UTILS = mockStatic(Utils.class);

    @Mock
    private NewsStorage newsStorage;

    @Mock
    private SpaceService spaceService;

    @Mock
    private ActivityManager activityManager;

    @Mock
    private IdentityManager identityManager;

    @Mock
    private NewsESSearchConnector newsESSearchConnector;

    @Mock
    private IndexingService indexingService;

    @Mock
    private UserACL userACL;

    @Mock
    private NewsTargetingService newsTargetingService;

    @Mock
    private UploadService uploadService;

    @Mock
    private MetadataService metadataService;

    private NewsService newsService;

    @AfterClass
    public static void afterRunBare() throws Exception { // NOSONAR
      NEWS_UTILS.close();
      SOCIAL_UTILS.close();
    }

    @Before
    public void setUp() {
        this.newsService = new NewsServiceImpl(spaceService, activityManager, identityManager, uploadService,
                newsESSearchConnector,indexingService, newsStorage, userACL, newsTargetingService, metadataService);
    }

    @Test
    public void markAsRead() throws Exception {
        News news = new News();
        news.setId("1");
        news.setViewsCount((long) 2);

        when(newsStorage.isCurrentUserInNewsViewers(anyString(), anyString())).thenReturn(true, false);
        doNothing().when(newsStorage).markAsRead(any(), anyString());
        newsService.markAsRead(news, "user");
        NEWS_UTILS.verify(() -> NewsUtils.broadcastEvent(anyString(), any(), any()), atLeastOnce());
        verify(newsStorage, times(0)).markAsRead(news, "user");
        newsService.markAsRead(news, "user");
        NEWS_UTILS.verify(() -> NewsUtils.broadcastEvent(anyString(), any(), any()), atLeastOnce());
        verify(newsStorage, times(1)).markAsRead(news, "user");
    }

    @Test
    public void testUpdateNews() throws Exception {
        List<String> oldTargets = new LinkedList<>();
        oldTargets.add("sliderNews");
        List<String> newTargets = new LinkedList<>();
        newTargets.add("newsTarget");

        News originalNews = new News();
        originalNews.setTitle("title");
        originalNews.setAuthor("root");
        originalNews.setId("id123");
        originalNews.setSpaceId("3");
        originalNews.setActivities("3:39;");
        originalNews.setActivityId("10");
        originalNews.setPublished(false);
        originalNews.setPublicationState("archived");

        News news = new News();
        news.setTitle("title");
        news.setAuthor("root");
        news.setId("id123");
        news.setSpaceId("3");
        news.setActivities("3:39;");
        news.setActivityId("10");
        news.setPublished(false);
        news.setPublicationState("draft");
        news.setCanPublish(false);
        news.setTargets(newTargets);

        org.exoplatform.services.security.Identity identity = new Identity("1");
        org.exoplatform.social.core.identity.model.Identity rootIdentity = new org.exoplatform.social.core.identity.model.Identity("1");
        Profile currentProfile = new Profile();
        currentProfile.setProperty(Profile.FULL_NAME, "root");
        rootIdentity.setProfile(currentProfile);
        when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(rootIdentity);

        Space space = new Space();
        space.setId(news.getSpaceId());
        space.setGroupId("/spaces/test_space");
        space.setPrettyName("space_test");

        doNothing().when(newsTargetingService).deleteNewsTargets(any(News.class), anyString());
        doNothing().when(newsTargetingService).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());
        when(newsTargetingService.getTargetsByNewsId(news.getId())).thenReturn(Collections.emptyList());
        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        when(newsStorage.updateNews(any(News.class), anyString())).thenReturn(originalNews);
        doNothing().when(newsTargetingService).deleteNewsTargets(news.getId());
        doNothing().when(newsTargetingService).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        NEWS_UTILS.when(() -> NewsUtils.processMentions(anyString(),any())).thenReturn(new HashSet<>());
        NEWS_UTILS.when(() -> NewsUtils.getUserIdentity(anyString())).thenReturn(identity);
        NEWS_UTILS.when(() -> NewsUtils.canPublishNews(anyString(), any(org.exoplatform.services.security.Identity.class))).thenReturn(true);

        when(spaceService.getSpaceById(news.getSpaceId())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> newsService.updateNews(news, "root", false, false));
        when(spaceService.getSpaceById(news.getSpaceId())).thenReturn(space);

        newsService.updateNews(news, "root", false, false);
        verify(newsStorage, times(1)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(0)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(0)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        news.setTitle("title updated");
        newsService.updateNews(news, "root", null, false);
        verify(newsStorage, times(2)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(0)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(0)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        news.setSummary("summary updated");
        newsService.updateNews(news, "root", null, false);
        verify(newsStorage, times(3)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(0)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(0)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        news.setBody("body updated");
        newsService.updateNews(news, "root", null, false);
        verify(newsStorage, times(4)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(0)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(0)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        news.setPublished(false);
        news.setPublicationState("archived");
        news.setCanPublish(true);
        newsService.updateNews(news, "root", null, true);
        verify(newsStorage, times(5)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(2)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(2)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());

        when(newsTargetingService.getTargetsByNewsId(news.getId())).thenReturn(oldTargets);
        originalNews.setPublished(true);
        originalNews.setTargets(oldTargets);
        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        newsService.updateNews(news, "root", null, false);
        verify(newsStorage, times(6)).updateNews(any(News.class), anyString());
        verify(newsTargetingService, times(3)).deleteNewsTargets(any(News.class), anyString());
        verify(newsTargetingService, times(2)).saveNewsTarget(any(News.class), anyBoolean(), anyList(), anyString());
    }

  @Test
  public void testDeleteNews() throws Exception {
    News news = new News();
    news.setId("id123");
    news.setSpaceId("1");

    org.exoplatform.services.security.Identity identity = new Identity("1");
    org.exoplatform.social.core.identity.model.Identity rootIdentity =
                                                                     new org.exoplatform.social.core.identity.model.Identity("1");
    Profile currentProfile = new Profile();
    currentProfile.setProperty(Profile.FULL_NAME, "root");
    rootIdentity.setProfile(currentProfile);
    when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root")).thenReturn(rootIdentity);
    NEWS_UTILS.when(() -> NewsUtils.getUserIdentity(anyString())).thenReturn(identity);
    Space space = new Space();
    space.setId(news.getSpaceId());
    space.setGroupId("/spaces/test_space");
    space.setPrettyName("space_test");
    when(spaceService.getSpaceById(news.getSpaceId())).thenReturn(space);
    when(newsStorage.getNewsById(news.getId(), false)).thenReturn(news);

    assertThrows(IllegalArgumentException.class, () -> newsService.deleteNews(news.getId(), identity, false));
    verify(newsStorage, times(1)).getNewsById(anyString(), anyBoolean());
    verify(metadataService, times(0)).deleteMetadataItemsByObject(any(MetadataObject.class));

    news.setAuthor(identity.getUserId());
    when(newsStorage.getNewsById(news.getId(), false)).thenReturn(news);
    newsService.deleteNews(news.getId(), identity, false);
    verify(newsStorage, times(2)).getNewsById(anyString(), anyBoolean());
    verify(metadataService, times(1)).deleteMetadataItemsByObject(any(MetadataObject.class));
  }
}
