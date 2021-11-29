/*
 * Copyright (C) 2003-2021 eXo Platform SAS.
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

import javax.jcr.Node;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.news.model.News;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.wcm.extensions.publication.lifecycle.authoring.AuthoringPublicationConstant;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;

public class NewsPublicationListener extends Listener<CmsService, Node> {

  private NewsService            newsService;

  public NewsPublicationListener() {
    newsService = WCMCoreUtils.getService(NewsService.class);
  }

  public void onEvent(Event<CmsService, Node> event) throws Exception {
    if (AuthoringPublicationConstant.POST_CHANGE_STATE_EVENT.equals(event.getEventName())) {
      Node targetNode = event.getData();
      if (targetNode.isNodeType(NewsUtils.EXO_NEWS) && targetNode.getProperty(StageAndVersionPublicationConstant.CURRENT_STATE)
                                                         .getString()
                                                         .equals(PublicationDefaultStates.PUBLISHED)) {
        News news = newsService.getNewsById(targetNode.getUUID(), false);
        if (StringUtils.isEmpty(news.getActivities())) {
          newsService.postNews(news, news.getAuthor());
        }
      }
    }
  }
}