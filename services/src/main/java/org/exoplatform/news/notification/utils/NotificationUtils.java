package org.exoplatform.news.notification.utils;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.news.model.News;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;

public class NotificationUtils {

  public static String getUserFullName(String userName) throws Exception {
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    UserHandler userHandler = organizationService.getUserHandler();
    User user = userHandler.findUserByName(userName);
    if (user == null) {
      throw new Exception("An error occured when trying to retreive a user with username " + userName);
    }
    return user.getFullName();
  }

  public static String getNewsIllustration(News news) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    RepositoryService repositoryService = CommonsUtils.getService(RepositoryService.class);
    Session session = sessionProvider.getSession(
                                                 repositoryService.getCurrentRepository()
                                                                  .getConfiguration()
                                                                  .getDefaultWorkspaceName(),
                                                 repositoryService.getCurrentRepository());
    StringBuffer illustrationURL = new StringBuffer();
    String currentDomain = CommonsUtils.getCurrentDomain();
    if (!currentDomain.endsWith("/")) {
      currentDomain += "/";
    }
    try {
      Node newsNode = session.getNodeByUUID(news.getId());
      if (newsNode == null) {
        throw new ItemNotFoundException("Cannot find a node with UUID equals to " + news.getId() + ", it may not exist");
      }
      if (newsNode.hasNode("illustration")) {
        illustrationURL.append(currentDomain).append("portal/rest/v1/news/").append(news.getId()).append("/illustration");
      } else {
        illustrationURL.append(currentDomain).append("news/images/newsImageDefault.png");
      }
    } finally {
      if (session != null) {
        session.logout();
      }
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
