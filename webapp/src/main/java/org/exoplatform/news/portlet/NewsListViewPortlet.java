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
package org.exoplatform.news.portlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.portlet.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.portlet.GenericDispatchedViewPortlet;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

public class NewsListViewPortlet extends GenericDispatchedViewPortlet {

  private NewsService newsService;

  @Override
  public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
    ConversationState conversationState = ConversationState.getCurrent();
    Identity currentIdentity = null;
    if (conversationState != null) {
      currentIdentity = ConversationState.getCurrent().getIdentity();
    }
    if (NewsUtils.canPublishNews(currentIdentity)) {
      PortletPreferences preferences = request.getPreferences();
      Enumeration<String> parameterNames = request.getParameterNames();
      while (parameterNames.hasMoreElements()) {
        String name = parameterNames.nextElement();
        if (StringUtils.equals(name, "action") || StringUtils.contains(name, "portal:")) {
          continue;
        }
        String value = request.getParameter(name);
        preferences.setValue(name, value);
      }
      preferences.store();
    } else {
      throw new PortletException("Illegal Access to attempt to store News List Portlet preferences for user "
          + request.getRemoteUser());
    }
  }

  public NewsService getNewsService() {
    if (newsService == null) {
      newsService = ExoContainerContext.getService(NewsService.class);
    }
    return newsService;
  }

}
