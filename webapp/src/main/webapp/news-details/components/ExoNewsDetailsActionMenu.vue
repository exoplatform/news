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
        <v-list-item v-if="showShareButton && news.activityId" @click="$root.$emit('activity-share-drawer-open', news.activityId)">
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
        <v-list-item v-if="isMobile && showPublishButton" @click="confirmAction">
          <v-list-item-title>
            {{ publishLabel }}
          </v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>
    <exo-confirm-dialog
      ref="publishConfirmDialog"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      :ok-label="$t('news.publish.btn.confirm')"
      :cancel-label="$t('news.publish.btn.cancel')"
      @ok="updatePublishedField" />
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
    publishSuccess: true,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    confirmDialogTitle() {
      return this.newsPublished && this.$t('news.unpublish.action') || this.$t('news.publish.action');
    },
    confirmDialogMessage() {
      return this.newsPublished && this.$t('news.unpublish.confirm', {0: this.news.title}) || this.$t('news.publish.confirm');
    },
    publishLabel() {
      return this.newsPublished ? this.$t('news.details.header.menu.unpublish'): this.$t('news.details.header.menu.publish');
    },
  },
  mounted() {
    $('#UIPortalApplication').parent().click(() => {
      this.actionMenu = false;
    });
  },
  methods: {
    confirmAction() {
      this.$refs.publishConfirmDialog.open();
    },
    updatePublishedField() {
      const publishMessageTime = 5000;
      const context = this;
      let updatedNews = null;
      if (this.newsPublished === false) {
        updatedNews = {
          pinned: true,
        };
      } else {
        updatedNews = {
          pinned: false,
        };
      }
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${this.news.newsId}`,{
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PATCH',
        body: JSON.stringify(updatedNews)
      }).then (function() {
        context.showPublishMessage = true;
        if (context.newsPublished === false) {
          context.publishMessage = context.$t('news.publish.success');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPublished = true;
        } else {
          context.publishMessage = context.$t('news.unpublish.success');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPublished = false;
        }
        setTimeout(function () {
          context.showPublishMessage = false;
        }, publishMessageTime);
      })
        .catch (function() {
          context.showPublishMessage = true;
          context.publishSuccess = false;
          if (context.newsPublished === false) {
            context.publishMessage = context.$t('news.publish.error');
          } else {
            context.publishMessage = context.$t('news.unpublish.error');
          }
          setTimeout(function () {
            context.publishSuccess = true;
            context.showPublishMessage = false;
          }, publishMessageTime);
        });
    },
  }
};
</script>

