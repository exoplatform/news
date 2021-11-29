package org.exoplatform.news.filter;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewsFilter {

  private boolean      publishedNews;

  private boolean      archivedNews;

  private boolean      draftNews;
  
  private boolean      scheduledNews;

  private String       searchText;

  private String       order;

  private List<String> spaces;

  private String author;

  private int offset;

  private int limit;
}
