<template>
  <div id="pinNewsActivity">
    <div v-if="showPinMessage" id="messagePin" class="confirmPinMessage">
      <transition name="fade">
        <div :class="[successPin ? 'alert-success' : 'alert-error']" class="alert">
          <i :class="[successPin ? 'uiIconSuccess' : 'uiIconError']" ></i>
          <span>{{ messagePin }}</span>
        </div>
      </transition>
    </div>
    <a id="newsPinButton" :data-original-title="pinLabel" :class="[newsArchived ? 'unauthorizedPin' : '']"
       class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="confirmAction">
      <i :class="[newsPinned ? '' : 'unpinned']" class="uiIconPin"> </i>
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
      showPinMessage : false,
      messagePin : '',
      pinLabel: '',
      successPin: true,
    };
  },
  created() {
    if(!this.newsPinned) {
      this.pinLabel = this.$t('news.pin.action');
    } else {
      this.pinLabel = this.$t('news.unpin.action');
    }
  },
  methods: {
    confirmAction : function() {
      if(!this.newsArchived) {
        let confirmText = this.$t('news.pin.confirm');
        let captionText = this.$t('news.pin.action');
        const confirmButton = this.$t('news.pin.btn.confirm');
        const cancelButton = this.$t('news.pin.btn.cancel');
        if(this.newsPinned === true) {
          confirmText = this.$t('news.unpin.confirm').replace('{0}', this.newsTitle);
          captionText = this.$t('news.unpin.action');
        }
        eXo.social.PopupConfirmation.confirm('newsPinButtonFromDetailsForm', [{action: this.updatePinnedField, label : confirmButton}], captionText, confirmText, cancelButton);
      }
    },
    updatePinnedField : function () {
      const pinMessageTime = 5000;
      const context = this;
      let updatedNews = null;
      if(this.newsPinned === false) {
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
        if(context.newsPinned === false) {
          context.messagePin = context.$t('news.pin.success');
          context.pinLabel = context.$t('news.unpin.action');
          context.newsPinned = true;
        } else {
          context.messagePin = context.$t('news.unpin.success');
          context.pinLabel = context.$t('news.pin.action');
          context.newsPinned = false;
        }
        setTimeout(function () {
          context.showPinMessage = false;
        }, pinMessageTime);
      })
        .catch (function() {
          context.showPinMessage = true;
          context.successPin = false;
          if(context.newsPinned === false) {
            context.messagePin = context.$t('news.pin.error');
          } else {
            context.messagePin = context.$t('news.unpin.error');
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
