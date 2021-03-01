package org.exoplatform.news;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.HTMLSanitizer;
import org.exoplatform.news.model.News;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import java.util.StringTokenizer;

public class NewsUtils {

  /**
   * Formats the body of the news to add profile link when mentioned
   *
   * @param news
   * @return News with formatted body
   */
  public static News formatNews(News news) {
    if (news == null || StringUtils.isBlank(news.getBody())) {
      return news;
    }
    HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();
    StringBuilder sb = new StringBuilder();
    StringTokenizer tokenizer = new StringTokenizer(news.getBody());
    while (tokenizer.hasMoreElements()) {
      String next = (String) tokenizer.nextElement();
      if (next.length() == 0) {
        continue;
      } else if (next.contains("@")) {
        String[] splitTable = next.split("@");
        if (splitTable.length > 1) {
          org.exoplatform.social.core.identity.model.Identity identity = loadUser(splitTable[1]);
          if (identity != null) {
            next = splitTable[0] + "<a href=\"" + identity.getProfile().getUrl() + "\">"
                    + encoder.encodeHTML(identity.getProfile().getFullName()) + "</a>";
          }
        }
      }
      sb.append(next);
      sb.append(' ');
    }
    try {
      news.setBody(HTMLSanitizer.sanitize(sb.toString().trim()));
    } catch (Exception e) {
      // Do nothing
    }
    return news;
  }

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
