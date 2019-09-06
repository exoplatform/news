package org.exoplatform.news;

import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.social.core.space.model.Space;

import javax.jcr.RepositoryException;
import java.util.List;

public interface NewsService {
  News createNews(News news) throws Exception;

  News getNews(String id) throws Exception;

  void updateNews(News news) throws Exception;

  void shareNews(SharedNews sharedNews, List<Space> spaces) throws Exception;
  
  void pinNews(String newsId) throws Exception;
}
