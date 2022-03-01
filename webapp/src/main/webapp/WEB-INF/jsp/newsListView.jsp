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
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="org.exoplatform.services.security.ConversationState"%>
<%@page import="org.exoplatform.services.security.Identity"%>
<%@ page import="org.exoplatform.news.utils.NewsUtils" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>

<portlet:defineObjects />
<portlet:actionURL var="saveSettingsURL" />

<div id="newsListViewApp" class="VuetifyApp">
  <%
    int generatedId = (int) (Math.random() * 1000000l);
    String appId = "news-list-view-" + generatedId;
    String[] viewTemplateParams = (String[]) request.getAttribute("viewTemplate");
    String[] newsTargetParams = (String[]) request.getAttribute("newsTarget");
    String[] headerParams = (String[]) request.getAttribute("header");
    String[] limitParams = (String[]) request.getAttribute("limit");
    String[] showHeaderParams = (String[]) request.getAttribute("showHeader");
    String[] showSeeAllParams = (String[]) request.getAttribute("showSeeAll");
    String[] showArticleTitleParams = (String[]) request.getAttribute("showArticleTitle");
    String[] showSummaryParams = (String[]) request.getAttribute("showSummary");
    String[] showArticleImageParams = (String[]) request.getAttribute("showArticleImage");
    String[] showArticleAuthorParams = (String[]) request.getAttribute("showArticleAuthor");
    String[] showArticleSpaceParams = (String[]) request.getAttribute("showArticleSpace");
    String[] showArticleReactionsParams = (String[]) request.getAttribute("showArticleReactions");
    String[] showArticleDateParams = (String[]) request.getAttribute("showArticleDate");
    String[] seeAllUrlParams = (String[]) request.getAttribute("seeAllUrl");
    String[] selectedTypeParams = (String[]) request.getAttribute("selectedType");
    String viewTemplate = viewTemplateParams == null || viewTemplateParams.length == 0 ? "": viewTemplateParams[0];
    String newsTarget = newsTargetParams == null || newsTargetParams.length == 0 ? "": newsTargetParams[0];
    String header = headerParams == null || headerParams.length == 0 ? "": headerParams[0];
    String limit = limitParams == null || limitParams.length == 0 ? "": limitParams[0];
    String showHeader = showHeaderParams == null || showHeaderParams.length == 0 ? "": showHeaderParams[0];
    String showSeeAll = showSeeAllParams == null || showSeeAllParams.length == 0 ? "": showSeeAllParams[0];
    String showArticleTitle = showArticleTitleParams == null || showArticleTitleParams.length == 0 ? "": showArticleTitleParams[0];
    String showSummary = showSummaryParams == null || showSummaryParams.length == 0 ? "": showSummaryParams[0];
    String showArticleImage = showArticleImageParams == null || showArticleImageParams.length == 0 ? "": showArticleImageParams[0];
    String showArticleAuthor = showArticleAuthorParams == null || showArticleAuthorParams.length == 0 ? "": showArticleAuthorParams[0];
    String showArticleDate = showArticleDateParams == null || showArticleDateParams.length == 0 ? "": showArticleDateParams[0];
    String showArticleSpace = showArticleSpaceParams == null || showArticleSpaceParams.length == 0 ? "": showArticleSpaceParams[0];
    String showArticleReactions = showArticleReactionsParams == null || showArticleReactionsParams.length == 0 ? "": showArticleReactionsParams[0];
    String seeAllUrl = seeAllUrlParams == null || seeAllUrlParams.length == 0 ? "": seeAllUrlParams[0];
    String selectedType = selectedTypeParams == null || selectedTypeParams.length == 0 ? "": selectedTypeParams[0];

    ConversationState conversationState = ConversationState.getCurrent();
    Identity currentIdentity = null;
    if (conversationState != null) {
      currentIdentity = ConversationState.getCurrent().getIdentity();
    }
    if (!NewsUtils.canPublishNews(currentIdentity)) {
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
        header: <%= header == null ? null : "'" + header + "'" %>,
        showHeader: <%= showHeader == null ? null : "'" + showHeader + "'" %>,
        limit: <%= limit == null ? null : "'" + limit + "'" %>,
        showSeeAll: <%= showSeeAll == null ? null : "'" + showSeeAll + "'" %>,
        showArticleTitle: <%= showArticleTitle == null ? null : "'" + showArticleTitle + "'" %>,
        showSummary: <%= showSummary == null ? null : "'" + showSummary + "'" %>,
        showArticleImage: <%= showArticleImage == null ? null : "'" + showArticleImage + "'" %>,
        showArticleAuthor: <%= showArticleAuthor == null ? null : "'" + showArticleAuthor + "'" %>,
        showArticleSpace: <%= showArticleSpace == null ? null : "'" + showArticleSpace + "'" %>,
        showArticleReactions: <%= showArticleReactions == null ? null : "'" + showArticleReactions + "'" %>,
        showArticleDate: <%= showArticleDate == null ? null : "'" + showArticleDate + "'" %>,
        seeAllUrl: <%= seeAllUrl == null ? null : "'" + seeAllUrl + "'" %>,
        selectedType: <%= selectedType == null ? null : "'" + selectedType + "'" %>,
      }));
    </script>
  </div>
</div>