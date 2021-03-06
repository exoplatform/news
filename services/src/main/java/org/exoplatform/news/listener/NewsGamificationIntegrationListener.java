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
  private static final Log   LOG                                         =
                                 ExoLogger.getLogger(NewsGamificationIntegrationListener.class);

  public static final String GAMIFICATION_GENERIC_EVENT                   = "exo.gamification.generic.action";

  public static final String GAMIFICATION_POST_NEWS_ARTICLE_RULE_TITLE    = "PostArticle";

  public static final String GAMIFICATION_PUBLISH_NEWS_ARTICLE_RULE_TITLE = "PublishArticle";

  private PortalContainer    container;

  private ListenerService    listenerService;

  private NewsService        newsService;

  public NewsGamificationIntegrationListener(PortalContainer container, ListenerService listenerService, NewsService newsService) {
    this.container = container;
    this.listenerService = listenerService;
    this.newsService = newsService;
  }

  @Override
  public void onEvent(Event<String, String> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      String eventName = event.getEventName();
      String newsId = event.getSource();
      String earnerId = event.getData();
      News news = newsService.getNewsById(newsId, false);
      String ruleTitle = "";
      if (StringUtils.equals(eventName, NewsUtils.POST_NEWS)) {
        ruleTitle = GAMIFICATION_POST_NEWS_ARTICLE_RULE_TITLE;
      } else if (StringUtils.equals(eventName, NewsUtils.PUBLISH_NEWS)) {
        ruleTitle = GAMIFICATION_PUBLISH_NEWS_ARTICLE_RULE_TITLE;
      }
      try {
        Map<String, String> gamificationMap = new HashMap<>();
        gamificationMap.put("ruleTitle", ruleTitle);
        gamificationMap.put("object", news.getUrl());
        gamificationMap.put("senderId", earnerId); // matches the gamification's earner id
        gamificationMap.put("receiverId", earnerId);
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gamificationMap, news.getId());
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }
    } finally {
      RequestLifeCycle.end();
    }
  }
}
