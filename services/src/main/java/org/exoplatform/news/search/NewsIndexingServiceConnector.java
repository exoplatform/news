/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.news.search;

import java.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.search.domain.Document;
import org.exoplatform.commons.search.index.impl.ElasticIndexingServiceConnector;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class NewsIndexingServiceConnector extends ElasticIndexingServiceConnector {
  private static final long     serialVersionUID = 1L;

  public static final String    TYPE             = "news";

  private static final Log      LOG              = ExoLogger.getLogger(NewsIndexingServiceConnector.class);

  private final NewsService     newsService;

  private final IdentityManager identityManager;

  public NewsIndexingServiceConnector(IdentityManager identityManager, InitParams initParams, NewsService newsService) {
    super(initParams);
    this.newsService = newsService;
    this.identityManager = identityManager;
  }

  @Override
  public String getType() {
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

  @Override
  public boolean canReindex() {
    return false;
  }

  private Document getDocument(String id) {
    if (StringUtils.isBlank(id)) {
      throw new IllegalArgumentException("id is mandatory");
    }
    LOG.debug("Index document for news id={}", id);
    Document document = new Document();
    News news = null;
    String spaceId = null;
    SessionProvider systemProvider = SessionProvider.createSystemProvider();
    SessionProviderService sessionProviderService = CommonsUtils.getService(SessionProviderService.class);
    sessionProviderService.setSessionProvider(null, systemProvider);
    try {
      news = newsService.getNewsById(id);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      sessionProviderService.removeSessionProvider(null);
    }
    if (news == null) {
      throw new IllegalStateException("news with id '" + id + "' is mandatory");
    }
    Map<String, String> fields = new HashMap<>();
    fields.put("id", news.getId().replace("comment", ""));

    fields.put("title", news.getTitle());

    String body = news.getBody();
    if (StringUtils.isBlank(body)) {
      body = news.getTitle();
    }
    fields.put("body", body);

    if (StringUtils.isNotBlank(news.getAuthor())) {
      fields.put("posterId", news.getAuthor());
      Identity posterIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, news.getAuthor());
      if (posterIdentity != null && posterIdentity.getProfile() != null
          && StringUtils.isNotBlank(posterIdentity.getProfile().getFullName())) {
        fields.put("posterName", posterIdentity.getProfile().getFullName());
      }
    }
    spaceId = news.getSpaceId();
    if (spaceId != null) {
      fields.put("spaceDisplayName", String.valueOf(news.getSpaceDisplayName()));
    }

    String newsActivityId = news.getActivities();
    if (newsActivityId != null) {
      fields.put("newsActivityId", newsActivityId.split(":")[1].substring(0, 1));
    } else {
      return null;
    }
    if (news.getCreationDate() != null) {
      fields.put("postedTime", String.valueOf(news.getCreationDate().getTime()));
    }
    if (news.getUpdateDate() != null) {
      fields.put("lastUpdatedTime", String.valueOf(news.getUpdateDate().getTime()));
    }

    document = new Document(TYPE, id, null, news.getUpdateDate(), Collections.singleton(spaceId), fields);
    body = document.getFields().get("body");

    // Ensure to index text only without html tags
    if (StringUtils.isNotBlank(body)) {
      body = StringEscapeUtils.unescapeHtml(body);
      try {
        body = HTMLSanitizer.sanitize(body);
      } catch (Exception e) {
        LOG.warn("Error sanitizing news '{}' body", news.getId());
      }
      body = htmlToText(body);
      document.addField("body", body);
    }
    return document;
  }

  private String htmlToText(String source) {
    source = source.replaceAll("<( )*head([^>])*>", "<head>");
    source = source.replaceAll("(<( )*(/)( )*head( )*>)", "</head>");
    source = source.replaceAll("(<head>).*(</head>)", "");
    source = source.replaceAll("<( )*script([^>])*>", "<script>");
    source = source.replaceAll("(<( )*(/)( )*script( )*>)", "</script>");
    source = source.replaceAll("(<script>).*(</script>)", "");
    source = source.replaceAll("javascript:", "");
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

}
