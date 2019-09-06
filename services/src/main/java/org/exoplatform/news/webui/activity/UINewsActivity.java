package org.exoplatform.news.webui.activity;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "war:/groovy/news/webui/activity/UINewsActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class) })
public class UINewsActivity extends BaseUIActivity {
  public static final String ACTIVITY_TYPE = "news";

  private News news;

  public News getNews() {
    return news;
  }

  public void setNews(News news) {
    this.news = news;
  }

  public String getUserFullName(String userId) {
    String fullName = "";
    Identity identity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
    if(identity != null) {
      fullName = identity.getProfile().getFullName();
    }
    return fullName;
  }

  public String getUserProfileURL(String userId) {
    return LinkProvider.getUserProfileUri(userId);
  }

  public boolean canEditNews(ExoSocialActivity activity) {
    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
            .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, true);
    String authenticatedUserId = currentUser.getId();
    return authenticatedUserId.equals(activity.getPosterId());
  }

  public boolean canPinNews(ExoSocialActivity activity) {
    return ConversationState.getCurrent().getIdentity().getMemberships()
            .contains(new MembershipEntry("/platform/web-contributors", "publisher"));
  }
}
