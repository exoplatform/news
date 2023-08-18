<template>
  <v-list-item class="clickable" :href="url">
    <v-list-item-icon class="me-3 my-auto">
      <v-avatar
        tile
        size="25">
        <img
          v-if="news.illustrationURL"
          :alt="activityTitle"
          :src="`${news.illustrationURL}&size=25x25`">
        <img
          v-else
          :alt="activityTitle"
          src="/news/images/news.png">
      </v-avatar>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">{{ activityTitle }}</v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <favorite-button
        :id="id"
        :favorite="isFavorite"
        :top="top"
        :right="right"
        type="news"
        type-label="News"
        @removed="removed"
        @remove-error="removeError" />
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    activityTitle: '',
    url: '',
    isFavorite: true
  }),
  created() {
    this.$newsServices.getNewsById(this.id, false)
      .then(news => {
        this.activityTitle = news.title;
        this.url = news.url;
        this.news = news;
      });
  },
  methods: {
    removed() {
      this.isFavorite = !this.isFavorite;
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('news.label')}));
      this.$emit('removed');
      this.$root.$emit('refresh-favorite-list');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('news.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('news-notification-alert', {
        activityId: this.id,
        message,
        type: type || 'success',
      });
    },
  }
};
</script>
