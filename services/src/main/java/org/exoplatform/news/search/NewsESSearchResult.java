package org.exoplatform.news.search;

import org.exoplatform.social.core.identity.model.Identity;

import java.util.List;

public class NewsESSearchResult {

  private String       id;

  private String       title;

  private Identity     poster;

  private String       body;

  private String       spaceDisplayName;

  private String       newsUrl;

  private List<String> excerpts;

  private long         postedTime;

  private long         lastUpdatedTime;

  private String         activityId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Identity getPoster() {
    return poster;
  }

  public void setPoster(Identity poster) {
    this.poster = poster;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public long getLastUpdatedTime() {
    return lastUpdatedTime;
  }

  public void setLastUpdatedTime(long lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  public String getSpaceDisplayName() {
    return spaceDisplayName;
  }

  public void setSpaceDisplayName(String spaceDisplayName) {
    this.spaceDisplayName = spaceDisplayName;
  }

  public String getNewsUrl() {
    return newsUrl;
  }

  public void setNewsUrl(String newsUrl) {
    this.newsUrl = newsUrl;
  }

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
}
