package org.exoplatform.news;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.notification.plugin.MentionInNewsNotificationPlugin;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

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

  public static List<Space> getRedactorOrManagerSpaces(String userId) throws Exception {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    ListAccess<Space> memberSpacesListAccess = spaceService.getMemberSpaces(userId);
    List<Space> spaces = Arrays.asList(memberSpacesListAccess.load(0, memberSpacesListAccess.getSize()));
    return spaces.stream()
                 .filter(space -> (spaceService.isManager(space, userId) || spaceService.isRedactor(space, userId)))
                 .collect(Collectors.toList());
  }

  public static org.exoplatform.services.security.Identity getUserIdentity(String username) {
    IdentityRegistry identityRegistry = ExoContainerContext.getService(IdentityRegistry.class);
    org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
    if (identity != null) {
      return identity;
    }
    Authenticator authenticator = ExoContainerContext.getService(Authenticator.class);
    try {
      identity = authenticator.createIdentity(username);
      if (identity != null) {
        // To cache identity for next times
        identityRegistry.register(identity);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error occurred while retrieving security identity of user " + username);
    }
    return identity;
  }

}
