<!--
Copyright (C) 2021 eXo Platform SAS.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
-->
<%@page import="org.exoplatform.news.NewsService"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />
<portlet:actionURL var="saveSettingsURL" />

<div class="VuetifyApp">
  <%
    int generatedId = (int) (Math.random() * 1000000l);
    String appId = "news-list-view-" + generatedId;
    String[] viewTemplateParams = (String[]) request.getAttribute("viewTemplate");
    String[] newsTargetParams = (String[]) request.getAttribute("newsTarget");
    String[] limitParams = (String[]) request.getAttribute("limit");
    String viewTemplate = viewTemplateParams == null || viewTemplateParams.length == 0 ? "": viewTemplateParams[0];
    String newsTarget = newsTargetParams == null || newsTargetParams.length == 0 ? "": newsTargetParams[0];
    String limit = limitParams == null || limitParams.length == 0 ? "10": limitParams[0];

    NewsService newsService = ExoContainerContext.getService(NewsService.class);
    if (!newsService.canPublishNews()) {
      saveSettingsURL = null;
    }
  %>
  <div class="news-list-view-app" id="<%= appId %>">
    <script type="text/javascript">
      require(['PORTLET/news/NewsListView'], app => app.init({
        appId: '<%=appId%>',
        saveSettingsURL: <%= saveSettingsURL == null ? null : "'" + saveSettingsURL + "'" %>,
        viewTemplate: <%= viewTemplate == null ? null : "'" + viewTemplate + "'" %>,
        newsTarget: <%= newsTarget == null ? null : "'" + newsTarget + "'" %>,
        limit: <%=limit%>,
      }));
    </script>
  </div>
</div>