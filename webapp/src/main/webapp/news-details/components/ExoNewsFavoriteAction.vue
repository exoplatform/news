<template>
  <favorite-button
    :id="activityId"
    :favorite="isFavorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    type="activity"
    @removed="removed"
    @remove-error="removeError"
    @added="added"
    @add-error="addError" />
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      default: null,
    },
    activityId: {
      type: Number,
      default: () => 0,
    },
    absolute: {
      type: Boolean,
      default: false,
    },
    top: {
      type: Number,
      default: () => 0,
    },
    right: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    isFavorite: false,
  }),
  created() {
    this.$activityService.getActivityById(this.activityId)
      .then(fullActivity => {
        this.isFavorite = fullActivity && fullActivity.metadatas && fullActivity.metadatas.favorites && fullActivity.metadatas.favorites.length;
      });
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('news.label')}));
      this.$favoriteService.removeFavorite('news', this.news.id)
        .then(() => {
          this.isFavorite = false;
        })
        .catch(() => this.$emit('remove-error'));
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('news.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('news.label')}));
      this.$favoriteService.addFavorite('news', this.news.id)
        .then(() => {
          this.isFavorite = true;
        })
        .catch(() => this.$emit('add-error'));

    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('news.label')}), 'error');
    },
    displayAlert(message, type) {
      this.$root.$emit('news-notification-alert', {
        activityId: this.activityId,
        message,
        type: type || 'success',
      });
    },
  },
};
</script>