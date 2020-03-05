<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.news.NewsService" %>
<%@ page import="org.exoplatform.commons.utils.PropertyManager" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>

<%
  NewsService newsService = CommonsUtils.getService(NewsService.class);
  PropertyManager propertyManager = CommonsUtils.getService(PropertyManager.class);

  boolean showPinInput = newsService.canPinNews();
  String maxToUpload = propertyManager.getProperty("exo.social.composer.maxToUpload");
  if(StringUtils.isBlank(maxToUpload)) {
    maxToUpload = "20";
  }
  String maxFileSize = propertyManager.getProperty("exo.social.composer.maxFileSizeInMB");
  if(StringUtils.isBlank(maxFileSize)) {
    maxFileSize = "200";
  }
%>

<div id="NewsComposerApp"></div>

<script>
require(['PORTLET/news/NewsComposer'], function(newsComposer) {
  newsComposer.init(<%=showPinInput%>, <%=maxToUpload%>, <%=maxFileSize%>);
});
</script>