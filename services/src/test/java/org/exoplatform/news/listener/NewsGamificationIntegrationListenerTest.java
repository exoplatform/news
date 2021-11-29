package org.exoplatform.news.listener;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.jcr.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(CommonsUtils.class)
public class NewsGamificationIntegrationListenerTest {

  @Mock
  ListenerService listenerService;

  @Mock
  NewsService     newsService;

  @Mock
  RepositoryService repositoryService;

  @Mock
  SessionProviderService sessionProviderService;

  @Mock
  ManageableRepository repository;

  @Mock
  RepositoryEntry repositoryEntry;

  @Mock
  SessionProvider sessionProvider;

  @Mock
  Session session;
  
  @Test
  public void testAddGamificationPointsAfterCreatingAnArticle() throws Exception { // NOSONAR
    when(sessionProviderService.getSystemSessionProvider(any())).thenReturn(sessionProvider);
    when(sessionProviderService.getSessionProvider(any())).thenReturn(sessionProvider);
    when(repositoryService.getCurrentRepository()).thenReturn(repository);
    when(repository.getConfiguration()).thenReturn(repositoryEntry);
    when(repositoryEntry.getDefaultWorkspaceName()).thenReturn("collaboration");
    when(sessionProvider.getSession(any(), any())).thenReturn(session);
    News news = new News();
    news.setTitle("title");
    news.setAuthor("jean");
    news.setId("id123");
    news.setSpaceId("3");
    news.setActivities("3:39;1:11");
    news.setActivityId("10");

    AtomicBoolean executeListener = new AtomicBoolean(true);
    listenerService.addListener(NewsUtils.POST_NEWS, new Listener<Long, Long>() {
      @Override
      public void onEvent(Event<Long, Long> event) throws Exception {
        executeListener.set(true);
      }
    });
    newsService.postNews(news, "root");
    assertTrue(executeListener.get());
  }

}
