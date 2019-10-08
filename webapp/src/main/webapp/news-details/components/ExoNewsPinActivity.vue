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
    <a id="newsPinButton" :data-original-title="$t('news.pin.action')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="pinNews">
      <i class="uiIconPin"> </i>
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
  },
  data() {
    return {
      showPinMessage : false,
      messagePin : '',
      successPin: true,
    };
  },
  methods: {
    pinNews : function() {
      const confirmText = this.$t('news.pin.confirm');
      const captionText = this.$t('news.pin.action');
      const confirmButton = this.$t('news.pin.btn.confirm');
      const cancelButton = this.$t('news.pin.btn.cancel');
      eXo.social.PopupConfirmation.confirm('newsPinButtonFromDetailsForm', [{action: this.pinActivity, label : confirmButton}], captionText, confirmText, cancelButton);
    },
    pinActivity : function () {
      const pinMessageTime = 5000;
      const context = this;
      const updatedNews = {
        pinned: true,
      };
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${this.newsId}`,{
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PATCH',
        body: JSON.stringify(updatedNews)
      }).then (function() {
        context.showPinMessage = true;
        context.messagePin = context.$t('news.pin.success');
        setTimeout(function () {
          context.showPinMessage = false;
        }, pinMessageTime);
      })
        .catch (function() {
          context.showPinMessage = true;
          context.successPin = false;
          context.messagePin = context.$t('news.pin.error');
          setTimeout(function () {
            context.successPin = true;
            context.showPinMessage = false;
          }, pinMessageTime);
        });
    },
  }
};
</script>
