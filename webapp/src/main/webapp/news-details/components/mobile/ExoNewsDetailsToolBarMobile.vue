<template>
  <v-app-bar
    absolute
    flat
    :src="illustrationUrl"
    prominent
    class="news-details-toolbar">
    <v-app-bar-nav-icon>
      <v-btn
        class="newsDetailsMenuBtn my-2"
        fab
        dark
        x-small
        :href="backURL">
        <v-btn icon>
          <v-icon>mdi-arrow-left</v-icon>
        </v-btn>
      </v-btn>
    </v-app-bar-nav-icon>
    <v-spacer />
    <v-btn
      v-if="publicationState !== 'staged' && showEditButton"
      class="newsDetailsMenuBtn my-2"
      fab
      dark
      x-small>
      <exo-news-details-action-menu
        v-if="showEditButton && publicationState !== 'staged'"
        :news="news"
        :show-edit-button="showEditButton"
        :show-delete-button="showDeleteButton"
        :show-publish-button="showPublishButton"
        :news-published="newsPublished" />
    </v-btn>
    <v-btn
      v-if="publicationState === 'staged'"
      class="btn newsDetailsActionMenu my-2 pull-right"
      @click="$root.$emit('open-schedule-drawer','editScheduledNews')">
      {{ $t("news.composer.btn.scheduleArticle") }}
    </v-btn>
  </v-app-bar>
</template>
<script>
export default {
  props: {
    news: {
      type: Object,
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
  computed: {
    backURL() {
      return this.news && this.news.spaceMember ? this.news.spaceUrl : `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}`;
    },
    illustrationUrl() {
      return this.news && this.news.illustrationURL ? this.news.illustrationURL.concat('&size=315x128').toString() : '/news/images/news.png';
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    newsPublished() {
      return this.news && this.news.published;
    }
  },
};
</script>
