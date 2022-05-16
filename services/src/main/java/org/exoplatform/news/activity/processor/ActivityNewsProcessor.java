package org.exoplatform.news.activity.processor;

import java.util.HashMap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

public class ActivityNewsProcessor extends BaseActivityProcessorPlugin {

  private static final Log LOG = ExoLogger.getLogger(ActivityNewsProcessor.class);

  private NewsService      newsService;

  private ActivityManager  activityManager;

  public ActivityNewsProcessor(ActivityManager activityManager, NewsService newsService, InitParams initParams) {
    super(initParams);
    this.newsService = newsService;
    this.activityManager = activityManager;
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
      try {
        news = newsService.getNewsById(activity.getTemplateParams().get("newsId"), false);

        RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity, true);
        news.setCommentsCount(listAccess.getSize());
        news.setLikesCount(activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length);

        activity.setMetadataObjectId(news.getId());
        activity.setMetadataObjectType(NewsUtils.NEWS_METADATA_OBJECT_TYPE);
      } catch (Exception e) {
        LOG.warn("Error retrieving news with id {}",
                 activity.getTemplateParams().get("newsId"),
                 e);
      }
      activity.getLinkedProcessedEntities().put("news", news);
    }
  }

}
