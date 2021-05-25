<template>
  <v-app :id="id">
    <share-activity-drawer
      ref="shareNewsDrawer"
      class="shareNewsDrawer"
      :activity-id="activityId"
      @share-activity="shareNews" />
    <exo-news-notification-alerts />
  </v-app>
</template>
<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: function() { return new Object(); }
    },
    id: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    activityId: '',
    activityType: '',
  }),
  created() {
    this.$root.$on('news-share-drawer-open', params => {
      this.newsId = params.news.newsId;
      this.openDrawer();
    });
  },
  methods: {
    openDrawer() {
      this.$refs.shareNewsDrawer.open();
    },
    shareNews(spaces, description) {
      const spacesList = [];
      spaces.forEach(space => {
        this.$spaceService.getSpaceByPrettyName(space,'identity').then(data => {
          spacesList.push(data.displayName);
        });
      });
      this.$newsServices.shareNews(this.news.newsId, this.news.activityId, description, spaces)
        .then(() => {
          this.$root.$emit('news-shared', this.news, spacesList);
          this.close();
        });
    },
    close() {
      this.$refs.shareNewsDrawer.close();
    },
  }
};
</script>