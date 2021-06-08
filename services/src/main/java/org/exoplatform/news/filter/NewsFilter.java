package org.exoplatform.news.filter;

import java.util.List;

public class NewsFilter {

  private boolean      pinnedNews;

  private boolean      archivedNews;

  private boolean      draftNews;

  private String       searchText;

  private String       order;

  private List<String> spaces;

  private String author;

  private int offset;

  private int limit;

  public NewsFilter(){
  }

  public boolean isPinnedNews() {
    return pinnedNews;
  }

  public void setPinnedNews(boolean pinnedNews) {
    this.pinnedNews = pinnedNews;
  }

  public boolean isArchivedNews() {
    return archivedNews;
  }

  public void setArchivedNews(boolean archivedNews) {
    this.archivedNews = archivedNews;
  }

  public boolean isDraftNews() {
    return draftNews;
  }

  public void setDraftNews(boolean draftNews) {
    this.draftNews = draftNews;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public List<String> getSpaces() {
    return spaces;
  }

  public void setSpaces(List<String> spaces) {
    this.spaces = spaces;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }
}
