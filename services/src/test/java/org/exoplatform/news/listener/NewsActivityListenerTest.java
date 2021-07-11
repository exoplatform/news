package org.exoplatform.news.listener;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

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
  public void testNotShareWhenSharedActivityWhenNewsNotFound() throws IllegalAccessException, ObjectNotFoundException {
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

    newsActivityListener.shareActivity(event);

    verify(newsService, times(1)).getNewsById(newsId, false);
    verify(newsService, never()).shareNews(nullable(News.class), nullable(Space.class), nullable(Identity.class));
  }

  @Test
  public void testShareWhenNewsFound() throws IllegalAccessException, ObjectNotFoundException {
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

    News news = new News();
    when(newsService.getNewsById(newsId, false)).thenReturn(news);

    newsActivityListener.shareActivity(event);

    verify(newsService, times(1)).getNewsById(newsId, false);
    verify(newsService, times(1)).shareNews(eq(news), nullable(Space.class), nullable(Identity.class));
  }

}
