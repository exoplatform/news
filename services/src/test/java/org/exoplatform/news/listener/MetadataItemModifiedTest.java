package org.exoplatform.news.listener;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class MetadataItemModifiedTest {

  @Mock
  private IndexingService       indexingService;

  @Mock
  private NewsService           newsService;

  @Mock
  private CachedActivityStorage activityStorage;

  @Test
  public void testNoInteractionWhenMetadataNotForNews() throws Exception {
    MetadataItemModified metadataItemModified = new MetadataItemModified(newsService, indexingService, activityStorage);
    MetadataItem metadataItem = mock(MetadataItem.class);
    Event<Long, MetadataItem> event = mock(Event.class);
    when(event.getData()).thenReturn(metadataItem);
    when(event.getData().getObjectType()).thenReturn("activity");
    when(metadataItem.getObjectId()).thenReturn("1");

    metadataItemModified.onEvent(event);

    verifyNoInteractions(newsService);
  }

  @Test
  public void testReindexNewsWhenNewsSetAsFavorite() throws Exception {
    MetadataItemModified metadataItemModified = new MetadataItemModified(newsService, indexingService, activityStorage);
    String newsId = "100";

    MetadataItem metadataItem = mock(MetadataItem.class);
    when(metadataItem.getObjectType()).thenReturn(NewsUtils.NEWS_METADATA_OBJECT_TYPE);
    when(metadataItem.getObjectId()).thenReturn(newsId);

    Event<Long, MetadataItem> event = mock(Event.class);
    when(event.getData()).thenReturn(metadataItem);

    News news = new News();
    news.setId(newsId);
    when(newsService.getNewsById(eq(newsId), anyBoolean())).thenReturn(news);

    metadataItemModified.onEvent(event);
    verify(newsService, times(1)).getNewsById(newsId, false);
    verify(indexingService, times(1)).reindex(NewsIndexingServiceConnector.TYPE, newsId);
  }

  @Test
  public void testCleanNewsActivityCacheWhenMarkAsFavorite() throws Exception {
    MetadataItemModified metadataItemModified = new MetadataItemModified(newsService, indexingService, activityStorage);
    String newsId = "200";

    MetadataItem metadataItem = mock(MetadataItem.class);
    when(metadataItem.getObjectType()).thenReturn(NewsUtils.NEWS_METADATA_OBJECT_TYPE);
    when(metadataItem.getObjectId()).thenReturn(newsId);

    Event<Long, MetadataItem> event = mock(Event.class);
    when(event.getData()).thenReturn(metadataItem);

    String activityId = "activityId";
    News news = new News();
    news.setId(newsId);
    news.setActivityId(activityId);
    when(newsService.getNewsById(eq(newsId), anyBoolean())).thenReturn(news);

    metadataItemModified.onEvent(event);
    verify(newsService, times(1)).getNewsById(newsId, false);
    verify(indexingService, times(1)).reindex(NewsIndexingServiceConnector.TYPE, newsId);
    verify(activityStorage, times(1)).clearActivityCached(activityId);
  }

}
