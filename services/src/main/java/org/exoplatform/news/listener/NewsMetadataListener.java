package org.exoplatform.news.listener;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.processor.MetadataActivityProcessor;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.model.MetadataItem;

public class NewsMetadataListener extends Listener<Long, MetadataItem> {

  private final IndexingService indexingService;

  private final NewsService     newsService;

  private final FavoriteService favoriteService;

  private final IdentityManager identityManager;

  private final ActivityManager activityManager;

  private static final String   METADATA_CREATED = "social.metadataItem.created";

  private static final String   METADATA_DELETED = "social.metadataItem.deleted";

  public NewsMetadataListener(IndexingService indexingService,
                              NewsService newsService,
                              FavoriteService favoriteService,
                              IdentityManager identityManager,
                              ActivityManager activityManager) {
    this.indexingService = indexingService;
    this.newsService = newsService;
    this.favoriteService = favoriteService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
  }

  @Override
  public void onEvent(Event<Long, MetadataItem> event) throws Exception {
    ConversationState conversationstate = ConversationState.getCurrent();
    Identity currentIdentity = conversationstate == null
        || conversationstate.getIdentity() == null ? null : conversationstate.getIdentity();
    MetadataItem metadataItem = event.getData();
    String objectType = event.getData().getObjectType();
    String objectId = metadataItem.getObjectId();
    if (StringUtils.equals(objectType, NewsUtils.NEWS_METADATA_OBJECT_TYPE)) {
      indexingService.reindex(NewsIndexingServiceConnector.TYPE, objectId);
    } else if (StringUtils.equals(objectType, MetadataActivityProcessor.ACTIVITY_METADATA_OBJECT_TYPE)) {
      ExoSocialActivity activity = activityManager.getActivity(objectId);
      if (activity.getType().equals(NewsUtils.NEWS_METADATA_OBJECT_TYPE) && currentIdentity != null) {
        News news = newsService.getNewsByActivityId(objectId, currentIdentity);
        org.exoplatform.social.core.identity.model.Identity userIdentity =
                                                                         identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                                             currentIdentity.getUserId());
        Favorite favorite = new Favorite(NewsUtils.NEWS_METADATA_OBJECT_TYPE,
                                         news.getId(),
                                         "",
                                         Long.parseLong(userIdentity.getId()));
        if (event.getEventName().equals(METADATA_CREATED)) {
          favoriteService.createFavorite(favorite);
        } else if (event.getEventName().equals(METADATA_DELETED)) {
          favoriteService.deleteFavorite(favorite);
        }
        indexingService.reindex(NewsIndexingServiceConnector.TYPE, news.getId());
      }
    }
  }
}
