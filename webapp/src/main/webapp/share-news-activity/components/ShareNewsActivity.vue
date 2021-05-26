<template>
  <v-app :id="id">
    <share-activity-drawer
      ref="shareNewsDrawer"
      class="shareNewsDrawer"
      @share-activity="shareNews" />
    <exo-news-notification-alerts />
  </v-app>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: ''
    }
  },
  created() {
    this.$root.$on('news-share-drawer-open', params => {
      this.newsId = params.newsId;
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
      this.$newsServices.shareNews(this.newsId, description, spaces)
        .then(() => {
          this.$root.$emit('news-shared', this.newsId, spacesList);
          this.$root.$emit('clear-suggester', this.newsId, spacesList);
          this.close();
        });
    },
    close() {
      this.$refs.shareNewsDrawer.close();
    },
  }
};
</script>