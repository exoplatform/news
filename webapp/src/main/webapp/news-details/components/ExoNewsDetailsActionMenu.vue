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
        <v-list-item v-if="isMobile && showPinButton" @click="confirmAction">
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
      :ok-label="$t('news.broadcast.btn.confirm')"
      :cancel-label="$t('news.broadcast.btn.cancel')"
      @ok="updatePinnedField" />
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
    newsPinned: {
      type: Boolean,
      required: true,
      default: false
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
    showPinButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data: () => ({
    actionMenu: null,
    messagePin: '',
    successPin: true,
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    confirmDialogTitle() {
      return this.newsPinned && this.$t('news.unbroadcast.action') || this.$t('news.broadcast.action');
    },
    confirmDialogMessage() {
      return this.newsPinned && this.$t('news.unbroadcast.confirm', {0: this.news.title}) || this.$t('news.broadcast.confirm');
    },
    publishLabel() {
      return this.newsPinned ? this.$t('news.details.header.menu.unpublish'): this.$t('news.details.header.menu.publish');
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
    updatePinnedField: function () {
      const pinMessageTime = 5000;
      const context = this;
      let updatedNews = null;
      if (this.newsPinned === false) {
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
        context.showPinMessage = true;
        if (context.newsPinned === false) {
          context.messagePin = context.$t('news.broadcast.success');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPinned = true;
        } else {
          context.messagePin = context.$t('news.unbroadcast.success');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPinned = false;
        }
        setTimeout(function () {
          context.showPinMessage = false;
        }, pinMessageTime);
      })
        .catch (function() {
          context.showPinMessage = true;
          context.successPin = false;
          if (context.newsPinned === false) {
            context.messagePin = context.$t('news.broadcast.error');
          } else {
            context.messagePin = context.$t('news.unbroadcast.error');
          }
          setTimeout(function () {
            context.successPin = true;
            context.showPinMessage = false;
          }, pinMessageTime);
        });
    },
  }
};
</script>

