package org.exoplatform.news.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;


import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NewsMetadataListenerTest{

  private static final String METADATA_TYPE_NAME = "testMetadataListener";

  @Mock
  private IndexingService     indexingService;

  @Mock
  private NewsService newsService;

  @Test
  public void testReindexNewsWhenNewsSetAsFavorite() throws Exception {
    NewsMetadataListener newsActivityListener = new NewsMetadataListener(indexingService, newsService);
    setCurrentUser("john");
    MetadataItem metadataItem =  mock(MetadataItem.class);
    Event<Long, MetadataItem> event = mock(Event.class);
    lenient().when(event.getData()).thenReturn(metadataItem);
    lenient().when(event.getData().getObjectType()).thenReturn("news");
    lenient().when(metadataItem.getObjectId()).thenReturn("1");

    newsActivityListener.onEvent(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testReindexActivityWhenNewsSetAsFavorite() throws Exception {
    NewsMetadataListener newsActivityListener = new NewsMetadataListener(indexingService, newsService);
    Identity johnIdentity = new Identity("1", Collections.singletonList(new MembershipEntry("john")));
    ConversationState.setCurrent(new ConversationState(johnIdentity));
    MetadataItem metadataItem =  mock(MetadataItem.class);
    Event<Long, MetadataItem> event = mock(Event.class);
    lenient().when(event.getData()).thenReturn(metadataItem);
    lenient().when(event.getData().getObjectType()).thenReturn("activity");
    lenient().when(metadataItem.getObjectId()).thenReturn("1");
    News news = new News();
    news.setId("1234");
    lenient().when(newsService.getNewsByActivityId("1", johnIdentity)).thenReturn(news);

    newsActivityListener.onEvent(event);
  }

  private void setCurrentUser(final String name) {
    ConversationState.setCurrent(new ConversationState(new org.exoplatform.services.security.Identity(name)));
  }

}