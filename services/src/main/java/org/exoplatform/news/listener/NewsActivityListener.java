package org.exoplatform.news.listener;

import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * A triggered listener class about activity lifecyles. This class is used to
 * propagate sharing activity in News elements to let targeted space members to
 * access News in JCR (especially for illustration)
 */
public class NewsActivityListener extends ActivityListenerPlugin {

  private static final Log    LOG     = ExoLogger.getLogger(NewsActivityListener.class);

  private static final String NEWS_ID = "newsId";

  private ActivityManager     activityManager;

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  private NewsService         newsService;

  public NewsActivityListener(ActivityManager activityManager,
                              IdentityManager identityManager,
                              SpaceService spaceService,
                              NewsService newsService) {
    this.newsService = newsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
  }

  @Override
  public void shareActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity sharedActivity = event.getActivity();
    if (sharedActivity != null && sharedActivity.getTemplateParams() != null
        && sharedActivity.getTemplateParams().containsKey("originalActivityId")) {
      String originalActivityId = sharedActivity.getTemplateParams().get("originalActivityId");
      ExoSocialActivity originalActivity = activityManager.getActivity(originalActivityId);
      if (originalActivity != null && originalActivity.getTemplateParams() != null
          && originalActivity.getTemplateParams().containsKey(NEWS_ID)) {
        String newsId = originalActivity.getTemplateParams().get(NEWS_ID);
        org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        try {
          News news = newsService.getNewsById(newsId, currentIdentity, false);
          if (news != null) {
            Identity posterIdentity = getIdentity(sharedActivity);
            Space space = getSpace(sharedActivity);
            newsService.shareNews(news, space, posterIdentity, sharedActivity.getId());
          }
        } catch (Exception e) {
          LOG.error("Error while sharing news {} to activity {}", newsId, sharedActivity.getId(), e);
        }
      }
    }
  }

  @Override
  public void likeActivity(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    if (activity != null && activity.getTemplateParams() != null && activity.getTemplateParams().containsKey(NEWS_ID)) {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      try {
        News news = newsService.getNewsByActivityId(activity.getId(), currentIdentity);
        NewsUtils.broadcastEvent(NewsUtils.LIKE_NEWS, currentIdentity.getUserId(), news);
      } catch (Exception e) {
        LOG.error("Error broadcast like news event", e);
      }
    }
  }

  @Override
  public void saveComment(ActivityLifeCycleEvent event) {
    ExoSocialActivity activity = event.getActivity();
    if (activity != null && activity.getTemplateParams() != null && activity.getTemplateParams().containsKey(NEWS_ID)) {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      try {
        News news = newsService.getNewsByActivityId(activity.getParentId(), currentIdentity);
        NewsUtils.broadcastEvent(NewsUtils.COMMENT_NEWS, currentIdentity.getUserId(), news);
      } catch (Exception e) {
        LOG.error("Error broadcast comment news event", e);
      }
    }
  }

  private Identity getIdentity(ExoSocialActivity sharedActivity) {
    String posterIdentityId = sharedActivity.getPosterId();
    return identityManager.getIdentity(posterIdentityId);
  }

  private Space getSpace(ExoSocialActivity sharedActivity) {
    String spacePrettyName = sharedActivity.getActivityStream().getPrettyId();
    return spaceService.getSpaceByPrettyName(spacePrettyName);
  }

}
