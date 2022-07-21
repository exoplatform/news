/*
 * Copyright (C) 2003-2022 eXo Platform SAS.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.listener;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsIndexingServiceConnector;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.metadata.tag.model.TagObject;

public class NewsMetadataListener extends Listener<String, News> {

  private final IndexingService indexingService;

  private final IdentityManager identityManager;

  private final SpaceService    spaceService;

  private TagService            tagService;

  public NewsMetadataListener(IndexingService indexingService,
                              SpaceService spaceService,
                              IdentityManager identityManager,
                              TagService tagService) {
    this.indexingService = indexingService;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.tagService = tagService;
  }

  @Override
  public void onEvent(Event<String, News> event) throws Exception {
    News news = event.getData();
    String username = event.getSource();

    saveTags(news, username);

    indexingService.reindex(NewsIndexingServiceConnector.TYPE, news.getId());
  }

  private void saveTags(News news, String username) {
    long creatorId = getPosterId(username);
    long audienceId = getStreamOwnerId(news.getSpaceId(), username);

    Set<TagName> tagNames = tagService.detectTagNames(news.getBody());
    tagService.saveTags(new TagObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE,
                                      news.getId(),
                                      null),
                        tagNames,
                        audienceId,
                        creatorId);
  }

  private long getStreamOwnerId(String spaceId, String username) {
    Space space = spaceService.getSpaceById(spaceId);
    return space == null ? getPosterId(username)
                         : Long.parseLong(identityManager.getOrCreateSpaceIdentity(space.getPrettyName()).getId());
  }

  private long getPosterId(String username) {
    return StringUtils.isBlank(username) ? 0 : Long.parseLong(identityManager.getOrCreateUserIdentity(username).getId());
  }

}
