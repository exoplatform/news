<template>
  <div id="archiveNews">
    <div
      v-if="showArchiveMessage"
      id="messageArchive"
      class="confirmPublishMessage">
      <transition name="fade">
        <div :class="[successArchive ? 'alert-success' : 'alert-error']" class="alert text-center">
          <i :class="[successArchive ? 'uiIconSuccess' : 'uiIconError']"></i>
          <span>{{ messageArchive }}</span>
        </div>
      </transition>
    </div>
    <a
      :title="newsArchiveLabel"
      class="btn newsArchiveButton"
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
    published: {
      type: Boolean,
      required: true,
      default: false
    }
  },
  data() {
    return {
      showArchiveMessage: false,
      messageArchive: '',
      successArchive: true,
      archiveLabel: '',
      newsArchiveLabel: '',
    };
  },
  created() {
    if (!this.newsArchived) {
      this.newsArchiveLabel = this.$t('news.details.archive.action');
      this.archiveLabel = this.$t('news.archive.action');
    } else {
      this.newsArchiveLabel = this.$t('news.details.unarchive.action');
      this.archiveLabel = this.$t('news.unarchive.action');
    }
  },
  methods: {
    confirmAction() {
      let confirmText = this.$t('news.archive.confirm');
      let captionText = this.$t('news.archive.action');
      const confirmButton = this.$t('news.archive.btn.confirm');
      const cancelButton = this.$t('news.archive.btn.cancel');
      if (this.newsArchived === true) {
        confirmText = this.$t('news.unarchive.confirm').replace('{0}', this.newsTitle);
        captionText = this.$t('news.unarchive.action');
      }
      eXo.social.PopupConfirmation.confirm('newsArchiveButton', [{action: this.updateArchivedField(), label: confirmButton}], captionText, confirmText, cancelButton);
    },
    updateArchivedField() {
      const publishMessageTime = 5000;
      const context = this;
      let updatedNews = null;
      if (this.newsArchived === false) {
        updatedNews = {
          archived: true,
          published: this.published,
        };
      } else {
        updatedNews = {
          archived: false,
          published: this.published,
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
        if (context.newsArchived === true) {
          context.$emit('update-archived-field');
        }
        context.showArchiveMessage = true;
        if (context.newsArchived === false) {
          context.messageArchive = context.$t('news.archive.success');
          context.archiveLabel = context.$t('news.unarchive.action');
          context.newsArchiveLabel = context.$t('news.details.unarchive.action');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsArchived = true;
        } else {
          context.messageArchive = context.$t('news.unarchive.success');
          context.archiveLabel = context.$t('news.archive.action');
          context.newsArchiveLabel = context.$t('news.details.archive.action');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsArchived = false;
        }
        setTimeout(function () {
          context.showArchiveMessage = false;
        }, publishMessageTime);
      })
        .catch (function() {
          context.showArchiveMessage = true;
          context.successArchive = false;
          if (context.newsArchived === false) {
            context.messageArchive = context.$t('news.archive.error');
          } else {
            context.messageArchive = context.$t('news.unarchive.error');
          }
          setTimeout(function () {
            context.successArchive = true;
            context.showArchiveMessage = false;
          }, publishMessageTime);
        });
    },
  }
};
</script>
