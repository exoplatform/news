package org.exoplatform.news.service;

import java.util.List;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

public interface NewsService {
  
  /**
   * Create and publish a News A news is composed of an activity and a CMS node
   * containing the data. If the given News has an id and that a draft already
   * exists with this id, the draft is updated and published.
   * 
   * @param news The news to create
   * @throws Exception when error
   */
  News createNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  boolean canCreateNews(Space space, org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  /**
   * Update a news If the uploadId of the news is null, the illustration is not
   * updated. If the uploadId of the news is empty, the illustration is removed
   * (if any).
   * 
   * @param news The new news
   * @throws Exception when error
   */
  News updateNews(News news) throws Exception;
  
  void updateNewsActivity(News news, boolean post);
  
  /**
   * @param news {@link News} to check
   * @param authenticatedUser authenticated username
   * @return true if user has access to news, else false
   */
  boolean canEditNews(News news, String authenticatedUser);
  
  /**
   * Delete news
   * 
   * @param newsId the news id to delete
   * @param currentUser
   * @param isDraft
   * @throws Exception when error
   */
  void deleteNews(String id, String currentUser, boolean isDraft) throws Exception;
  
  boolean canDeleteNews(String posterId, String spaceId);
  
  /**
   * Retrives a news identified by its technical identifier
   * 
   * @param newsId {@link News} identifier
   * @param editMode
   * @return {@link News} if found else null
   */
  News getNewsById(String newsId, boolean editMode);
  
  /**
   * Get all news
   * 
   * @return all news
   * @throws Exception when error
   */
  List<News> getNews(NewsFilter filter) throws Exception;
  
  int getNewsCount(NewsFilter filter) throws Exception;
  
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
   * @param news {@link News} to check
   * @param authenticatedUser authenticated username
   * @return true if user has access to news, else false
   */
  boolean canViewNews(News news, String authenticatedUser);  
  
  /**
   * Increment the number of views for a news
   * 
   * @param userId The current user id
   * @param news The news to be updated
   * @throws Exception when error
   */
  void markAsRead(News news, String userId) throws Exception;
  
  /**
   * Publish a news
   *
   * @param newsId The id of the news to be published
   * @throws Exception when error
   */
  void publishNews(String newsId) throws Exception;
  
  void unpublishNews(String newsId) throws Exception;
  
  /**
   * Return a boolean that indicates if the current user can publish the news or not
   * 
   * @return if the news can be published
   */
  public boolean canPublishNews();
  
  /**
   * Shares a news into a dedicated space
   * 
   * @param news {@link News} to share
   * @param space {@link Space} to share with, the news
   * @param userIdentity {@link Identity} of user making the modification
   * @param sharedActivityId newly generated activity identifier
   * @throws IllegalAccessException when user doesn't have access to {@link News}
   * @throws ObjectNotFoundException when {@link News} is not found
   */
  void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws Exception;
  
  /**
   * Archive a news
   *
   * @param newsId The id of the news to be archived
   * @throws Exception when error
   */
  void archiveNews(String newsId) throws Exception;
  
  void unarchiveNews(String newsId) throws Exception;
  
  boolean canArchiveNews(String newsAuthor);
  
  News scheduleNews(News news) throws Exception;
  
  News unScheduleNews(News news) throws Exception;
  
  public List<NewsESSearchResult> search(Identity currentUser, String term, int offset, int limit);
  
  List<News> searchNews(NewsFilter filter, String lang) throws Exception;

}