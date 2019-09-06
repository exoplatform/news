package org.exoplatform.news.webui.activity;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.BaseUIActivityBuilder;

public class UINewsActivityBuilder extends BaseUIActivityBuilder {

  private static final Log LOG = ExoLogger.getLogger(UINewsActivityBuilder.class);

  @Override
  protected void extendUIActivity(BaseUIActivity uiActivity, ExoSocialActivity activity) {
    NewsService newsService = CommonsUtils.getService(NewsService.class);

    try {
      String newsId = activity.getTemplateParams().get("newsId");
      if (newsId != null) {
        News news = newsService.getNews(newsId);
        if(news != null) {
          ((UINewsActivity) uiActivity).setNews(news);
        }
      }
    } catch (Exception e) {
      LOG.error("Error while getting news of activity " + activity.getId(), e);
    }
  }
}
