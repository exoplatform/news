/*
 * Copyright (C) 2024 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class NewsServiceImplV2 implements NewsService {

  public SpaceService spaceService;

  public NewsServiceImplV2(SpaceService spaceService) {
    this.spaceService = spaceService;
  }

  @Override
  public News createNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  @Override
  public News postNews(News news, String poster) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean canCreateNews(Space space, org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    return space != null
        && (spaceService.isSuperManager(currentIdentity.getUserId()) || spaceService.isManager(space, currentIdentity.getUserId())
            || spaceService.isRedactor(space, currentIdentity.getUserId())
            || spaceService.isMember(space, currentIdentity.getUserId()) && ArrayUtils.isEmpty(space.getRedactors()));
  }

  @Override
  public News updateNews(News news, String updater, Boolean post, boolean publish) throws Exception {
    return null;
  }

  @Override
  public void deleteNews(String id, Identity currentIdentity, boolean isDraft) throws Exception {

  }

  @Override
  public void publishNews(News news, String publisher) throws Exception {

  }

  @Override
  public void unpublishNews(String newsId, String publisher) throws Exception {

  }

  @Override
  public News getNewsById(String newsId, Identity currentIdentity, boolean editMode) throws IllegalAccessException {
    return null;
  }

  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    return null;
  }

  @Override
  public List<News> getNews(NewsFilter filter, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public List<News> getNewsByTargetName(NewsFilter filter, String targetName, Identity currentIdentity) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public int getNewsCount(NewsFilter filter) throws Exception {
    return 0;
  }

  @Override
  public void markAsRead(News news, String userId) throws Exception {

  }

  @Override
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public News getNewsByActivityId(String activityId, Identity currentIdentity) throws IllegalAccessException,
                                                                               ObjectNotFoundException {
    return null;
  }

  @Override
  public News scheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  @Override
  public News unScheduleNews(News news, Identity currentIdentity) throws Exception {
    return null;
  }

  @Override
  public List<NewsESSearchResult> search(org.exoplatform.social.core.identity.model.Identity currentIdentity, NewsFilter filter) {
    return new ArrayList<>();
  }

  @Override
  public boolean canScheduleNews(Space space, Identity currentIdentity) {
    return false;
  }

  @Override
  public boolean canViewNews(News news, String authenticatedUser) {
    return false;
  }

  @Override
  public void shareNews(News news,
                        Space space,
                        org.exoplatform.social.core.identity.model.Identity userIdentity,
                        String sharedActivityId) throws Exception {

  }

  @Override
  public void archiveNews(String newsId, String currentUserName) throws Exception {

  }

  @Override
  public void unarchiveNews(String newsId, String currentUserName) throws Exception {

  }

  @Override
  public boolean canArchiveNews(Identity currentIdentity, String newsAuthor) {
    return false;
  }
}
