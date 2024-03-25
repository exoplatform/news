package org.exoplatform.news.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.utils.MentionUtils;

public class NewsUtils {

  private static final Log    LOG                             = ExoLogger.getLogger(NewsUtils.class);

  public static final String  POST_NEWS                       = "exo.news.postArticle";

  public static final String  POST_NEWS_ARTICLE               = "exo.news.gamification.postArticle";

  public static final String  PUBLISH_NEWS                    = "exo.news.gamification.PublishArticle";

  public static final String  VIEW_NEWS                       = "exo.news.viewArticle";

  public static final String  SHARE_NEWS                      = "exo.news.shareArticle";

  public static final String  ARCHIVE_NEWS                    = "exo.news.archiveArticle";

  public static final String  UNARCHIVE_NEWS                  = "exo.news.unarchiveArticle";

  public static final String  COMMENT_NEWS                    = "exo.news.commentArticle";

  public static final String  LIKE_NEWS                       = "exo.news.likeArticle";

  public static final String  DELETE_NEWS                     = "exo.news.deleteArticle";

  public static final String  UPDATE_NEWS                     = "exo.news.updateArticle";

  public static final String  SCHEDULE_NEWS                   = "exo.news.scheduleArticle";

  public static final String  UNSCHEDULE_NEWS                 = "exo.news.unscheduleArticle";

  public static final String  NEWS_METADATA_OBJECT_TYPE       = "news";

  public static final String  DISPLAYED_STATUS                = "displayed";

  public static final String  TARGET_PERMISSIONS              = "permissions";

  public static final String  SPACE_NEWS_AUDIENCE             = "space";

  public static final String  ALL_NEWS_AUDIENCE               = "all";

  private static final String PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private static final String MANAGER_MEMBERSHIP_NAME         = "manager";

  private static final String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  public enum NewsObjectType {
    DRAFT, LATEST_DRAFT, ARTICLE;
  }

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
   * @param content : the content in which update mention
   * @param space : the space of the news (for group mentioning)
   * @return set of mentioned users
   */
  public static Set<String> processMentions(String content, Space space) {
    Set<String> mentions = new HashSet<>();
    mentions.addAll(MentionUtils.getMentionedUsernames(content));

    if (space != null) {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);
      String spaceIdentityId = identityStorage.findIdentityId(SpaceIdentityProvider.NAME, space.getPrettyName());
      Set<String> mentionedRoles = MentionUtils.getMentionedRoles(content, spaceIdentityId);
      mentionedRoles.forEach(role -> {
        if (StringUtils.equals("member", role) && space.getMembers() != null) {
          mentions.addAll(Arrays.asList(space.getMembers()));
        } else if (StringUtils.equals("manager", role) && space.getManagers() != null) {
          mentions.addAll(Arrays.asList(space.getManagers()));
        } else if (StringUtils.equals("redactor", role) && space.getRedactors() != null) {
          mentions.addAll(Arrays.asList(space.getRedactors()));
        } else if (StringUtils.equals("publisher", role) && space.getPublishers() != null) {
          mentions.addAll(Arrays.asList(space.getPublishers()));
        }
      });
    }

    return mentions.stream().map(remoteId -> {
      IdentityStorage identityStorage = CommonsUtils.getService(IdentityStorage.class);

      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
      return identity == null ? null : identity.getId();
    }).filter(Objects::nonNull).collect(Collectors.toSet());
  }

  public static List<Space> getAllowedDraftNewsSpaces(org.exoplatform.services.security.Identity userIdentity) throws Exception {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    ListAccess<Space> memberSpacesListAccess = spaceService.getMemberSpaces(userIdentity.getUserId());
    List<Space> memberSpaces = Arrays.asList(memberSpacesListAccess.load(0, memberSpacesListAccess.getSize()));
    return memberSpaces.stream().filter(space -> (spaceService.canRedactOnSpace(space, userIdentity) || canPublishNews(space.getId(), userIdentity))).toList();
  }

  public static List<Space> getAllowedScheduledNewsSpaces(org.exoplatform.services.security.Identity currentIdentity) throws Exception {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    ListAccess<Space> memberSpacesListAccess = spaceService.getMemberSpaces(currentIdentity.getUserId());
    List<Space> memberSpaces = Arrays.asList(memberSpacesListAccess.load(0, memberSpacesListAccess.getSize()));
    return memberSpaces.stream()
                       .filter(space -> (spaceService.isManager(space, currentIdentity.getUserId())
                           || spaceService.isRedactor(space, currentIdentity.getUserId())
                           || canPublishNews(space.getId(), currentIdentity)))
                       .toList();
  }

  public static boolean canPublishNews(String spaceId, org.exoplatform.services.security.Identity currentIdentity) {
    if (!StringUtils.isBlank(spaceId)) {
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceById(spaceId);
      return currentIdentity != null && space != null && spaceService.isMember(space, currentIdentity.getUserId())
          && (currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
              || spaceService.isPublisher(space, currentIdentity.getUserId()));
    }
    return currentIdentity != null && currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME);
  }

  public static boolean canManageNewsPublishTargets(org.exoplatform.services.security.Identity currentIdentity) {
    return currentIdentity != null && currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, MANAGER_MEMBERSHIP_NAME);
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
