<template>
  <v-app-bar
    absolute
    :src="illustrationUrl"
    prominent>
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
      class="newsDetailsMenuBtn my-2"
      fab
      dark
      x-small>
      <exo-news-details-action-menu
        v-if="showEditButton"
        :news="news"
        :show-edit-button="showEditButton"
        :show-delete-button="showDeleteButton" />
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
    illustrationUrl: {
      type: String,
      required: false,
      default: null
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showPinButton: {
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
      return this.news && this.news.spaceMember ? this.news.spaceUrl : `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
    },
  }
};
</script>