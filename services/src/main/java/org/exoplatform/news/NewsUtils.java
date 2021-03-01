package org.exoplatform.news;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.plugin.MentionInNewsNotificationPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

public class NewsUtils {
  /**
   * Load the user identity
   *
   * @param username
   * @return Identity of user
   */
  private static Identity loadUser(String username) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    if (username == null || username.isEmpty()) {
      return null;
    }
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, username);
  }
}
