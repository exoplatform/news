package org.exoplatform.news.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NewsMetadataListenerTest {

  private static final String METADATA_TYPE_NAME = "testMetadataListener";

  @Mock
  private IndexingService     indexingService;

  @Mock
  private NewsService         newsService;

  @Mock
  private FavoriteService     favoriteService;

  @Mock
  private IdentityManager     identityManager;

  @Mock
  private ActivityManager     activityManager;

  @Test
  public void testReindexNewsWhenNewsSetAsFavorite() throws Exception {
    NewsMetadataListener newsActivityListener = new NewsMetadataListener(indexingService,
                                                                         newsService,
                                                                         favoriteService,
                                                                         identityManager,
                                                                         activityManager);
    setCurrentUser("john");
    MetadataItem metadataItem = mock(MetadataItem.class);
    Event<Long, MetadataItem> event = mock(Event.class);
    lenient().when(event.getData()).thenReturn(metadataItem);
    lenient().when(event.getData().getObjectType()).thenReturn("news");
    lenient().when(metadataItem.getObjectId()).thenReturn("1");

    newsActivityListener.onEvent(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testReindexActivityWhenNewsSetAsFavorite() throws Exception {
    NewsMetadataListener newsActivityListener = new NewsMetadataListener(indexingService,
                                                                         newsService,
                                                                         favoriteService,
                                                                         identityManager,
                                                                         activityManager);
    Identity johnIdentity = new Identity("1", Collections.singletonList(new MembershipEntry("john")));
    ConversationState.setCurrent(new ConversationState(johnIdentity));
    MetadataItem metadataItem = mock(MetadataItem.class);
    Event<Long, MetadataItem> event = mock(Event.class);
    lenient().when(event.getData()).thenReturn(metadataItem);
    lenient().when(event.getData().getObjectType()).thenReturn("activity");
    lenient().when(event.getEventName()).thenReturn("social.metadataItem.created");
    lenient().when(metadataItem.getObjectId()).thenReturn("1");
    News news = new News();
    news.setId("1234");
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType(NewsUtils.NEWS_METADATA_OBJECT_TYPE);
    lenient().when(newsService.getNewsByActivityId("1", johnIdentity)).thenReturn(news);
    org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                     new org.exoplatform.social.core.identity.model.Identity(OrganizationIdentityProvider.NAME,
                                                                                                                             "john");
    userIdentity.setId("1");
    lenient().when(identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "1")).thenReturn(userIdentity);
    lenient().when(activityManager.getActivity("1")).thenReturn(activity);

    newsActivityListener.onEvent(event);
    verify(newsService, times(1)).getNewsByActivityId("1", johnIdentity);
    verify(favoriteService, times(1)).createFavorite(any());
    verify(indexingService, times(1)).reindex(NewsIndexingServiceConnector.TYPE, news.getId());
  }

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }

}
