package org.exoplatform.news.storage;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

public interface NewsStorage {
  
  News createNews(News news) throws Exception;
  
  void publishNews(News news) throws Exception;
  
  String getNewsIllustration(News news) throws Exception;
  
  News updateNews(News news, String updater) throws Exception;
  
  void updateNewsActivities(String newsActivityId, News news) throws Exception;
  
  News getNewsById(String newsId, boolean editMode) throws Exception;
  
  List<News> getNews(NewsFilter newsFilter) throws Exception;
  
  int getNewsCount(NewsFilter newsFilter) throws Exception;
  
  void markAsRead(News news, String userId) throws Exception;
  
  boolean isCurrentUserInNewsViewers(String newsId, String userId) throws Exception;
  
  void unpublishNews(String newsId) throws Exception;
  
  void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws IllegalAccessException, ObjectNotFoundException;
  
  void deleteNews(String newsId, boolean isDraft) throws Exception;
  
  News scheduleNews(News news) throws Exception;
  
  News unScheduleNews(News news) throws Exception;
  
  void archiveNews(String newsId) throws Exception;
  
  void unarchiveNews(String newsId) throws Exception;
  
  List<News> searchNews(NewsFilter filter, String lang) throws Exception;

}