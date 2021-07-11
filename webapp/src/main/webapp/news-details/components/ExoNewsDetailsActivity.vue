<template>
  <exo-news-details
    v-if="news"
    :news="news"
    :news-id="newsId || sharedNewsId"
    :activity-id="activityId"
    :show-edit-button="showEditButton"
    :show-pin-button="showPinButton"
    :show-delete-button="showDeleteButton" />
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    news: null,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    sharedActivity() {
      return this.activity && this.activity.originalActivity;
    },
    sharedTemplateParams() {
      return this.sharedActivity && this.sharedActivity.templateParams;
    },
    templateParams() {
      return this.activity && this.activity.templateParams;
    },
    newsId() {
      return this.templateParams && this.templateParams.newsId;
    },
    sharedNewsId() {
      return this.sharedTemplateParams && this.sharedTemplateParams.newsId;
    },
    showDeleteButton() {
      return this.news && this.news.canDelete;
    },
    showEditButton() {
      return this.news && this.news.canEdit;
    },
    showPinButton() {
      return this.news && this.news.canPublish;
    },
  },
  created() {
    if (this.newsId || this.sharedNewsId) {
      this.retrieveNews();
    }
  },
  methods: {
    retrieveNews() {
      this.$newsServices.getNewsByActivityId(this.activityId)
        .then(news => {
          this.news = news;
          if (!this.news) {
            this.$root.$emit('activity-extension-abort', this.activityId);
          }
          this.activity.news = news;
        })
        .catch(() => {
          this.$root.$emit('activity-extension-abort', this.activityId);
        });
    },
  },
};
</script>