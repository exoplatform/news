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
      :activity-id="news.activityId"
      :show-edit-button="showEditButton"
      :show-publish-button="showPublishButton"
      :show-delete-button="showDeleteButton" />
  </div>
</template>

<script>
const UNAUTHORIZED_CODE = 401;
export default {
  props: {
    newsId: {
      type: String,
      default: ''
    },
    newsType: {
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
    this.$newsServices.getNewsById(this.newsId, false, this.newsType)
      .then(news => {
        if (news !== null && news !== UNAUTHORIZED_CODE) {
          this.news = news;
          this.showEditButton = this.news.canEdit;
          this.showPublishButton = this.news.canPublish;
          this.showDeleteButton = this.news.canDelete;
          if (!this.news.spaceMember) {
            this.$root.$emit('restricted-space', this.news.spaceDisplayName, this.news.hiddenSpace);
            const message = this.news.hiddenSpace ? this.$t('news.activity.notAuthorizedUserForSpaceHidden'): this.$t('news.activity.notAuthorizedUser').replace('{0}',  this.news.spaceDisplayName);
            this.$root.$emit('alert-message', message, 'warning');
          }
        } else {
          this.notFound = true;
        }
      });
  },
};
</script>