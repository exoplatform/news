package org.exoplatform.news.model;

import java.util.List;

public class SharedNews {

  private String newsId;

  private String poster;

  private String description;

  private List<String> spacesNames;

  private String activityId;

  public String getNewsId() {
    return newsId;
  }

  public void setNewsId(String newsId) {
    this.newsId = newsId;
  }

  public String getPoster() {
    return poster;
  }

  public void setPoster(String poster) {
    this.poster = poster;
  }

  public String getDescription() {
    return description;
  }

  public List<String> getSpacesNames() {
    return spacesNames;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setSpacesNames(List<String> spacesNames) {
    this.spacesNames = spacesNames;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }
}
