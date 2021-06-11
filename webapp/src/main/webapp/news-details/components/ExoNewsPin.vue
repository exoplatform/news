<template>
  <div id="pinNewsActivity">
    <div
      v-if="showPinMessage"
      id="messagePin"
      class="confirmPinMessage">
      <transition name="fade">
        <div :class="[successPin ? 'alert-success' : 'alert-error']" class="alert">
          <i :class="[successPin ? 'uiIconSuccess' : 'uiIconError']"></i>
          <span>{{ messagePin }}</span>
        </div>
      </transition>
    </div>
    <a
      id="newsPinButton"
      :data-original-title="pinLabel"
      :class="[newsArchived ? 'unauthorizedPin' : '']"
      class="btn"
      rel="tooltip"
      data-placement="bottom"
      @click="confirmAction">
      <v-icon
        dense
        :class="[newsPinned ? '' : 'unbroadcastArticle']"
        class="fas fa-bullhorn broadcastArticle" />
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
  },
  data() {
    return {
      showPinMessage: false,
      messagePin: '',
      pinLabel: '',
      successPin: true,
    };
  },
  created() {
    if (!this.newsPinned) {
      this.pinLabel = this.$t('news.broadcast.action');
    } else {
      this.pinLabel = this.$t('news.unbroadcast.action');
    }
  },
  methods: {
    confirmAction: function() {
      if (!this.newsArchived) {
        let confirmText = this.$t('news.broadcast.confirm');
        let captionText = this.$t('news.broadcast.action');
        const confirmButton = this.$t('news.broadcast.btn.confirm');
        const cancelButton = this.$t('news.broadcast.btn.cancel');
        if (this.newsPinned === true) {
          confirmText = this.$t('news.unbroadcast.confirm').replace('{0}', this.newsTitle);
          captionText = this.$t('news.unbroadcast.action');
        }
        eXo.social.PopupConfirmation.confirm('newsPinButtonFromDetailsForm', [{action: this.updatePinnedField, label: confirmButton}], captionText, confirmText, cancelButton);
      }
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