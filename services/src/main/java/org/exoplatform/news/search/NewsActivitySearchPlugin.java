package org.exoplatform.news.search;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.webui.activity.UINewsActivity;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.jpa.search.ActivitySearchPlugin;

/**
 * A search plugin to index news content in generated activity index
 */
public class NewsActivitySearchPlugin extends ActivitySearchPlugin {

  private static final Log    LOG                        = ExoLogger.getLogger(NewsActivitySearchPlugin.class);

  private static final String ACTIVITY_PARAMETER_NEWS_ID = "newsId";

  @Override
  public String getActivityType() {
    return UINewsActivity.ACTIVITY_TYPE;
  }

  @Override
  public void index(ExoSocialActivity activity, Document document) {
    if (activity != null && activity.getTemplateParams() != null
        && activity.getTemplateParams().containsKey(ACTIVITY_PARAMETER_NEWS_ID)) {
      NewsService newsService = ExoContainerContext.getService(NewsService.class);
      String newsId = activity.getTemplateParams().get(ACTIVITY_PARAMETER_NEWS_ID);

      SessionProvider systemProvider = SessionProvider.createSystemProvider();
      SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
      sessionProviderService.setSessionProvider(null, systemProvider);
      try {
        News news = newsService.getNewsById(newsId);
        StringBuilder newsContent = new StringBuilder(news.getTitle());
        newsContent.append("\n");
        newsContent.append(news.getSummary());
        newsContent.append("\n");
        newsContent.append(news.getBody());
        document.addField("body", newsContent.toString());
      } catch (Exception e) {
        LOG.error("Error getting news content with id {}", newsId, e);
      } finally {
        sessionProviderService.removeSessionProvider(null);
      }
    }
  }
}
