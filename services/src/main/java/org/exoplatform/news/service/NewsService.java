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
   * @param currentIdentity
   * @param news The news to create
   * @return created News object
   * @throws Exception when error
   */
  News createNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  /**
   * Create and publish a News A news is composed of an activity and a CMS node
   * containing the data. If the given News has an id and that a draft already
   * exists with this id, the draft is updated and published.
   * 
   * @param news The news to post
   * @param poster the Poster of the News
   * @return The posted News
   * @throws Exception when error
   */
  News postNews(News news, String poster) throws Exception;
  
  /**
   * Checks if the user can create a News
   *
   * @param space
   * @param currentIdentity
   * @return boolean true if the user can create the news
   * @throws Exception if an error occurred
   */
  boolean canCreateNews(Space space, org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  /**
   * Update a news If the uploadId of the news is null, the illustration is not
   * updated. If the uploadId of the news is empty, the illustration is removed
   * (if any).
   * 
   * @param news
   * @param updater user attempting to update news
   * @param post
   * @param publish
   * @return updated News
   * @throws Exception
   */
  News updateNews(News news, String updater, Boolean post, boolean publish) throws Exception;

  /**
   * Update a news If the uploadId of the news is null, the illustration is not
   * updated. If the uploadId of the news is empty, the illustration is removed
   * (if any).
   *
   * @param news
   * @param updater user attempting to update news
   * @param post
   * @param publish
   * @param newsObjectType
   * @return updated News
   * @throws Exception
   */
  News updateNews(News news, String updater, Boolean post, boolean publish, String newsObjectType) throws Exception;
  
  /**
   * Delete news
   * 
   * @param id the news id to delete
   * @param currentIdentity user attempting to delete news
   * @param isDraft if the News is still draft
   * @throws Exception when error
   */
  void deleteNews(String id, org.exoplatform.services.security.Identity currentIdentity, boolean isDraft) throws Exception;
  
  /**
   * Publish a news
   *
   * @param news to be published
   * @param publisher of the News
   * @throws Exception when error
   */
  void publishNews(News news, String publisher) throws Exception;
  
  /**
   * Unpublish a News
   * @param newsId the ID of the News
   * @param publisher the publisher of the News
   * @throws Exception when an error occurs
   */
  void unpublishNews(String newsId, String publisher) throws Exception;
  
  /**
   * Retrives a news identified by its technical identifier
   * 
   * @param newsId {@link News} identifier
   * @param editMode access mode to news: whether to edit news to to view it.
   * @return {@link News} if found else null
   * @throws Exception when user doesn't have access to {@link News}
   */
  News getNewsById(String newsId, boolean editMode) throws Exception;
  
  /**
   * Retrives a news identified by its technical identifier
   * 
   * @param newsId {@link News} identifier
   * @param currentIdentity user attempting to access news
   * @param editMode access mode to news: whether to edit news to to view it.
   * @return {@link News} if found else null
   * @throws IllegalAccessException when user doesn't have access to {@link News}
   */
  News getNewsById(String newsId, org.exoplatform.services.security.Identity currentIdentity, boolean editMode) throws IllegalAccessException;
  
  /**
   * Retrieves a news identified by its technical identifier
   *
   * @param newsId {@link News} identifier
   * @param currentIdentity user attempting to access news
   * @param editMode access mode to news: whether to edit news to to view it.
   * @param newsObjectType news object type to be retrieved.
   * @return {@link News} if found else null
   * @throws IllegalAccessException when user doesn't have access to {@link News}
   */
  News getNewsById(String newsId, org.exoplatform.services.security.Identity currentIdentity, boolean editMode, String newsObjectType) throws IllegalAccessException;
  
  /**
   * Get all news
   * @param filter
   * @param currentIdentity
   * @return all news
   * @throws Exception when error
   */
  List<News> getNews(NewsFilter filter, org.exoplatform.services.security.Identity currentIdentity) throws Exception;

  /**
   * Get list of news by a given target name
   * @param filter
   * @param targetName
   * @param currentIdentity user attempting to access news
   * @return {@link News} list by target name.
   * @throws Exception when error
   */
  List<News> getNewsByTargetName(NewsFilter filter, String targetName,  org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  /**
   * get the count of News after applying a filter
   * @param filter
   * @return int the number of News
   * @throws Exception
   */
  int getNewsCount(NewsFilter filter) throws Exception;
  
  /**
   * Increment the number of views for a news
   * 
   * @param userId The current user id
   * @param news The news to be updated
   * @throws Exception when error
   */
  void markAsRead(News news, String userId) throws Exception;
  
  /**
   * Search news with the given text
   * 
   * @param filter news filter
   * @param lang language
   * @throws Exception when error
   * @return List of News returned by the search
   */
  List<News> searchNews(NewsFilter filter, String lang) throws Exception;
  
  /**
   * Retrieves a news item identified by originating Activity identifier or a shared
   * activity identifier
   * 
   * @param activityId {@link ExoSocialActivity} identifier
   * @param currentIdentity user attempting to access news
   * @return {@link News} if found else null
   * @throws IllegalAccessException when user doesn't have access to
   *           {@link News} or {@link ExoSocialActivity}
   * @throws ObjectNotFoundException when a {@link News} wasn't found for this
   *           activity identifier
   */
  News getNewsByActivityId(String activityId, org.exoplatform.services.security.Identity currentIdentity) throws IllegalAccessException, ObjectNotFoundException;
  
  /**
   * Schedule publishing a News
   * @param news
   * @param currentIdentity
   * @return the published news
   * @throws Exception when error occurs
   */
  News scheduleNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception;
  
  /**
   * Un-schedule publishing a News
   *
   * @param news
   * @param currentIdentity
   * @return unscheduled News
   * @throws Exception when error occurs
   */
  News unScheduleNews(News news, org.exoplatform.services.security.Identity currentIdentity) throws Exception;

  /**
   * Search news by term
   *
   * @param currentIdentity
   * @param filter
   * @return News Search Result
   */
  List<NewsESSearchResult> search(Identity currentIdentity, NewsFilter filter);
  
  /**
   * Checks if the user can schedule publishinga  News
   * @param space
   * @param currentIdentity
   * @return boolean : true if the user can schedule publishing a News
   */
  boolean canScheduleNews(Space space, org.exoplatform.services.security.Identity currentIdentity);

  /**
   * Checks if the user can view the News
   * @param news {@link News} to check
   * @param authenticatedUser authenticated username
   * @return true if user has access to news, else false
   */
  boolean canViewNews(News news, String authenticatedUser);
  
  /**
   * Shares a news item into a dedicated space
   * 
   * @param news {@link News} to share
   * @param space {@link Space} to share with, the news
   * @param userIdentity {@link Identity} of user making the modification
   * @param sharedActivityId newly generated activity identifier
   * @throws Exception when user doesn't have access to {@link News}
   */
  void shareNews(News news, Space space, Identity userIdentity, String sharedActivityId) throws Exception;
  
  /**
   * Archive a news
   *
   * @param newsId The id of the news to be archived
   * @param currentUserName {@link Identity} of user archiving the news
   * @throws Exception when error
   */
  void archiveNews(String newsId, String currentUserName) throws Exception;
  
  /**
   * Unarchive a news
   *
   * @param newsId The id of the news to be unarchived
   * @param currentUserName {@link Identity} of user unarchiving the news
   * @throws Exception when error
   */
  void unarchiveNews(String newsId, String currentUserName) throws Exception;
  
  /**
   * checks if the user can archive the news
   * @param currentIdentity
   * @param newsAuthor
   * @return boolean : true if user can archive the news
   */
  boolean canArchiveNews(org.exoplatform.services.security.Identity currentIdentity, String newsAuthor);
}
