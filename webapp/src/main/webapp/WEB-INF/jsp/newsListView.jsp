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