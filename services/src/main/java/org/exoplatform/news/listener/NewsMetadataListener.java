package org.exoplatform.news.listener;

import org.apache.commons.collections.MapUtils;
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
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

import java.util.Set;

public class NewsMetadataListener extends Listener<Long, MetadataItem> {

  public static final String NEWS_METADATA_OBJECT_TYPE = "news";

  private final IndexingService indexingService;

  private final NewsService     newsService;

  private final FavoriteService favoriteService;

  private final IdentityManager identityManager;

  private final ActivityManager activityManager;

  private TagService tagService;


  private static final String   METADATA_CREATED = "social.metadataItem.created";

  private static final String   METADATA_DELETED = "social.metadataItem.deleted";

  private static final String   METADATA_TAG = "tags";

  private static final String   METADATA_FAVORITE = "favorite";


  public NewsMetadataListener(IndexingService indexingService,
                              NewsService newsService,
                              FavoriteService favoriteService,
                              IdentityManager identityManager,
                              ActivityManager activityManager,
                              TagService tagService) {
    this.indexingService = indexingService;
    this.newsService = newsService;
    this.favoriteService = favoriteService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
    this.tagService = tagService;
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
          if (!metadataItem.getObjectType().equals(NEWS_METADATA_OBJECT_TYPE) && metadataItem.getMetadataTypeName().equals(METADATA_TAG)) {
            updateActivityTags(activity, news);
          }
          else {
            favoriteService.createFavorite(favorite);
          }
        } else if (event.getEventName().equals(METADATA_DELETED) && metadataItem.getMetadataTypeName().equals(METADATA_FAVORITE)) {
          favoriteService.deleteFavorite(favorite);
        }
        indexingService.reindex(NewsIndexingServiceConnector.TYPE, news.getId());
      }
    }
  }
  private void updateActivityTags(ExoSocialActivity activity, News news) {
    String objectType = NEWS_METADATA_OBJECT_TYPE;

    long creatorId = getPosterId(activity);

    org.exoplatform.social.core.identity.model.Identity audienceIdentity = activityManager.getActivityStreamOwnerIdentity(activity.getId());
    long audienceId = Long.parseLong(audienceIdentity.getId());
    String content = getActivityBody(activity);

    Set<TagName> tagNames = tagService.detectTagNames(content);
    tagService.saveTags(new TagObject(objectType,
                    news.getId(),
                    activity.getParentId()),
            tagNames,
            audienceId,
            creatorId);
  }
  private long getPosterId(ExoSocialActivity activity) {
    String userId = activity.getUserId();
    if (StringUtils.isBlank(userId)) {
      userId = activity.getPosterId();
    }
    return StringUtils.isBlank(userId) ? 0 : Long.parseLong(userId);
  }

  private String getActivityBody(ExoSocialActivity activity) {
    String body = MapUtils.getString(activity.getTemplateParams(), "comment");
    if (StringUtils.isNotBlank(body)) {
      return body;
    } else if (StringUtils.isNotBlank(activity.getTitle())) {
      return activity.getTitle();
    } else {
      return activity.getBody();
    }
  }

}
