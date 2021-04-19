<template>
  <div id="newsShareActivity">
    <transition name="fade">
      <div v-show="showMessage" :class="'alert-' + messageType" class="alert">
        <i :class="messageIconClass"></i> <span v-html="message"></span>
      </div>
    </transition>

    <a id="newsShareButton" :data-original-title="$t('news.share.share')" :class="[newsArchived ? 'unauthorizedPin' : '']" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="$root.$emit('news-share-drawer-open',news)">
      <i :class="[newsArchived ? 'disabledIcon' : '']" class="uiIconShare"></i>
    </a>
  </div>
</template>

<script>

export default {
  props: {
    news: {
      type: Object,
      required: true,
      default: function() { return new Object(); }
    }
  },
  data() {
    return {
      message: '',
      showMessage: false,
      messageType: 'success',
      messageDisplayTime: 5000,
    };
  },
  computed:{
    messageIconClass: function() {
      return `uiIcon${this.messageType.charAt(0).toUpperCase()}${this.messageType.slice(1)}`;
    }
  },
  watch: {
    showMessage(newValue) {
      if(newValue) {
        setTimeout(() => {this.showMessage = false;}, this.messageDisplayTime);
      }
    }
  },
  created() {
    this.$root.$on('display-share-alert', shareNews => {
      this.showMessage = shareNews;
    });
  }
};
</script>