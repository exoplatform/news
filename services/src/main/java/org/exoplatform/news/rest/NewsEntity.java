package org.exoplatform.news.rest;

import org.exoplatform.news.model.News;

import java.util.List;

public class NewsEntity {

  private List<News> news;

  private Integer offset;

  private Integer limit;

  private Integer size;

  public List<News> getNews() {
    return news;
  }

  public void setNews(List<News> news) {
    this.news = news;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public Integer getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public Integer getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
