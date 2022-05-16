package org.exoplatform.news.listener;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NewsActivityListenerTest {

  @Mock
  private ActivityManager activityManager;

  @Mock
  private IdentityManager identityManager;

  @Mock
  private SpaceService    spaceService;

  @Mock
  private NewsService     newsService;

  @Test
  public void testNotShareWhenActivityNotFound() {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    newsActivityListener.shareActivity(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testNotShareWhenActivityNotHavingTemplates() {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    newsActivityListener.shareActivity(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testNotShareWhenActivityNotSharedOne() {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    Map<String, String> templateParams = mock(Map.class);
    when(activity.getTemplateParams()).thenReturn(templateParams);

    newsActivityListener.shareActivity(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testNotShareWhenSharedActivityNotFound() {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    Map<String, String> templateParams = mock(Map.class);
    when(activity.getTemplateParams()).thenReturn(templateParams);
    when(templateParams.containsKey("originalActivityId")).thenReturn(true);

    String originalActivityId = "originalActivityId";
    when(templateParams.get("originalActivityId")).thenReturn(originalActivityId);

    newsActivityListener.shareActivity(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testNotShareWhenSharedActivityNotNewsActivity() {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    Map<String, String> templateParams = mock(Map.class);
    when(activity.getTemplateParams()).thenReturn(templateParams);
    when(templateParams.containsKey("originalActivityId")).thenReturn(true);

    String originalActivityId = "originalActivityId";
    when(templateParams.get("originalActivityId")).thenReturn(originalActivityId);

    ExoSocialActivity sharedActivity = mock(ExoSocialActivity.class);
    when(activityManager.getActivity(originalActivityId)).thenReturn(sharedActivity);

    newsActivityListener.shareActivity(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testNotShareWhenSharedActivityWhenNewsNotFound() throws Exception {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    Map<String, String> templateParams = mock(Map.class);
    when(activity.getTemplateParams()).thenReturn(templateParams);
    when(templateParams.containsKey("originalActivityId")).thenReturn(true);

    String originalActivityId = "originalActivityId";
    when(templateParams.get("originalActivityId")).thenReturn(originalActivityId);

    ExoSocialActivity sharedActivity = mock(ExoSocialActivity.class);
    when(activityManager.getActivity(originalActivityId)).thenReturn(sharedActivity);

    Map<String, String> sharedTemplateParams = mock(Map.class);
    when(sharedActivity.getTemplateParams()).thenReturn(sharedTemplateParams);
    when(sharedTemplateParams.containsKey("newsId")).thenReturn(true);

    String newsId = "newsId";
    when(sharedTemplateParams.get("newsId")).thenReturn(newsId);
    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    newsActivityListener.shareActivity(event);

    verify(newsService, times(1)).getNewsById(newsId, currentIdentity, false);
    verify(newsService, never()).shareNews(nullable(News.class),
                                           nullable(Space.class),
                                           nullable(Identity.class),
                                           nullable(String.class));
  }

  @Test
  public void testShareWhenNewsFound() throws Exception {
    NewsActivityListener newsActivityListener = new NewsActivityListener(activityManager,
                                                                         identityManager,
                                                                         spaceService,
                                                                         newsService);

    ActivityLifeCycleEvent event = mock(ActivityLifeCycleEvent.class);
    ExoSocialActivity activity = mock(ExoSocialActivity.class);
    when(event.getActivity()).thenReturn(activity);
    Map<String, String> templateParams = mock(Map.class);
    when(activity.getTemplateParams()).thenReturn(templateParams);
    when(templateParams.containsKey("originalActivityId")).thenReturn(true);

    String originalActivityId = "originalActivityId";
    when(templateParams.get("originalActivityId")).thenReturn(originalActivityId);

    ExoSocialActivity sharedActivity = mock(ExoSocialActivity.class);
    when(activityManager.getActivity(originalActivityId)).thenReturn(sharedActivity);

    ActivityStream activityStream = mock(ActivityStream.class);
    when(activity.getActivityStream()).thenReturn(activityStream);

    String spacePrettyName = "space1";
    when(activityStream.getPrettyId()).thenReturn(spacePrettyName);

    Map<String, String> sharedTemplateParams = mock(Map.class);
    when(sharedActivity.getTemplateParams()).thenReturn(sharedTemplateParams);
    when(sharedTemplateParams.containsKey("newsId")).thenReturn(true);

    String newsId = "newsId";
    when(sharedTemplateParams.get("newsId")).thenReturn(newsId);

    org.exoplatform.services.security.Identity currentIdentity = new org.exoplatform.services.security.Identity("john");
    ConversationState.setCurrent(new ConversationState(currentIdentity));

    News news = new News();
    when(newsService.getNewsById(newsId, currentIdentity, false)).thenReturn(news);

    newsActivityListener.shareActivity(event);

    verify(newsService, times(1)).getNewsById(newsId, currentIdentity, false);
    verify(newsService, times(1)).shareNews(eq(news), nullable(Space.class), nullable(Identity.class), nullable(String.class));
  }
}
