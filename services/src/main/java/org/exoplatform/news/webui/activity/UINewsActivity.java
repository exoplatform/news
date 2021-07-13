package org.exoplatform.news.webui.activity;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.NewsUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

import org.exoplatform.webui.event.Event;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "war:/groovy/news/webui/activity/UINewsActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = UINewsActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = UINewsActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.HideActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class) })
public class UINewsActivity extends BaseUIActivity {
  public static final String  ACTIVITY_TYPE                   = "news";

  private final static String PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private final static String PLATFORM_ADMINISTRATORS_GROUP   = "/platform/administrators";

  private static final String MANAGER_MEMBERSHIP_NAME         = "manager";

  private News                news;

  public News getNews() {
    return news;
  }

  public void setNews(News news) {
    this.news = news;
  }

  public String getUserFullName(String userId) {
    String fullName = "";
    Identity identity = CommonsUtils.getService(IdentityManager.class)
                                    .getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
    if (identity != null) {
      fullName = identity.getProfile().getFullName();
    }
    return fullName;
  }

  public String getUserProfileURL(String userId) {
    return LinkProvider.getUserProfileUri(userId);
  }

  public boolean canEditNews(ExoSocialActivity activity) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    String authenticatedUser = currentIdentity.getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    String authenticatedUserId = currentUser.getId();
    SpaceService spaceService = Utils.getSpaceService();
    Space currentSpace = spaceService.getSpaceById(news.getSpaceId());
    return authenticatedUserId.equals(activity.getPosterId()) || spaceService.isSuperManager(authenticatedUser)
        || currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
        || currentIdentity.isMemberOf(currentSpace.getGroupId(), MANAGER_MEMBERSHIP_NAME);
  }

  public boolean canPinNews(ExoSocialActivity activity) {
    return ConversationState.getCurrent().getIdentity().isMemberOf(PLATFORM_ADMINISTRATORS_GROUP, "*")
        || ConversationState.getCurrent().getIdentity().isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }

  public static class LikeActivityActionListener extends BaseUIActivity.LikeActivityActionListener {

    public void execute(Event<BaseUIActivity> event) throws Exception {
      super.execute(event);
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      boolean isLiked = Boolean.parseBoolean(isLikedStr);
      if (isLiked) {
        BaseUIActivity uiActivity = event.getSource();
        ExoSocialActivity activity = uiActivity.getActivity();
        if (activity != null) {
          if (activity.getTemplateParams() != null) {
            if (activity.getTemplateParams().get("newsId") != null) {
              String newsId = activity.getTemplateParams().get("newsId");
              NewsService newsService = CommonsUtils.getService(NewsService.class);
              News news = newsService.getNewsById(newsId, false);
              NewsUtils.broadcastEvent(NewsUtils.LIKE_NEWS, null, news);
            }
          }
        }
      }
    }
  }

  public static class PostCommentActionListener extends BaseUIActivity.PostCommentActionListener {

    public void execute(Event<BaseUIActivity> event) throws Exception {
      super.execute(event);
      BaseUIActivity uiActivity = event.getSource();
      ExoSocialActivity activity = uiActivity.getActivity();
      if (activity != null) {
        if (activity.getTemplateParams() != null) {
          if (activity.getTemplateParams().get("newsId") != null) {
            String newsId = activity.getTemplateParams().get("newsId");
            NewsService newsService = CommonsUtils.getService(NewsService.class);
            News news = newsService.getNewsById(newsId, false);
            NewsUtils.broadcastEvent(NewsUtils.COMMENT_NEWS, null, news);
          }
        }
      }
    }
  }
}
