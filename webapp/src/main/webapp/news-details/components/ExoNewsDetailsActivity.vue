<template>
  <exo-news-details
    v-if="news"
    :news="news"
    :news-id="newsId"
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
    templateParams() {
      return this.activity && this.activity.templateParams;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    newsId() {
      return this.templateParams && this.templateParams.newsId;
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
    if (this.newsId) {
      this.retrieveNews();
    }
  },
  methods: {
    retrieveNews() {
      this.$newsServices.getNewsById(this.newsId)
        .then(news => this.news = news);
    },
  },
};
</script>