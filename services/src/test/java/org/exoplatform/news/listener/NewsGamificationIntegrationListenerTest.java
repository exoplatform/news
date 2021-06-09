package org.exoplatform.news.listener;

import static org.junit.Assert.assertTrue;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.NewsUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(CommonsUtils.class)
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
    newsService.createNews(news);
    assertTrue(executeListener.get());
  }

}
