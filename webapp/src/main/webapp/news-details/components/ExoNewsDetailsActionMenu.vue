<template>
  <div>
    <v-menu
      v-model="actionMenu"
      eager
      bottom
      left
      offset-y
      min-width="108px"
      class="px-0 text-right mx-2">
      <template #activator="{ on, attrs }">
        <v-btn
          v-bind="attrs"
          class="newsDetailsActionMenu pull-right"
          :aria-label="$t('news.details.menu.open')"
          icon
          v-on="on">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>

      <v-list>
        <v-list-item v-if="showEditButton" @click="$root.$emit('edit-news')">
          <v-list-item-title>
            {{ $t('news.details.header.menu.edit') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item v-if="showShareButton && news.activityId" @click="$root.$emit('activity-share-drawer-open', news.activityId, 'newsDetails')">
          <v-list-item-title>
            {{ $t('news.details.header.menu.share') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item v-if="showResumeButton" @click="$root.$emit('edit-news')">
          <v-list-item-title>
            {{ $t('news.details.header.menu.resume') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item v-if="showDeleteButton" @click="$root.$emit('delete-news')">
          <v-list-item-title>
            {{ $t('news.details.header.menu.delete') }}
          </v-list-item-title>
        </v-list-item>
        <v-list-item v-if="showPublishButton" @click="$root.$emit('open-edit-publishing-drawer')">
          <v-list-item-title>
            {{ $t('news.details.header.menu.publish') }}
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
  </div>
</template>

<script>

export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: null
    },
    newsArchived: {
      type: Boolean,
      required: true,
      default: false
    },
    showShareButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showResumeButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showPublishButton: {
      type: Boolean,
      required: false,
      default: false
    },
    newsPublished: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data: () => ({
    actionMenu: null,
    showPublishMessage: false,
    publishMessage: '',
    publishSuccess: true
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
  },
  mounted() {
    $('#UIPortalApplication').parent().click(() => {
      this.actionMenu = false;
    });
  },
};
</script>

