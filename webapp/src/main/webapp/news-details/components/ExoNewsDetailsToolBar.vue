<template>
  <div :class="!showEditButton && 'me-5'" class="newsDetailsTopBar">
    <a class="backBtn" @click="goBack"><i class="uiIconBack my-4"></i></a>
    <v-btn
      v-if="publicationState === 'staged'"
      class="btn newsDetailsActionMenu mt-6 mr-2 pull-right"
      @click="$root.$emit('open-schedule-drawer','editScheduledNews')">
      {{ $t("news.composer.btn.scheduleArticle") }}
    </v-btn>
    <exo-news-details-action-menu
      v-if="showEditButton && publicationState !== 'staged'"
      class="pull-right"
      :news="news"
      :news-published="newsPublished"
      :show-edit-button="showEditButton"
      :show-delete-button="showDeleteButton"
      :show-publish-button="showPublishButton" />
    <exo-news-favorite-action
      v-if="displayFavoriteButton"
      :news="news"
      :activity-id="activityId"
      class="mt-6 pull-right" />
  </div>
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: function() { return new Object(); }
    },
    newsId: {
      type: String,
      required: false,
      default: null
    },
    activityId: {
      type: String,
      required: false,
      default: null
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showPublishButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data() {
    return {
      spaceId: null,
      updaterIdentity: null,
      BYTES_IN_MB: 1048576,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
      dateTimeFormat: {
        hour: '2-digit',
        minute: '2-digit',
      },
    };
  },
  computed: {
    historyClearedBackUrl() {
      return this.news && this.news.spaceMember ? this.news.spaceUrl : `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
    },
    archivedNews() {
      return this.news && this.news.archived;
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    newsTitle() {
      return this.news && this.news.title;
    },
    newsPublished() {
      return this.news && this.news.published;
    },
    lastVisitedPage(){
      return history && history.length && history.length > 2;
    },
    displayFavoriteButton() {
      return this.publicationState !== 'staged';
    }
  },
  methods: {
    goBack() {
      if (this.lastVisitedPage){
        history.back();
      }
      else {
        window.open(this.historyClearedBackUrl ,'_self');
      }
    },
  }
};
</script>