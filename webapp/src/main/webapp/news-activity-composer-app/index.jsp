<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.news.NewsService" %>

<%
  NewsService newsService = CommonsUtils.getService(NewsService.class);
  boolean showPinInput = newsService.canPinNews();
%>
<div class="VuetifyApp">
  <div id="NewsComposerApp">
  </div>
</div>


<script>
require(['PORTLET/news/NewsComposer'], function(newsComposer) {
  newsComposer.init(<%=showPinInput%>);
});
</script>