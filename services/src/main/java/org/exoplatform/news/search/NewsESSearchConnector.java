/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.search;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.exoplatform.commons.search.es.ElasticSearchException;
import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;

public class NewsESSearchConnector {
  private static final Log             LOG                          = ExoLogger.getLogger(NewsESSearchConnector.class);

  private static final String          SEARCH_QUERY_FILE_PATH_PARAM = "query.file.path";

  private final ConfigurationManager   configurationManager;

  private final IdentityManager        identityManager;
  
  private final ActivityStorage        activityStorage;

  private final ElasticSearchingClient client;

  private final String                 index;

  private final String                 searchType;

  private String                       searchQueryFilePath;

  private String                       searchQuery;

  public NewsESSearchConnector(ConfigurationManager configurationManager,
                               IdentityManager identityManager,
                               ActivityStorage activityStorage,
                               ElasticSearchingClient client,
                               InitParams initParams) {
    this.configurationManager = configurationManager;
    this.identityManager = identityManager;
    this.activityStorage = activityStorage;
    this.client = client;

    PropertiesParam param = initParams.getPropertiesParam("constructor.params");
    this.index = param.getProperty("index");
    this.searchType = param.getProperty("searchType");
    if (initParams.containsKey(SEARCH_QUERY_FILE_PATH_PARAM)) {
      searchQueryFilePath = initParams.getValueParam(SEARCH_QUERY_FILE_PATH_PARAM).getValue();
      try {
        retrieveSearchQuery();
      } catch (Exception e) {
        LOG.error("Can't read elasticsearch search query from path {}", searchQueryFilePath, e);
      }
    }
  }

  public List<NewsESSearchResult> search(Identity viewerIdentity, NewsFilter filter) {
    if (viewerIdentity == null) {
      throw new IllegalArgumentException("Viewer identity is mandatory");
    }
    if (filter.getOffset() < 0) {
      throw new IllegalArgumentException("Offset must be positive");
    }
    if (filter.getLimit() < 0) {
      throw new IllegalArgumentException("Limit must be positive");
    }
    if (StringUtils.isBlank(filter.getSearchText()) && !filter.isFavorites()) {
      throw new IllegalArgumentException("Filter term is mandatory");
    }
    Set<Long> streamFeedOwnerIds = activityStorage.getStreamFeedOwnerIds(viewerIdentity);
    String esQuery = buildQueryStatement(viewerIdentity, streamFeedOwnerIds, filter);
    String jsonResponse = this.client.sendRequest(esQuery, this.index);
    return buildResult(jsonResponse);
  }

  private String buildQueryStatement(Identity viewerIdentity, Set<Long> streamFeedOwnerIds, NewsFilter filter) {
    String term = removeSpecialCharacters(filter.getSearchText());
    term = StringUtils.isBlank(term) ? "*:*" : term;
    List<String> termsQuery = Arrays.stream(term.split(" ")).filter(StringUtils::isNotBlank).map(word -> {
      word = word.trim();
      if (word.length() > 5) {
        word = word + "~1";
      }
      return word;
    }).collect(Collectors.toList());
    Map<String, List<String>> metadataFilters = buildMetadatasFilter(filter, viewerIdentity);
    String metadataQuery = buildMetadatasQueryStatement(metadataFilters);
    String termQuery = termsQuery.isEmpty() ? "*:*" : StringUtils.join(termsQuery, " AND ");
    return retrieveSearchQuery().replace("@term@", term)
                                .replace("@term_query@", termQuery)
                                .replace("@metadatas_query@", metadataQuery)
                                .replace("@permissions@", StringUtils.join(streamFeedOwnerIds, ","))
                                .replace("@offset@", String.valueOf(filter.getOffset()))
                                .replace("@limit@", String.valueOf(filter.getLimit()));
  }

