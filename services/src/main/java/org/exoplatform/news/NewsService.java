package org.exoplatform.news;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.social.core.space.model.Space;
import java.util.List;

public interface NewsService {
  News createNews(News news) throws Exception;

  News getNewsById(String id) throws Exception;

  List<News> getNews() throws Exception;

  void updateNews(News news) throws Exception;

  void shareNews(SharedNews sharedNews, List<Space> spaces) throws Exception;
  
  void pinNews(String newsId) throws Exception;

  void unpinNews(String newsId) throws Exception;

  News createNewsDraft(News news) throws Exception;

  List<News> getNewsDrafts(String spaceId, String author) throws Exception;

  void deleteNews(String id) throws Exception;
}
