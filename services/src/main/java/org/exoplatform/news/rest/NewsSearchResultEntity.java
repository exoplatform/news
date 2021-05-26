package org.exoplatform.news.rest;

import java.util.List;

import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.social.rest.entity.BaseEntity;
import org.exoplatform.social.rest.entity.IdentityEntity;

public class NewsSearchResultEntity extends BaseEntity {

  private static final long serialVersionUID = 1L;

  private IdentityEntity    poster;

  private String            title;

  private String            body;

  private String            spaceDisplayName;

  private String            newsUrl;

  private List<String>      excerpts;

  private long              postedTime;

  private long              lastUpdatedTime;

  public NewsSearchResultEntity() {
  }

  public NewsSearchResultEntity(NewsESSearchResult newsESSearchResult) {
    this.setId(String.valueOf(newsESSearchResult.getId()));
    this.body = newsESSearchResult.getBody();
    this.title = newsESSearchResult.getTitle();
    this.newsUrl = newsESSearchResult.getNewsUrl();
    this.spaceDisplayName = newsESSearchResult.getSpaceDisplayName();
    this.excerpts = newsESSearchResult.getExcerpts();
    this.postedTime = newsESSearchResult.getPostedTime();
    this.lastUpdatedTime = newsESSearchResult.getLastUpdatedTime();
  }

  public IdentityEntity getPoster() {
    return poster;
  }

  public void setPoster(IdentityEntity poster) {
    this.poster = poster;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public List<String> getExcerpts() {
    return excerpts;
  }

  public void setExcerpts(List<String> excerpts) {
    this.excerpts = excerpts;
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

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

}
