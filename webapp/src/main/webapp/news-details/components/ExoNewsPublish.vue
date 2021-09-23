<template>
  <div id="publishNewsActivity" class="newsDetailsIcons">
    <exo-confirm-dialog
      ref="publishConfirmDialog"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      :ok-label="$t('news.broadcast.btn.confirm')"
      :cancel-label="$t('news.broadcast.btn.cancel')"
      @ok="updatePinnedField" />
    <a
      id="newsPinButton"
      :title="pinLabel"
      :class="[newsArchived ? 'unauthorizedPin' : '']"
      class="btn my-5"
      @click="confirmAction">
      <v-icon
        :class="broadcastArticleClass"
        class="fas fa-bullhorn" />
    </a>
  </div>
</template>

<script>
export default {
  props: {
    newsId: {
      type: String,
      required: false,
      default: null
    },
    newsTitle: {
      type: String,
      required: true,
      default: null
    },
    newsPublished: {
      type: Boolean,
      required: true,
      default: false
    },
    newsArchived: {
      type: Boolean,
      required: true,
      default: false
    },
  },
  data() {
    return {
      showPinMessage: false,
      messagePin: '',
      successPin: true,
    };
  },
  computed: {
    broadcastArticleClass() {
      return this.newsPinned ? 'broadcastArticle' : 'unbroadcastArticle';
    },
    pinLabel() {
      return this.newsPinned && this.$t('news.unbroadcast.action') || this.$t('news.broadcast.action');
    },
    confirmDialogTitle() {
      return this.newsPinned && this.$t('news.unbroadcast.action') || this.$t('news.broadcast.action');
    },
    confirmDialogMessage() {
      return this.newsPinned && this.$t('news.unbroadcast.confirm', {0: this.newsTitle}) || this.$t('news.broadcast.confirm');
    },
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
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${this.newsId}`,{
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
          context.pinLabel = context.$t('news.unbroadcast.action');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPinned = true;
        } else {
          context.messagePin = context.$t('news.unbroadcast.success');
          context.pinLabel = context.$t('news.broadcast.action');
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