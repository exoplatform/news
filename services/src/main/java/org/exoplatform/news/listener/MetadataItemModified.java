/*
 * Copyright (C) 2003-2022 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.listener;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.metadata.model.MetadataItem;

public class MetadataItemModified extends Listener<Long, MetadataItem> {

  private IndexingService       indexingService;

  private NewsService           newsService;

  private CachedActivityStorage cachedActivityStorage;

  public MetadataItemModified(NewsService newsService, IndexingService indexingService, ActivityStorage activityStorage) {
    this.newsService = newsService;
    this.indexingService = indexingService;
    if (activityStorage instanceof CachedActivityStorage) {
      this.cachedActivityStorage = (CachedActivityStorage) activityStorage;
    }
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    MetadataItem metadataItem = event.getData();
    String objectType = metadataItem.getObjectType();
    String objectId = metadataItem.getObjectId();
    if (isNewsEvent(objectType)) {
      // Ensure to re-execute all ActivityProcessors to compute & cache
      // metadatas of the activity again
      News news = newsService.getNewsById(objectId, false);
      if (news != null) {
        if (StringUtils.isNotBlank(news.getActivityId())) {
          clearCache(news.getActivityId());
        }
        reindexNews(objectId);
      }
    }
  }

  protected boolean isNewsEvent(String objectType) {
    return StringUtils.equals(objectType, NewsUtils.NEWS_METADATA_OBJECT_TYPE);
  }

  private void clearCache(String activityId) {
    if (cachedActivityStorage != null) {
      cachedActivityStorage.clearActivityCached(activityId);
    }
  }

  private void reindexNews(String newsId) {
    indexingService.reindex(NewsIndexingServiceConnector.TYPE, newsId);
  }

}
