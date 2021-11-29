package org.exoplatform.news.notification.utils;

public class NotificationConstants {

  public static final String NEWS_ID          = "NEWS_ID";

  public static final String CONTENT_TITLE    = "CONTENT_TITLE";

  public static final String CONTENT_AUTHOR   = "CONTENT_AUTHOR";
  
  public static final String CONTENT_BODY    = "CONTENT_BODY";

  public static final String CONTENT_URL      = "CONTENT_URL";

  public static final String ILLUSTRATION_URL = "ILLUSTRATION_URL";

  public static final String AUTHOR_AVATAR_URL = "AUTHOR_AVATAR_URL";

  public static final String CONTENT_SPACE    = "CONTENT_SPACE";

  public static final String CONTENT_SPACE_ID = "CONTENT_SPACE_ID";
  
  public static final String ACTIVITY_LINK    = "ACTIVITY_LINK";

  public static final String CONTEXT          = "CONTEXT";

  public static final String CURRENT_USER     = "CURRENT_USER";

  public static final String MENTIONED_IDS    = "MENTIONED_IDS";

  public static enum NOTIFICATION_CONTEXT {
    POST_NEWS ("POST NEWS"),
    MENTION_IN_NEWS ("MENTION IN NEWS"),
    PUBLISH_IN_NEWS("PUBLISH NEWS");

    private String context;

    private NOTIFICATION_CONTEXT(String context) {
      this.context = context;
    }

    public String getContext() {
      return this.context;
    }
  }

}
