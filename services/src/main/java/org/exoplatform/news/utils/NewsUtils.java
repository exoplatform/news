/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

  private static final Log   LOG               = ExoLogger.getLogger(NewsUtils.class);

  public static final String POST_NEWS         = "exo.news.postArticle";

  public static final String POST_NEWS_ARTICLE = "exo.news.gamification.postArticle";

  public static final String PUBLISH_NEWS      = "exo.news.gamification.PublishArticle";

  public static final String VIEW_NEWS         = "exo.news.viewArticle";

  public static final String SHARE_NEWS        = "exo.news.shareArticle";

  public static final String COMMENT_NEWS      = "exo.news.commentArticle";

  public static final String LIKE_NEWS         = "exo.news.likeArticle";

  public static final String DELETE_NEWS       = "exo.news.deleteArticle";

  public static final String UPDATE_NEWS       = "exo.news.updateArticle";

  public static final String EXO_NEWS          = "exo:news";

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
