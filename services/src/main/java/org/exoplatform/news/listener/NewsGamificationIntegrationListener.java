package org.exoplatform.news.listener;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.news.model.News;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.HashMap;
import java.util.Map;

public class NewsGamificationIntegrationListener extends Listener<String, News> {
  private static final Log   LOG                                         =
                                 ExoLogger.getLogger(NewsGamificationIntegrationListener.class);

  public static final String GAMIFICATION_GENERIC_EVENT                   = "exo.gamification.generic.action";

  public static final String GAMIFICATION_POST_NEWS_ARTICLE_RULE_TITLE    = "PostArticle";

  public static final String GAMIFICATION_PUBLISH_NEWS_ARTICLE_RULE_TITLE = "PublishArticle";

  String ACTIVITY_OBJECT_TYPE                                       = "news";

  String OBJECT_ID_PARAM                                            = "objectId";

  String OBJECT_TYPE_PARAM                                          = "objectType";


  private PortalContainer    container;

  private ListenerService    listenerService;

  public NewsGamificationIntegrationListener(PortalContainer container, ListenerService listenerService) {
    this.container = container;
    this.listenerService = listenerService;
  }

  @Override
  public void onEvent(Event<String, News> event) throws Exception {
    ExoContainerContext.setCurrentContainer(container);
    RequestLifeCycle.begin(container);
    try {
      String eventName = event.getEventName();
      News news = event.getData();
      String ruleTitle = "";
      if (StringUtils.equals(eventName, NewsUtils.POST_NEWS_ARTICLE)) {
        ruleTitle = GAMIFICATION_POST_NEWS_ARTICLE_RULE_TITLE;
      } else if (StringUtils.equals(eventName, NewsUtils.PUBLISH_NEWS)) {
        ruleTitle = GAMIFICATION_PUBLISH_NEWS_ARTICLE_RULE_TITLE;
      }
      try {
        Map<String, String> gamificationMap = new HashMap<>();
        gamificationMap.put("ruleTitle", ruleTitle);
        gamificationMap.put("object", news.getUrl());
        gamificationMap.put("senderId", news.getAuthor()); // matches the gamification's earner id
        gamificationMap.put("receiverId", news.getAuthor());
        gamificationMap.put(OBJECT_ID_PARAM, news.getActivityId());
        gamificationMap.put(OBJECT_TYPE_PARAM, ACTIVITY_OBJECT_TYPE);
        listenerService.broadcast(GAMIFICATION_GENERIC_EVENT, gamificationMap, news.getId());
      } catch (Exception e) {
        LOG.error("Cannot broadcast gamification event");
      }
    } finally {
      RequestLifeCycle.end();
    }
  }
}
