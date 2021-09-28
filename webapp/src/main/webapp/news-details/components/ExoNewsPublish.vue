<template>
  <div id="publishNewsActivity" class="newsDetailsIcons">
    <exo-confirm-dialog
      ref="publishConfirmDialog"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      :ok-label="$t('news.publish.btn.confirm')"
      :cancel-label="$t('news.publish.btn.cancel')"
      @ok="updatePublishedField" />
    <a
      id="newsPublishButton"
      :title="publishLabel"
      :class="[newsArchived ? 'unauthorizedPublish' : '']"
      class="btn my-5"
      @click="confirmAction">
      <v-icon
        :class="publishArticleClass"
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
      showPublishMessage: false,
      messagePublish: '',
      successPublish: true,
    };
  },
  computed: {
    publishArticleClass() {
      return this.newsPublished ? 'publishArticle' : 'unpublishArticle';
    },
    publishLabel() {
      return this.newsPublished && this.$t('news.unpublish.action') || this.$t('news.publish.action');
    },
    confirmDialogTitle() {
      return this.newsPublished && this.$t('news.unpublish.action') || this.$t('news.publish.action');
    },
    confirmDialogMessage() {
      return this.newsPublished && this.$t('news.unpublish.confirm', {0: this.newsTitle}) || this.$t('news.publish.confirm');
    },
  },
  methods: {
    confirmAction() {
      this.$refs.publishConfirmDialog.open();
    },
    updatePublishedField: function () {
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
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${this.newsId}`,{
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PATCH',
        body: JSON.stringify(updatedNews)
      }).then (function() {
        context.showPublishMessage = true;
        if (context.newsPublished === false) {
          context.messagePublish = context.$t('news.publish.success');
          context.publishLabel = context.$t('news.unpublish.action');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPublished = true;
        } else {
          context.messagePublish = context.$t('news.unpublish.success');
          context.publishLabel = context.$t('news.publish.action');
          // eslint-disable-next-line vue/no-mutating-props
          context.newsPublished = false;
        }
        setTimeout(function () {
          context.showPublishMessage = false;
        }, publishMessageTime);
      })
        .catch (function() {
          context.showPublishMessage = true;
          context.successPublish = false;
          if (context.newsPublished === false) {
            context.messagePublish = context.$t('news.publish.error');
          } else {
            context.messagePublish = context.$t('news.unpublish.error');
          }
          setTimeout(function () {
            context.successPublish = true;
            context.showPublishMessage = false;
          }, publishMessageTime);
        });
    },
  }
};
</script>