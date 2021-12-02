package org.exoplatform.news.utils;

import javax.ws.rs.core.UriInfo;

import org.exoplatform.news.rest.NewsSearchResultEntity;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.exoplatform.social.metadata.favorite.model.Favorite;
public class EntityBuilder {
  public static final NewsSearchResultEntity fromNewsSearchResult(FavoriteService favoriteService,
                                                                  NewsESSearchResult newsESSearchResult,
                                                                  Identity currentIdentity,
                                                                  UriInfo uriInfo) {
    NewsSearchResultEntity newsSearchResultEntity = new NewsSearchResultEntity(newsESSearchResult);
    newsSearchResultEntity.setPoster(org.exoplatform.social.rest.api.EntityBuilder.buildEntityIdentity(newsESSearchResult.getPoster(),
                                                                                                       uriInfo.getPath(),
                                                                                                       "all"));
    Favorite favorite = new Favorite(NewsUtils.NEWS_METADATA_OBJECT_TYPE,
                                     newsESSearchResult.getId(),
                                     null,
                                     Long.parseLong(currentIdentity.getId()));
    newsSearchResultEntity.setFavorite(favoriteService.isFavorite(favorite));

    return newsSearchResultEntity;
  }
}
