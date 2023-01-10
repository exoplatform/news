package org.exoplatform.news.service.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.PropertyManager;
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
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.upload.UploadService;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest(NewsUtils.class)
@PowerMockIgnore({ "javax.management.*", "jdk.internal.*", "javax.xml.*", "org.apache.xerces.*", "org.xml.*",
    "com.sun.org.apache.*", "org.w3c.*" })
public class NewsServiceImplTest {

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

    private NewsService newsService;

    @Before
    public void setUp() {
        this.newsService = new NewsServiceImpl(spaceService, activityManager, identityManager, uploadService,
                newsESSearchConnector,indexingService, newsStorage, userACL, newsTargetingService);
    }

    @Test
    public void markAsRead() throws Exception {
        News news = new News();
        news.setId("1");
        news.setViewsCount((long) 2);

        PowerMockito.mockStatic(NewsUtils.class);
        when(newsStorage.isCurrentUserInNewsViewers(anyString(), anyString())).thenReturn(true, false);
        doNothing().when(newsStorage).markAsRead(any(), anyString());
        newsService.markAsRead(news, "user");
        PowerMockito.verifyStatic(NewsUtils.class, atLeastOnce());
        NewsUtils.broadcastEvent(anyString(), any(), any());
        verify(newsStorage, times(0)).markAsRead(news, "user");
        newsService.markAsRead(news, "user");
        PowerMockito.verifyStatic(NewsUtils.class, atLeastOnce());
        NewsUtils.broadcastEvent(anyString(), any(), any());
        verify(newsStorage, times(1)).markAsRead(news, "user");

    }

    @Test
    public void testUpdateNews() throws Exception {
        List<String> targets = new LinkedList<>();
        targets.add("sliderNews");

        News originalNews = new News();
        originalNews.setTitle("title");
        originalNews.setAuthor("root");
        originalNews.setId("id123");
        originalNews.setSpaceId("3");
        originalNews.setActivities("3:39;");
        originalNews.setActivityId("10");
        originalNews.setPublished(false);
        originalNews.setPublicationState("draft");

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

        doNothing().when(newsTargetingService).deleteNewsTargets(anyString(), anyString());
        doNothing().when(newsTargetingService).saveNewsTarget(anyString(), anyBoolean(), anyList(), anyString());
        when(newsTargetingService.getTargetsByNewsId(news.getId())).thenReturn(Collections.emptyList());
        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        when(newsStorage.updateNews(any(News.class), anyString())).thenReturn(originalNews);
        doNothing().when(newsStorage).unpublishNews(news.getId());
        doNothing().when(newsStorage).publishNews(any(News.class));

        PowerMockito.mockStatic(NewsUtils.class);
        when(NewsUtils.processMentions(anyString())).thenReturn(new HashSet<>());
        when(NewsUtils.getUserIdentity(anyString())).thenReturn(identity);
        when(NewsUtils.canPublishNews(any(org.exoplatform.services.security.Identity.class))).thenReturn(true);

        when(spaceService.getSpaceById(news.getSpaceId())).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> newsService.updateNews(news, "root", false, false));
        when(spaceService.getSpaceById(news.getSpaceId())).thenReturn(space);

        newsService.updateNews(news, "root", false, false);
        verify(newsStorage, times(0)).unpublishNews(news.getId());
        verify(newsStorage, times(0)).publishNews(any(News.class));

        news.setPublished(true);
        news.setPublicationState("archived");
        news.setCanPublish(true);
        newsService.updateNews(news, "root", null, true);
        verify(newsStorage, times(0)).unpublishNews(news.getId());
        verify(newsStorage, times(1)).publishNews(any(News.class));

        when(newsTargetingService.getTargetsByNewsId(news.getId())).thenReturn(targets);
        originalNews.setPublished(true);
        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        news.setTitle("title updated");
        newsService.updateNews(news, "root", null, true);
        verify(newsStorage, times(1)).unpublishNews(news.getId());
        verify(newsStorage, times(2)).publishNews(any(News.class));

        originalNews.setTitle("title updated");
        when(newsStorage.getNewsById(news.getId(), false)).thenReturn(originalNews);
        newsService.updateNews(news, "root", null, false);
        verify(newsStorage, times(2)).unpublishNews(news.getId());
        verify(newsStorage, times(2)).publishNews(any(News.class));
    }
}
