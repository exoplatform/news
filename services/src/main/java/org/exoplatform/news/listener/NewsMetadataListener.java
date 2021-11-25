package org.exoplatform.news.listener;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.NewsUtils;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.metadata.model.MetadataItem;

public class NewsMetadataListener extends Listener<Long, MetadataItem> {

  private final IndexingService indexingService;

  private final NewsService newsService;

  public NewsMetadataListener(IndexingService indexingService, NewsService newsService) {
    this.indexingService = indexingService;
    this.newsService = newsService;
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    ConversationState conversationstate = ConversationState.getCurrent();
    final String currentUsername = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity().getUserId();
    MetadataItem metadataItem = event.getData();
    String objectType = event.getData().getObjectType();
    String objectId = metadataItem.getObjectId();
    if (StringUtils.equals(objectType, NewsUtils.NEWS_METADATA_OBJECT_TYPE)) {
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, objectId);
    } else if (StringUtils.equals(objectType, MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE)) {
      News news = newsService.getNewsByActivityId(objectId, currentUsername);
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, news.getId());
    }
  }

}
