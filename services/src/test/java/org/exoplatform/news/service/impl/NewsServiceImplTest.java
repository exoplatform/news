package org.exoplatform.news.service.impl;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.upload.UploadService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


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

    private MetadataService metadataService;
    
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
}
