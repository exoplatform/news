<template>
  <div id="newsDetail">
    <div v-if="notFound" class="articleNotFound">
      <i class="iconNotFound"></i>
      <h3>{{ $t('news.app.noNews') }}</h3>
    </div>
    <exo-news-details
      v-else
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
    notFound: false,
  }),
  created() {
    this.$newsServices.getNewsById(this.newsId, false)
      .then(news => {
        if (news !== 'error') {
          this.news = news;
          if (!this.news.spaceMember) {
            this.$root.$emit('restricted-space', this.news.spaceDisplayName);
          }
        } else {
          this.notFound = true;
        }
      });
  },
};
</script>