  @SuppressWarnings("rawtypes")
  private List<NewsESSearchResult> buildResult(String jsonResponse) {
    LOG.debug("Search Query response from ES : {} ", jsonResponse);

    List<NewsESSearchResult> results = new ArrayList<>();
    JSONParser parser = new JSONParser();

    Map json;
    try {
      json = (Map) parser.parse(jsonResponse);
    } catch (ParseException e) {
      throw new ElasticSearchException("Unable to parse JSON response", e);
    }

    JSONObject jsonResult = (JSONObject) json.get("hits");
    if (jsonResult == null) {
      return results;
    }

    //
    JSONArray jsonHits = (JSONArray) jsonResult.get("hits");
    for (Object jsonHit : jsonHits) {
      try {
        NewsESSearchResult newsSearchResult = new NewsESSearchResult();
        JSONObject jsonHitObject = (JSONObject) jsonHit;
        JSONObject hitSource = (JSONObject) jsonHitObject.get("_source");
        String id = (String) hitSource.get("id");
        String posterId = (String) hitSource.get("posterId");
        String spaceDisplayName = (String) hitSource.get("spaceDisplayName");
        String newsActivityId = (String) hitSource.get("newsActivityId");

        Long postedTime = parseLong(hitSource, "postedTime");
        Long lastUpdatedTime = parseLong(hitSource, "lastUpdatedTime");

        String title = (String) hitSource.get("title");
        String body = (String) hitSource.get("body");
        JSONObject highlightSource = (JSONObject) jsonHitObject.get("highlight");
        List<String> excerpts = new ArrayList<>();
        if (highlightSource != null) {
          JSONArray bodyExcepts = (JSONArray) highlightSource.get("body");
          if (bodyExcepts != null) {
            excerpts = Arrays.asList((String[]) bodyExcepts.toArray(new String[0]));
          }
        }
        newsSearchResult.setId(id);
        newsSearchResult.setTitle(title);
        if (posterId != null) {
          Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, posterId);
          newsSearchResult.setPoster(posterIdentity);
        }
        newsSearchResult.setPostedTime(postedTime);
        newsSearchResult.setLastUpdatedTime(lastUpdatedTime);
        newsSearchResult.setSpaceDisplayName(spaceDisplayName);

        String portalName = PortalContainer.getCurrentPortalContainerName();
        String portalOwner = CommonsUtils.getCurrentPortalOwner();
        StringBuilder newsUrl = new StringBuilder("");
        newsUrl.append("/").append(portalName).append("/").append(portalOwner).append("/activity?id=").append(newsActivityId);
        newsSearchResult.setNewsUrl(newsUrl.toString());

        newsSearchResult.setBody(body);
        newsSearchResult.setExcerpts(excerpts);

        results.add(newsSearchResult);
      } catch (Exception e) {
        LOG.warn("Error processing news search result item, ignore it from results", e);
      }
    }
    return results;
  }

  private Long parseLong(JSONObject hitSource, String key) {
    String value = (String) hitSource.get(key);
    return StringUtils.isBlank(value) ? null : Long.parseLong(value);
  }

  private String retrieveSearchQuery() {
    if (StringUtils.isBlank(this.searchQuery) || PropertyManager.isDevelopping()) {
      try {
        InputStream queryFileIS = this.configurationManager.getInputStream(searchQueryFilePath);
        this.searchQuery = IOUtil.getStreamContentAsString(queryFileIS);
      } catch (Exception e) {
        throw new IllegalStateException("Error retrieving search query from file: " + searchQueryFilePath, e);
      }
    }
    return this.searchQuery;
  }

  private String removeSpecialCharacters(String string) {
    string = Normalizer.normalize(string, Normalizer.Form.NFD);
    string = string.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "").replaceAll("'", " ");
    return string;
  }

  private String buildMetadatasQueryStatement(Map<String, List<String>> metadataFilters) {
    StringBuilder metadataQuerySB = new StringBuilder();
    Set<Map.Entry<String, List<String>>> metadataFilterEntries = metadataFilters.entrySet();
    for (Map.Entry<String, List<String>> metadataFilterEntry : metadataFilterEntries) {
      metadataQuerySB.append("{\"terms\":{\"metadatas.")
      .append(metadataFilterEntry.getKey())
      .append(".metadataName.keyword")
      .append("\": [\"")
      .append(StringUtils.join(metadataFilterEntry.getValue(), "\",\""))
      .append("\"]}},");
    }
    return metadataQuerySB.toString();
  }

  private Map<String, List<String>> buildMetadatasFilter(NewsFilter filter, Identity viewerIdentity) {
    Map<String, List<String>> metadataFilters = new HashMap<>();
    if (filter.isFavorites()) {
      metadataFilters.put(FavoriteService.METADATA_TYPE.getName(), Collections.singletonList(viewerIdentity.getId()));
    }
    return metadataFilters;
  }
}
