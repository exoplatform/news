<template>
  <div id="newsDetail">
    <exo-news-details
      :news="news"
      :news-id="newsId" />
  </div>
</template>

<script>
export default {
  props: {
    newsId: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    news: null,
  }),
  created() {
    this.$newsServices.getNewsById(this.newsId, false)
      .then(news => {
        this.news = news;
      });
  }
};
</script>