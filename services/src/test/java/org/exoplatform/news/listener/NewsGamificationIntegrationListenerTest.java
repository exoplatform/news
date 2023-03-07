package org.exoplatform.news.listener;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;

@RunWith(MockitoJUnitRunner.class)
public class NewsGamificationIntegrationListenerTest {

  @Mock
  ListenerService listenerService;

  @Mock
  NewsService     newsService;

  @Test
  public void testAddGamificationPointsAfterCreatingAnArticle() throws Exception { // NOSONAR
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
