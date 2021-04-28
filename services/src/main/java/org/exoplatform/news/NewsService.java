package org.exoplatform.news;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.social.core.space.model.Space;

public interface NewsService {
  News createNews(News news) throws Exception;

  News getNewsById(String id) throws Exception;

  List<News> getNews(NewsFilter filter) throws Exception;

  int getNewsCount(NewsFilter filter) throws Exception;

  News updateNews(News news) throws Exception;

  void markAsRead(News news, String userId) throws Exception;

  void shareNews(SharedNews sharedNews, List<Space> spaces) throws Exception;

  void pinNews(String newsId) throws Exception;

  void unpinNews(String newsId) throws Exception;

  News convertNodeToNews(Node node) throws Exception;

  News createNewsDraft(News news) throws Exception;

  List<News> getNewsDrafts(String spaceId, String author) throws Exception;

  void deleteNews(String id) throws Exception;

  public boolean canEditNews(String posterId, String spaceId);

  public boolean canPinNews();
  
  boolean canArchiveNews(String newsAuthor);

  List<News> searchNews(NewsFilter filter, String lang) throws Exception;

  void archiveNews(String newsId) throws Exception;

  void unarchiveNews(String newsId) throws Exception;

  public boolean canDeleteNews(String posterId, String spaceId);

}
