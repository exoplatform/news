<template>
  <div id="archiveNews">
    <div v-if="showArchiveMessage" id="messageArchive" class="confirmPinMessage">
      <transition name="fade">
        <div :class="[successArchive ? 'alert-success' : 'alert-error']" class="alert">
          <i :class="[successArchive ? 'uiIconSuccess' : 'uiIconError']" ></i>
          <span>{{ messageArchive }}</span>
        </div>
      </transition>
    </div>
    <a :data-original-title="archiveLabel" class="btn newsArchiveButton"
       rel="tooltip"
       data-placement="bottom"
       @click="confirmAction">
      <i :class="[newsArchived ? 'uiIconArchived' : 'uiIconUnarchived']"> </i>
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
    newsArchived: {
      type: Boolean,
      required: true,
      default: false
    },
    pinned: {
      type: Boolean,
      required: true,
      default: false
    }
  },
  data() {
    return {
      showArchiveMessage : false,
      messageArchive : '',
      successArchive: true,
      archiveLabel: '',
    };
  },
  created() {
    if(!this.newsArchived) {
      this.archiveLabel = this.$t('news.archive.action');
    } else {
      this.archiveLabel = this.$t('news.unarchive.action');
    }
  },
  methods: {
    confirmAction : function() {
      let confirmText = this.$t('news.archive.confirm');
      let captionText = this.$t('news.archive.action');
      const confirmButton = this.$t('news.archive.btn.confirm');
      const cancelButton = this.$t('news.archive.btn.cancel');
      if(this.newsArchived === true) {
        confirmText = this.$t('news.unarchive.confirm').replace('{0}', this.newsTitle);
        captionText = this.$t('news.unarchive.action');
      }
      eXo.social.PopupConfirmation.confirm('newsArchiveButton', [{action: this.updateArchivedField, label : confirmButton}], captionText, confirmText, cancelButton);
    },
    updateArchivedField : function () {
      const pinMessageTime = 5000;
      const context = this;
      let updatedNews = null;
      if(this.newsArchived === false) {
        updatedNews = {
          archived: true,
          pinned: this.pinned,
        };
      } else {
        updatedNews = {
          archived: false,
          pinned: this.pinned,
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
        context.$emit('refresh-news-list');
        if(context.newsArchived === true) {
          context.$emit('update-archived-field');
        }
        context.showArchiveMessage = true;
        if(context.newsArchived === false) {
          context.messageArchive = context.$t('news.archive.success');
          context.archiveLabel = context.$t('news.unarchive.action');
          context.newsArchived = true;
        } else {
          context.messageArchive = context.$t('news.unarchive.success');
          context.archiveLabel = context.$t('news.archive.action');
          context.newsArchived = false;
        }
        setTimeout(function () {
          context.showArchiveMessage = false;
        }, pinMessageTime);
      })
        .catch (function() {
          context.showArchiveMessage = true;
          context.successArchive = false;
          if(context.newsArchived === false) {
            context.messageArchive = context.$t('news.archive.error');
          } else {
            context.messageArchive = context.$t('news.unarchive.error');
          }
          setTimeout(function () {
            context.successArchive = true;
            context.showArchiveMessage = false;
          }, pinMessageTime);
        });
    },
  }
};
</script>
