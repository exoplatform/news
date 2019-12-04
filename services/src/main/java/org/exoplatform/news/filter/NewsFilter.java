package org.exoplatform.news.filter;

public class NewsFilter {

  private boolean pinnedNews;

  private String searchText;

  private String order;

  public NewsFilter(){
  }

  public boolean isPinnedNews() {
    return pinnedNews;
  }

  public void setPinnedNews(boolean pinnedNews) {
    this.pinnedNews = pinnedNews;
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
}
