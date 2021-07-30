package org.exoplatform.news.activity.processor;

import java.util.HashMap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityNewsProcessor extends BaseActivityProcessorPlugin {

  private NewsService newsService;

  public ActivityNewsProcessor(NewsService newsService, InitParams initParams) {
    super(initParams);
    this.newsService = newsService;
  }

  @Override
  public void processActivity(ExoSocialActivity activity) {
    if (activity.isComment()
        || activity.getType() == null
        || !activity.getTemplateParams().containsKey("newsId")) {
      return;
    }
    if (activity.getLinkedProcessedEntities() == null) {
      activity.setLinkedProcessedEntities(new HashMap<>());
    }
    News news = (News) activity.getLinkedProcessedEntities().get("news");
    if (news == null) {
      news = newsService.getNewsById(activity.getTemplateParams().get("newsId"), false);
      activity.getLinkedProcessedEntities().put("news", news);
    }
  }

}
