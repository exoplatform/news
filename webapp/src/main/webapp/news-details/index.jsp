<%@ page import="org.exoplatform.commons.utils.CommonsUtils" %>
<%@ page import="org.exoplatform.news.NewsService" %>

<%
    NewsService newsService = CommonsUtils.getService(NewsService.class);
%>
<div class="VuetifyApp">
    <div id="newsDetails">
    </div>
</div>


<script>
    require(['PORTLET/news/NewsDetail'], function(newsDetails) {
        newsDetails.init();
    });
</script>