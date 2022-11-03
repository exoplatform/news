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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.search.DocumentWithMetadata;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataObject;

public class NewsIndexingServiceConnector extends ElasticIndexingServiceConnector {

  public static final String    TYPE             = "news";

  private static final Log      LOG              = ExoLogger.getLogger(NewsIndexingServiceConnector.class);

  private final NewsService     newsService;

  private final IdentityManager identityManager;

  private final ActivityManager activityManager;

  private final MetadataService metadataService;

  public NewsIndexingServiceConnector(IdentityManager identityManager,
                                      InitParams initParams,
                                      NewsService newsService,
                                      ActivityManager activityManager,
                                      MetadataService metadataService) {
    super(initParams);
    this.newsService = newsService;
    this.identityManager = identityManager;
    this.activityManager = activityManager;
    this.metadataService = metadataService;
  }

  @Override
  public String getConnectorName() {
    return TYPE;
  }

  @Override
  public Document create(String id) {
    return getDocument(id);
  }

  @Override
  public Document update(String id) {
    return getDocument(id);
  }

  @Override
  public List<String> getAllIds(int offset, int limit) {
    throw new UnsupportedOperationException();
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    LOG.debug("Index document for news id={}", id);
    News news = null;
    //TODO to be tested and removed
    SessionProvider systemProvider = SessionProvider.createSystemProvider();
    SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
    sessionProviderService.setSessionProvider(null, systemProvider);
    try {
      news = newsService.getNewsById(id, false);
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
    }
    if (news == null) {
      throw new IllegalStateException("news with id '" + id + "' is mandatory");
    }
    Map<String, String> fields = new HashMap<>();
    fields.put("id", news.getId());

    fields.put("title", news.getTitle());

    String body = news.getBody();
    String summary = news.getSummary();
    if (StringUtils.isBlank(body)) {
      body = news.getTitle();
    }
    // Ensure to index text only without html tags
    if (StringUtils.isNotBlank(body)) {
      body = StringEscapeUtils.unescapeHtml(body);
      try {
        body = HTMLSanitizer.sanitize(body);
      } catch (Exception e) {
        LOG.warn("Error sanitizing news '{}' body", news.getId());
      }
      body = htmlToText(body);
      fields.put("body", body);
    }

    if (StringUtils.isNotBlank(summary)) {
      summary = StringEscapeUtils.unescapeHtml(summary);
      try {
        summary = HTMLSanitizer.sanitize(summary);
      } catch (Exception e) {
        LOG.warn("Error sanitizing news '{}' summary", news.getId());
      }
      summary = htmlToText(summary);
      fields.put("summary", summary);
    }

    if (StringUtils.isNotBlank(news.getAuthor())) {
      fields.put("posterId", news.getAuthor());
      Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor());
      if (posterIdentity != null && posterIdentity.getProfile() != null
          && StringUtils.isNotBlank(posterIdentity.getProfile().getFullName())) {
        fields.put("posterName", posterIdentity.getProfile().getFullName());
      }
    }
    if (news.getSpaceDisplayName() != null) {
      fields.put("spaceDisplayName", news.getSpaceDisplayName());
    }

    String newsActivities = news.getActivities();
    String ownerIdentityId = null;

    if (newsActivities != null) {
      String newsActivityId = newsActivities.split(";")[0].split(":")[1];
      fields.put("newsActivityId", newsActivityId);
      ExoSocialActivity newsActivity = activityManager.getActivity(newsActivityId);
      ActivityStream activityStream = newsActivity.getActivityStream();

      if (newsActivity.getParentId() != null
          && (activityStream == null || activityStream.getType() == null || StringUtils.isBlank(activityStream.getPrettyId()))) {
        ExoSocialActivity parentActivity = activityManager.getActivity(newsActivity.getParentId());
        activityStream = parentActivity.getActivityStream();
      }

      if (activityStream != null && activityStream.getType() != null && StringUtils.isNotBlank(activityStream.getPrettyId())) {
        String prettyId = activityStream.getPrettyId();
        String providerId = activityStream.getType().getProviderId();
        Identity streamOwner = identityManager.getOrCreateIdentity(providerId, prettyId);
        ownerIdentityId = streamOwner.getId();
      }
    } else {
      return null;
    }
    if (news.getCreationDate() != null) {
      fields.put("postedTime", String.valueOf(news.getCreationDate().getTime()));
    }
    if (news.getUpdateDate() != null) {
      fields.put("lastUpdatedTime", String.valueOf(news.getUpdateDate().getTime()));
    }
    DocumentWithMetadata document = new DocumentWithMetadata();
    document.setId(id);
    document.setLastUpdatedDate(news.getUpdateDate());
    document.setPermissions(Collections.singleton(ownerIdentityId));
    document.setFields(fields);
    addDocumentMetadata(document, news.getId());

    return document;
  }

  private String htmlToText(String source) {
    source = source.replaceAll("<( )*head([^>])*>", "<head>");
    source = source.replaceAll("(<( )*(/)( )*head( )*>)", "</head>");
    source = source.replaceAll("(<head>).*(</head>)", "");
    source = source.replaceAll("<( )*script([^>])*>", "<script>");
    source = source.replaceAll("(<( )*(/)( )*script( )*>)", "</script>");
    source = source.replaceAll("(<script>).*(</script>)", "");
    source = source.replace("javascript:", "");
    source = source.replaceAll("<( )*style([^>])*>", "<style>");
    source = source.replaceAll("(<( )*(/)( )*style( )*>)", "</style>");
    source = source.replaceAll("(<style>).*(</style>)", "");
    source = source.replaceAll("<( )*td([^>])*>", "\t");
    source = source.replaceAll("<( )*br( )*(/)*>", "\n");
    source = source.replaceAll("<( )*li( )*>", "\n");
    source = source.replaceAll("<( )*div([^>])*>", "\n");
    source = source.replaceAll("<( )*tr([^>])*>", "\n");
    source = source.replaceAll("<( )*p([^>])*>", "\n");
    source = source.replaceAll("<[^>]*>", "");
    return source;
  }

  private void addDocumentMetadata(DocumentWithMetadata document, String documentId) {
    MetadataObject metadataObject = new MetadataObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, documentId);
    List<MetadataItem> metadataItems = metadataService.getMetadataItemsByObject(metadataObject);
    document.setMetadataItems(metadataItems);
  }

}