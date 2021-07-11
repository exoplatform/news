package org.exoplatform.news;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

public interface NewsService {
  News createNews(News news) throws Exception;

  /**
   * Retrives a news identified by its technical identifier
   * 
   * @param newsId {@link News} identifier
   * @param editMode
   * @return {@link News} if found else null
   */
  News getNewsById(String newsId, boolean editMode);

  /**
   * Retrives a news identified by its technical identifier
   * 
   * @param newsId {@link News} identifier
   * @param authenticatedUser user attempting to access news
   * @param editMode access mode to news: whether to edit news to to view it.
   * @return {@link News} if found else null
   * @throws IllegalAccessException when user doesn't have access to {@link News}
   */
  News getNewsById(String newsId, String authenticatedUser, boolean editMode) throws IllegalAccessException;

  /**
   * Retrives a news identified by originating Activity identifier or a shared
   * activity identifier
   * 
   * @param activityId {@link ExoSocialActivity} identifier
   * @param authenticatedUser user attempting to access news
   * @return {@link News} if found else null
   * @throws IllegalAccessException when user doesn't have access to
   *           {@link News} or {@link ExoSocialActivity}
   * @throws ObjectNotFoundException when a {@link News} wasn't found for this
   *           activity identifier
   */
  News getNewsByActivityId(String activityId, String authenticatedUser) throws IllegalAccessException, ObjectNotFoundException;

  List<News> getNews(NewsFilter filter) throws Exception;

  int getNewsCount(NewsFilter filter) throws Exception;

  News updateNews(News news) throws Exception;

  void markAsRead(News news, String userId) throws Exception;

  /**
   * Shares a news into a dedicated space
   * 
   * @param news {@link News} to share
   * @param space {@link Space} to share with, the news
   * @param userIdentity {@link Identity} of user making the modification
   * @throws IllegalAccessException when user doesn't have access to {@link News}
   * @throws ObjectNotFoundException when {@link News} is not found
   */
  void shareNews(News news, Space space, Identity userIdentity) throws IllegalAccessException, ObjectNotFoundException;

  void pinNews(String newsId) throws Exception;

  void unpinNews(String newsId) throws Exception;

  News convertNodeToNews(Node node, boolean editMode) throws Exception;

  News createNewsDraft(News news) throws Exception;

  void deleteNews(String id, boolean isDraft) throws Exception;

  public boolean canEditNews(String posterId, String spaceId);

  /**
   * @param news {@link News} to check
   * @param authenticatedUser authenticated username
   * @return true if user has access to news, else false
   */
  boolean canViewNews(News news, String authenticatedUser);

  /**
   * @param news {@link News} to check
   * @param authenticatedUser authenticated username
   * @return true if user has access to news, else false
   */
  boolean canEditNews(News news, String authenticatedUser);

  public boolean canPinNews();
  
  boolean canArchiveNews(String newsAuthor);

  List<News> searchNews(NewsFilter filter, String lang) throws Exception;

  void archiveNews(String newsId) throws Exception;

  void unarchiveNews(String newsId) throws Exception;

  public boolean canDeleteNews(String posterId, String spaceId);

  public List<NewsESSearchResult> search(Identity currentUser, String term, int offset, int limit);

  News scheduleNews(News news) throws Exception;

}
