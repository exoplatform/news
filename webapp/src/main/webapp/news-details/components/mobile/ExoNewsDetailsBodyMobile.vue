<template>
  <div class="newsContent">
    <div :title="newsTitle" class="newsDetailsTitle mt-2 pl-2 pr-2 ms-2 font-weight-bold subtitle-1">
      {{ newsTitle }}
    </div>
    <div class="d-flex flex-row pa-2 ms-2">
      <div v-if="news" class="flex-column newsAuthor">
        <exo-user-avatar
          :profile-id="newsAuthor"
          :size="32"
          class="align-center my-auto text-truncate flex-grow-0 flex"
          bold-title
          link-style />
      </div>
      <v-icon>
        mdi-chevron-right
      </v-icon>
      <div class="flex-column">
      </div>
      <v-list-item-title v-if="space" class="font-weight-bold body-2 mb-0">
        <exo-space-avatar
          :space="space"
          :size="32"
          class="align-center my-auto text-truncate flex-grow-0 flex"
          bold-title
          link-style />
      </v-list-item-title>
    </div>
    <div class="d-flex flex-row caption text-light-color">
      <exo-news-details-time :news="news" />
    </div>
    <div class="d-flex flex-row ms-2 me-2 caption font-italic grey--text text-darken-1 pa-4">
      <span v-sanitized-html="newsSummary"></span>
    </div>
    <v-divider class="mx-4" />
    <div class="d-flex flex-column pa-4 ms-2 me-2 newsBody">
      <div
        class="rich-editor-content extended-rich-content"
        v-html="newsBody"></div>
    </div>
    <div v-show="attachments && attachments.length" class="d-flex flex-row pa-4 newsAttachmentsTitle subtitle-2">
      {{ $t('news.details.attachments.title') }} ({{ attachments ? attachments.length : 0 }})
    </div>
    <div v-show="attachments && attachments.length" class="newsAttachments">
      <div
        v-for="attachedFile in attachments"
        :key="attachedFile.id"
        class="newsAttachment text-truncate"
        @click="openPreview(attachedFile)">
        <exo-attachment-item :file="attachedFile" />
      </div>
    </div>
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
    newsId: {
      type: String,
      required: false,
      default: null
    },
    space: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    dateTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  computed: {
    newsTitle() {
      return this.news && this.news.title;
    },
    spaceUrl() {
      return this.news && this.news.spaceAvatarUrl;
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    newsSummary() {
      return this.news?.summary;
    },
    newsBody() {
      return this.news?.body;
    },
    newsAuthor() {
      return this.news && this.news.author;
    },
    attachments() {
      return this.news && this.news.attachments;
    },
  },
  methods: {
    openPreview(attachedFile) {
      const self = this;
      window.require(['SHARED/documentPreview'], function(documentPreview) {
        documentPreview.init({
          doc: {
            id: attachedFile.id,
            repository: 'repository',
            workspace: 'collaboration',
            title: attachedFile.name,
            downloadUrl: `/portal/rest/v1/news/attachments/${attachedFile.id}/file`,
            openUrl: `/portal/rest/v1/news/attachments/${attachedFile.id}/open`
          },
          showComments: false
        });
        self.hideDocPreviewComments();
      });
    },
  }
};
</script>