package org.exoplatform.news.activity.processor;

import java.util.HashMap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityNewsProcessor extends BaseActivityProcessorPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(ActivityNewsProcessor.class);

  private NewsService      newsService;

  private static final String NEWS_ID = "newsId";
  
  private static final String NEWS_ACTIVITY_FIELD = "news";

  public ActivityNewsProcessor(NewsService newsService, InitParams initParams) {
    super(initParams);
    this.newsService = newsService;
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity.isComment() || activity.getType() == null || !activity.getTemplateParams().containsKey(NEWS_ID)) {
      return;
    }
    if (activity.getLinkedProcessedEntities() == null) {
      activity.setLinkedProcessedEntities(new HashMap<>());
    }
    News news = (News) activity.getLinkedProcessedEntities().get(NEWS_ACTIVITY_FIELD);
    if (news == null) {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      try {
        news = newsService.getNewsById(activity.getTemplateParams().get(NEWS_ID), currentIdentity, false);
      } catch (IllegalAccessException e) {
        LOG.warn("User {} attempt to access unauthorized news with id {}",
                 currentIdentity.getUserId(),
                 activity.getTemplateParams().get(NEWS_ID),
                 e);
      }
      activity.getLinkedProcessedEntities().put(NEWS_ACTIVITY_FIELD, news);
    }
  }

}