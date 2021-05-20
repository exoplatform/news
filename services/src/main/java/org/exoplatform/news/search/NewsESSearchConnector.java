/*
 * Copyright (C) 2020 eXo Platform SAS.
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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.ActivityStorage;
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
import org.exoplatform.social.core.space.spi.SpaceService;

public class NewsESSearchConnector {
  private static final Log             LOG                          = ExoLogger.getLogger(NewsESSearchConnector.class);

  private static final String          SEARCH_QUERY_FILE_PATH_PARAM = "query.file.path";

  private final ConfigurationManager   configurationManager;

  private final IdentityManager        identityManager;

  private final SpaceService           spaceService;

  private final ActivityStorage        activityStorage;

  private final ElasticSearchingClient client;

  private final String                 index;

  private final String                 searchType;

  private String                       searchQueryFilePath;

  private String                       searchQuery;

  public NewsESSearchConnector(ConfigurationManager configurationManager,
                               IdentityManager identityManager,
                               SpaceService spaceService,
                               ActivityStorage activityStorage,
                               ElasticSearchingClient client,
                               InitParams initParams) {
    this.configurationManager = configurationManager;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
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

  public List<NewsESSearchResult> search(Identity viewerIdentity, String term, long offset, long limit) {
    if (viewerIdentity == null) {
      throw new IllegalArgumentException("Viewer identity is mandatory");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("Offset must be positive");
    }
    if (limit < 0) {
      throw new IllegalArgumentException("Limit must be positive");
    }
    if (StringUtils.isBlank(term)) {
      throw new IllegalArgumentException("Filter term is mandatory");
    }
    Set<Long> streamFeedOwnerIds = activityStorage.getStreamFeedOwnerIds(viewerIdentity);
    String esQuery = buildQueryStatement(streamFeedOwnerIds, term, offset, limit);
    String jsonResponse = this.client.sendRequest(esQuery, this.index, this.searchType);
    return buildResult(jsonResponse);
  }

  private String buildQueryStatement(Set<Long> streamFeedOwnerIds, String term, long offset, long limit) {
    term = removeSpecialCharacters(term);
    List<String> termsQuery = Arrays.stream(term.split(" ")).filter(StringUtils::isNotBlank).map(word -> {
      word = word.trim();
      if (word.length() > 5) {
        word = word + "~1";
      }
      return word;
    }).collect(Collectors.toList());
    String termQuery = StringUtils.join(termsQuery, " AND ");
    return retrieveSearchQuery().replaceAll("@term@", term)
                                .replaceAll("@term_query@", termQuery)
                                .replaceAll("@permissions@", StringUtils.join(streamFeedOwnerIds, ","))
                                .replaceAll("@offset@", String.valueOf(offset))
                                .replaceAll("@limit@", String.valueOf(limit));
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
            String[] bodyExceptsArray = (String[]) bodyExcepts.toArray(new String[0]);
            excerpts = Arrays.asList(bodyExceptsArray);
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
        LOG.warn("Error processing activity search result item, ignore it from results", e);
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
}
