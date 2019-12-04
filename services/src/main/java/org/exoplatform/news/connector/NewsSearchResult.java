package org.exoplatform.news.connector;

import org.exoplatform.commons.api.search.data.SearchResult;

import javax.jcr.Node;

public class NewsSearchResult extends SearchResult {
  private Node node;

  public NewsSearchResult(String url, String title, String excerpt, String detail, String imageUrl, long date, long relevancy, Node node) {
    super(url, title, excerpt, detail, imageUrl, date, relevancy);
    this.node = node;
  }

  public Node getNode() {
    return node;
  }
}
