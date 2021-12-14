<template>
  <favorite-button
    :id="activityId"
    :favorite="isFavorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    :template-params="templateParams"
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
      type: String,
      default: null,
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
    templateParams: {},
  }),
  created() {
    this.$activityService.getActivityById(this.activityId)
      .then(fullActivity => {
        this.isFavorite = fullActivity && fullActivity.metadatas && fullActivity.metadatas.favorites && fullActivity.metadatas.favorites.length;
      });
    this.templateParams.newsId = this.news.id;
    this.templateParams.spaceId = this.news.spaceId;
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('news.label')}));
      this.$favoriteService.removeFavorite('news', this.news.id)
        .then(() => {
          this.isFavorite = false;
          this.$emit('removed');
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
          this.$emit('added');
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