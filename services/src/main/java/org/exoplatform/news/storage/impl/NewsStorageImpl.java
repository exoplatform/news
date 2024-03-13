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
package org.exoplatform.news.storage.impl;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.storage.NewsStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

public class NewsStorageImpl implements NewsStorage {
  @Override
  public News createNews(News news) throws Exception {
    return null;
  }

  @Override
  public void publishNews(News news) throws Exception {

  }

  @Override
  public String getNewsIllustration(News news) throws Exception {
    return null;
  }

  @Override
  public News updateNews(News news, String updater) throws Exception {
    return null;
  }

  @Override
  public void updateNewsActivities(String newsActivityId, News news) throws Exception {

  }

  @Override
  public News getNewsById(String newsId, boolean editMode) throws Exception {
    return null;
  }

  @Override
  public List<News> getNews(NewsFilter newsFilter) throws Exception {
    return new ArrayList<>();
  }

  @Override
  public int getNewsCount(NewsFilter newsFilter) throws Exception {
    return 0;
  }

  @Override
  public void markAsRead(News news, String userId) throws Exception {

  }

  @Override
  public boolean isCurrentUserInNewsViewers(String newsId, String userId) throws Exception {
    return false;
  }

  @Override
  public void unpublishNews(String newsId) throws Exception {

  }

  @Override
  public void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws IllegalAccessException,
                                                                                                ObjectNotFoundException {

  }

  @Override
  public void deleteNews(String newsId, boolean isDraft) throws Exception {

  }

  @Override
  public News scheduleNews(News news) throws Exception {
    return null;
  }

  @Override
  public News unScheduleNews(News news) throws Exception {
    return null;
  }

  @Override
  public void archiveNews(String newsId) throws Exception {

  }

  @Override
  public void unarchiveNews(String newsId) throws Exception {

  }

  @Override
  public List<News> searchNews(NewsFilter filter, String lang) throws Exception {
    return new ArrayList<>();
  }
}
