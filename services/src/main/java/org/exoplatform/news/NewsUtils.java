package org.exoplatform.news;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.news.notification.plugin.MentionInNewsNotificationPlugin;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class NewsUtils {

  private static final Log   LOG          = ExoLogger.getLogger(NewsUtils.class);

  public static final String POST_NEWS    = "exo.news.postArticle";

  public static final String PUBLISH_NEWS = "exo.news.PublishArticle";

  public static void broadcastEvent(String eventName, Object source, Object data) {
    try {
      ListenerService listenerService = CommonsUtils.getService(ListenerService.class);
      listenerService.broadcast(eventName, source, data);
    } catch (Exception e) {
      LOG.warn("Error broadcasting event '" + eventName + "' using source '" + source + "' and data " + data, e);
    }
  }

  /**
   * Processes Mentioners who has been mentioned via the news body.
   *
   * @param body
   * @return set of mentioned users
   */
  public static Set<String> processMentions(String body) {
    Set<String> mentions = new HashSet<>();
    mentions.addAll(parseMention(body));

    return mentions;
  }

  private static Set<String> parseMention(String str) {
    if (str == null || str.length() == 0) {
      return Collections.emptySet();
    }

    Set<String> mentions = new HashSet<>();
    Matcher matcher = MentionInNewsNotificationPlugin.MENTION_PATTERN.matcher(str);
    while (matcher.find()) {
      String remoteId = matcher.group().substring(1);
      Identity identity = loadUser(remoteId);
      // if not the right mention then ignore
      if (identity != null && !mentions.contains(identity.getRemoteId())) {
        mentions.add(identity.getRemoteId());
      }
    }
    return mentions;
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

  public static List<Space> getSpacesWhichIsManagerOrRedactor(String userId) throws SpaceException {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    List<Space> spaces = spaceService.getSpaces(userId);
    return spaces.stream().filter(space -> {
      if (spaceService.isManager(space, userId) || spaceService.isRedactor(space, userId)) {
        return true;
      }
      return false;
    }).collect(Collectors.toList());
  }

}
