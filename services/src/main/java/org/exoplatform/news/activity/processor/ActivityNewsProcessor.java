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

  private NewsService      newsService;

  private static final Log LOG = ExoLogger.getLogger(ActivityNewsProcessor.class);

  public ActivityNewsProcessor(NewsService newsService, InitParams initParams) {
    super(initParams);
    this.newsService = newsService;
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity.isComment() || activity.getType() == null || !activity.getTemplateParams().containsKey("newsId")) {
      return;
    }
    if (activity.getLinkedProcessedEntities() == null) {
      activity.setLinkedProcessedEntities(new HashMap<>());
    }
    News news = (News) activity.getLinkedProcessedEntities().get("news");
    if (news == null) {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      try {
        news = newsService.getNewsById(activity.getTemplateParams().get("newsId"), currentIdentity, false);
      } catch (IllegalAccessException e) {
        LOG.warn("User {} attempt to access unauthorized news with id {}",
                 currentIdentity.getUserId(),
                 activity.getTemplateParams().get("newsId"),
                 e);
      }
      activity.getLinkedProcessedEntities().put("news", news);
    }
  }

}