<template>
  <favorite-button
    :id="newsId"
    :favorite="news && news.favorite"
    :absolute="absolute"
    :top="top"
    :right="right"
    :template-params="templateParams"
    :space-id="spaceId"
    type="news"
    type-label="News"
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
    templateParams: {},
  }),
  computed: {
    newsId() {
      return this.news?.id;
    },
    spaceId() {
      return this.news?.spaceId;
    },
  },
  created() {
    this.templateParams.newsId = this.news && this.news.id;
    this.templateParams.spaceId = this.news && this.news.spaceId;
  },
  methods: {
    removed() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyDeletedFavorite', {0: this.$t('news.label')}));
      this.$emit('removed');
    },
    removeError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorDeletingFavorite', {0: this.$t('news.label')}), 'error');
    },
    added() {
      this.displayAlert(this.$t('Favorite.tooltip.SuccessfullyAddedAsFavorite', {0: this.$t('news.label')}));
      this.$emit('added');
    },
    addError() {
      this.displayAlert(this.$t('Favorite.tooltip.ErrorAddingAsFavorite', {0: this.$t('news.label')}), 'error');
    },
    displayAlert(message, type) {
      document.dispatchEvent(new CustomEvent('notification-alert', {detail: {
        message,
        type: type || 'success',
      }}));
    },
  },
};
</script>
