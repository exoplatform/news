package org.exoplatform.news.listener;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.NewsUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.HashMap;
import java.util.Map;

public class NewsGamificationIntegrationListener extends Listener<String, String> {
  private static final Log   LOG                                 = ExoLogger.getLogger(NewsGamificationIntegrationListener.class);

  public static final String GAMIFICATION_GENERIC_EVENT          = "exo.gamification.generic.action";

  public static final String GAMIFICATION_CREATE_NEWS_RULE_TITLE = "PostNews";

  private PortalContainer    container;

  private ListenerService    listenerService;

  private NewsService        newsService;

  public NewsGamificationIntegrationListener(PortalContainer container, ListenerService listenerService) {
    this.container = container;
    this.listenerService = listenerService;
  }

  @Override
  public void onEvent(Event<String, String> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      String eventName = event.getEventName();
      String newsId = event.getSource();
      String earnerId = event.getData();
      News news = getNewsService().getNewsById(newsId);
      String ruleTitle = "";
      if (StringUtils.equals(eventName, NewsUtils.POST_ARTICLE_NEWS)) {
        ruleTitle = GAMIFICATION_CREATE_NEWS_RULE_TITLE;
      }
      try {
        Map<String, String> gam = new HashMap<>();
        gam.put("ruleTitle", ruleTitle);
        gam.put("object", NewsUtils.getNewsURL(news));
        gam.put("senderId", String.valueOf(earnerId)); // matches the gamification's earner id
        gam.put("receiverId", String.valueOf(earnerId));
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gam, String.valueOf(news.getId()));
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

  public NewsService getNewsService() {
    if (newsService == null) {
      newsService = ExoContainerContext.getService(NewsService.class);
    }
    return newsService;
  }
}
