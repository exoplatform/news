package org.exoplatform.news.notification.utils;

public class NotificationConstants {
  public static final String CONTENT_TITLE    = "CONTENT_TITLE";

  public static final String CONTENT_AUTHOR   = "CONTENT_AUTHOR";

  public static final String CONTENT_URL      = "CONTENT_URL";

  public static final String ILLUSTRATION_URL = "ILLUSTRATION_URL";

  public static final String CONTENT_SPACE    = "CONTENT_SPACE";

  public static final String CONTENT_SPACE_ID = "CONTENT_SPACE_ID";

  public static final String ACTIVITY_LINK    = "ACTIVITY_LINK";

  public static final String CONTEXT          = "CONTEXT";

  public static final String CURRENT_USER     = "CURRENT_USER";

  public static enum NOTIFICATION_CONTEXT {
    POST_NEWS ("POST NEWS"),
    SHARE_NEWS ("SHARE NEWS"),
    SHARE_MY_NEWS ("SHARE MY NEWS"),
    LIKE_MY_NEWS ("LIKE MY NEWS"),
    LIKE_MY_SHARED_NEWS ("LIKE MY SHARED NEWS"),
    COMMENT_MY_NEWS ("COMMENT MY NEWS"),
    COMMENT_MY_SHARED_NEWS ("COMMENT MY SHARED NEWS"),
    MENTION_IN_NEWS ("MENTION IN NEWS");

    private String context;

    private NOTIFICATION_CONTEXT(String context) {
      this.context = context;
    }

    public String getContext() {
      return this.context;
    }
  }

}
