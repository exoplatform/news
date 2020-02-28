<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.news.NewsService" %>
<%@ page import="org.exoplatform.commons.utils.PropertyManager" %>

<%
  NewsService newsService = CommonsUtils.getService(NewsService.class);
  PropertyManager propertyManager = CommonsUtils.getService(PropertyManager.class);

  boolean showPinInput = newsService.canPinNews();
  String maxToUpload = propertyManager.getProperty("exo.social.composer.maxToUpload");
  String maxFileSize = propertyManager.getProperty("exo.social.composer.maxFileSizeInMB");
%>

<div id="NewsComposerApp"></div>

<script>
require(['PORTLET/news/NewsComposer'], function(newsComposer) {
  newsComposer.init(<%=showPinInput%>, <%=maxToUpload%>, <%=maxFileSize%>);
});
</script>