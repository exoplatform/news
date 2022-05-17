package org.exoplatform.news.notification.utils;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.model.News;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationUtils {

  public static List<String> getReceivers(String contentSpaceId,
                                    String currentUserName) throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    UserHandler userHandler = organizationService.getUserHandler();
    Space space = spaceService.getSpaceById(contentSpaceId);
    ListAccess<User> members =  userHandler.findUsersByGroupId(space.getGroupId());
    User[] userArray = members.load(0, members.getSize());
    return Arrays.stream(userArray)
            .filter(u -> !u.getUserName().equals(currentUserName))
            .distinct()
            .map(User::getUserName)
            .collect(Collectors.toList());
  }
  public static String getUserFullName(String userName) throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(userName);
    if (user == null) {
      throw new Exception("An error occured when trying to retreive a user with username " + userName);
    }
    return user.getFullName();
  }

  public static String getNewsIllustration(News news, Session session) throws Exception {
    StringBuffer illustrationURL = new StringBuffer();
    String currentDomain = CommonsUtils.getCurrentDomain();
    if (!currentDomain.endsWith("/")) {
      currentDomain += "/";
    }
    Node newsNode = session.getNodeByUUID(news.getId());
    if (newsNode == null) {
      throw new ItemNotFoundException("Cannot find a node with UUID equals to " + news.getId() + ", it may not exist");
    }
    if (newsNode.hasNode("illustration")) {
      illustrationURL.append(currentDomain).append("portal/rest/v1/news/").append(news.getId()).append("/illustration");
    } else {
      illustrationURL.append(currentDomain).append("news/images/news.png");
    }
    return illustrationURL.toString();
  }

  public static String getNotificationActivityLink(Space space, String activityId, boolean isMember) {
    String activityLink = "";
    if (isMember) {
      activityLink = getActivityPermalink(activityId);
    } else {
      activityLink = getNotificationActivityLinkForNotSpaceMembers(space);
    }
    String baseUrl = PropertyManager.getProperty("gatein.email.domain.url");
    return baseUrl == null ? activityLink : baseUrl.concat(activityLink);
  }

  private static String getActivityPermalink(String activityId) {
    return LinkProvider.getSingleActivityUrl(activityId);
  }

  public static String getNotificationActivityLinkForNotSpaceMembers(Space space) {
    return "/".concat(PortalContainer.getCurrentPortalContainerName())
              .concat("/g/:spaces:")
              .concat(space.getGroupId().replaceFirst("/spaces/", ""))
              .concat("/")
              .concat(space.getPrettyName());
  }

}
