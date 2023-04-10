/*
 * Copyright (C) 2003-2022 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.listener;

import org.exoplatform.analytics.model.StatisticData;
import org.exoplatform.analytics.utils.AnalyticsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.model.News;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.listener.*;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import static org.exoplatform.analytics.utils.AnalyticsUtils.addSpaceStatistics;

@Asynchronous
public class AnalyticsNewsListener extends Listener<String, News> {

  private static final String CREATE_CONTENT_OPERATION_NAME  = "createContent";

  private static final String UPDATE_CONTENT_OPERATION_NAME  = "updateContent";

  private static final String DELETE_CONTENT_OPERATION_NAME  = "deleteContent";

  private static final String VIEW_CONTENT_OPERATION_NAME    = "viewContent";

  private static final String SHARE_CONTENT_OPERATION_NAME   = "shareContent";

  private static final String LIKE_CONTENT_OPERATION_NAME    = "likeContent";

  private static final String COMMENT_CONTENT_OPERATION_NAME = "commentContent";

  private IdentityManager     identityManager;

  private SpaceService        spaceService;

  @Override
  public void onEvent(Event<String, News> event) throws Exception {
    News news = event.getData();
    String operation = "";
    switch (event.getEventName()) {
    case "exo.news.postArticle":
      operation = CREATE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.updateArticle":
      operation = UPDATE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.deleteArticle":
      operation = DELETE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.viewArticle":
      operation = VIEW_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.shareArticle":
      operation = SHARE_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.commentArticle":
      operation = COMMENT_CONTENT_OPERATION_NAME;
      break;
    case "exo.news.likeArticle":
      operation = LIKE_CONTENT_OPERATION_NAME;
      break;
    }
    long userId = 0;
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, event.getSource());
    if (identity != null) {
      userId = Long.parseLong(identity.getId());
    }
    StatisticData statisticData = new StatisticData();

    statisticData.setModule("contents");
    statisticData.setSubModule("contents");
    statisticData.setOperation(operation);
    statisticData.setUserId(userId);
    statisticData.addParameter("contentId", news.getId());
    statisticData.addParameter("contentTitle", news.getTitle());
    statisticData.addParameter("contentAuthor", news.getAuthor());
    statisticData.addParameter("contentLastModifier", news.getUpdater());
    statisticData.addParameter("contentType", "News");
    statisticData.addParameter("contentUpdatedDate", news.getUpdateDate());
    statisticData.addParameter("contentCreationDate", news.getCreationDate());
    statisticData.addParameter("contentPublication", news.isPublished() ? "Yes" : "No");
    if (news.isPublished() && (operation.equals(CREATE_CONTENT_OPERATION_NAME) || operation.equals(UPDATE_CONTENT_OPERATION_NAME))) {
      statisticData.addParameter("contentPublicationAudience", news.getAudience().equals(NewsUtils.ALL_NEWS_AUDIENCE) ? "All users" : "Only space members");
    }
    Space space = getSpaceService().getSpaceById(news.getSpaceId());
    if (space != null) {
      addSpaceStatistics(statisticData, space);
    }
    AnalyticsUtils.addStatisticData(statisticData);
  }

  public IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = ExoContainerContext.getService(IdentityManager.class);
    }
    return identityManager;
  }

  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = ExoContainerContext.getService(SpaceService.class);
    }
    return spaceService;
  }
}
