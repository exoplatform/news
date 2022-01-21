<template>
  <div id="newsDetails">
    <div v-if="notFound" class="articleNotFound">
      <i class="iconNotFound"></i>
      <h3>{{ $t('news.details.restricted') }}</h3>
    </div>
    <exo-news-details
      v-else
      id="newsFullDetails"
      :news="news"
      :news-id="newsId"
      :show-edit-button="showEditButton"
      :show-publish-button="showPublishButton"
      :show-delete-button="showDeleteButton" />
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
    showEditButton: false,
    showPublishButton: false,
    showDeleteButton: false,
  }),
  created() {
    this.$newsServices.getNewsById(this.newsId, false)
      .then(news => {
        if (news !== null) {
          this.news = news;
          this.showEditButton = this.news.canEdit;
          this.showPublishButton = this.news.canPublish;
          this.showDeleteButton = this.news.canDelete;
          if (!this.news.spaceMember) {
            this.$root.$emit('restricted-space', this.news.spaceDisplayName, this.news.hiddenSpace);
          }
        } else {
          this.notFound = true;
        }
      });
  },
};
</script>