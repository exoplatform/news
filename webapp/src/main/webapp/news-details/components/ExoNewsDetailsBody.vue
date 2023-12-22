<template>
  <div v-if="news">
    <div v-if="archivedNews && !news.canArchive">
      <div class="userNotAuthorized">
        <div class="notAuthorizedIconDiv">
          <img src="/news/images/notauthorized.png" class="iconNotAuthorized">
        </div>
        <h3>{{ $t('news.archive.text') }}</h3>
      </div>
    </div>
    <div class="newsDetails-description">
      <div :class="[illustrationURL ? 'newsDetails-header' : '']" class="newsDetails-header">
        <div v-if="illustrationURL" class="illustration center">
          <img
            :src="illustrationURL.concat('&size=0x400').toString()"
            class="newsDetailsImage illustrationPicture"
            :alt="newsTitle"
            longdesc="#newsSummary">
        </div>
        <div class="newsDetails">
          <div class="news-top-information d-flex">
            <div id="titleNews" class="newsTitle newsTitleMobile">
              <a class="activityLinkColor newsTitleLink">{{ newsTitle }}</a>
              <div v-if="archivedNews" class="newsArchived">
                <span class="newsArchiveLabel"> ( {{ $t('news.archive.label') }} ) </span>
              </div>
            </div>
          </div>
          <div class="newsInformationBackground center">
            <div :class="[showUpdateInfo ? 'news-update-details-header' : 'news-details-header']" class="news-header-content  d-inline-flex align-center">
              <div :class="[ showUpdateInfo ? 'newsUpdateInfo' : '']">
                <exo-user-avatar 
                  :profile-id="authorProfile"
                  :size="50"
                  class="me-1"
                  popover
                  avatar />
              </div>
              <div id="informationNews" class="newsInformation pa-1">
                <div class="newsPosted d-flex align-center">
                  <exo-user-avatar
                    :profile-id="authorProfile"
                    extra-class="me-1"
                    fullname
                    small-font-size
                    link-style
                    popover />
                  <span v-if="!hiddenSpace" class="text-light-color caption"> {{ $t('news.activity.in') }} </span>
                  <exo-space-avatar
                    v-if="!hiddenSpace"
                    :space-id="spaceId"
                    fullname
                    extra-class="mx-1"
                    small-font-size
                    link-style
                    popover />
                  <template v-if="publicationDate">
                    -
                    <date-format
                      :value="publicationDate"
                      :format="dateFormat"
                      class="newsInformationValue newsPostedDate news-details-information caption ms-1" />
                  </template>
                  <extension-registry-component
                    v-if="translateExtension"
                    :component="translateExtension"
                    :params="params"
                    element="div" />
                  <span v-else-if="postedDate" class="newsInformationValue newsPostedDate news-details-information">- {{ postedDate }}</span>
                </div>
                <div class="newsUpdater caption">
                  <div v-if="publicationState !== 'staged' && showUpdateInfo">
                    <span class="text-light-color">{{ $t('news.activity.lastUpdated') }} </span>
                  </div>
                  <div v-else-if="publicationState === 'staged'">
                    <span class="text-light-color">{{ $t('news.details.scheduled') }} </span>
                  </div>
                  <div>
                    <template v-if="publicationState !== 'staged' && updatedDate && showUpdateInfo">
                      <date-format
                        :value="updatedDate"
                        :format="dateFormat"
                        class="newsInformationValue newsUpdatedDate" />
                    </template>
                    <template v-else-if="publicationState === 'staged'">
                      <date-format
                        :value="scheduleDate"
                        :format="dateFormat"
                        class="newsInformationValue newsUpdatedDate" />
                      <span class="newsInformationValue">-</span>
                      <date-format
                        :value="scheduleDate"
                        :format="dateTimeFormat"
                        class="newsInformationValue newsUpdatedDate ml-1 me-1" />
                    </template>
                    <div v-if="notSameUpdater && showUpdateInfo">
                      <span class="text-light-color"> {{ $t('news.activity.by') }} </span>
                      <exo-user-avatar
                        :profile-id="newsUpdater"
                        extra-class="ms-1"
                        fullname
                        small-font-size
                        link-style
                        popover />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="newsSummary"
            id="newsSummary"
            class="summary center ms-13 me-13">
            <span v-sanitized-html="newsSummary"></span>
          </div>

          <div
            id="newsBody"
            :class="[!summary ? 'fullDetailsBodyNoSummary' : '']"
            class="fullDetailsBody ms-13 me-13 clearfix">
            <div
              class="rich-editor-content extended-rich-content"
              v-html="newsBody"></div>
          </div>

          <div v-show="attachments && attachments.length" class="newsAttachmentsTitle">
            {{ $t('news.details.attachments.title') }} ({{ attachments ? attachments.length : 0 }})
          </div>

          <div v-show="attachments && attachments.length" class="newsAttachments">
            <div
              v-for="attachedFile in attachments"
              :key="attachedFile.id"
              class="newsAttachment"
              @click="openPreview(attachedFile)">
              <exo-attachment-item :file="attachedFile" />
            </div>
          </div>
        </div>
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
    translateExtension: null,
    newsTitleContent: null,
    newsSummaryContent: null,
    newsBodyContent: null
  }),
  created() {
    this.setNewsTitle(this.news?.title);
    this.setNewsSummary(this.news?.summary);
    this.setNewsContent(this.news?.body);
    this.refreshTranslationExtensions();
    document.addEventListener('automatic-translation-extensions-updated', () => {
      this.refreshTranslationExtensions();
    });
    this.$root.$on('update-news-title', this.setNewsTitle);
    this.$root.$on('update-news-summary', this.setNewsSummary);
    this.$root.$on('update-news-body', this.setNewsContent);
  },
  computed: {
    params() {
      return {
        news: this.news,
      };
    },
    archivedNews() {
      return this.news && this.news.archived;
    },
    illustrationURL() {
      return this.news && this.news.illustrationURL;
    },
    newsTitle() {
      return this.news && this.newsTitleContent;
    },
    showUpdateInfo() {
      return this.news && this.news.updateDate && this.news.updater !=='__system' && this.news.updateDate !== 'null' && this.news.publicationDate && this.news.publicationDate !== 'null' && this.news.updateDate.time > this.news.publicationDate.time;
    },
    authorProfile() {
      return this.news && this.news.author;
    },
    hiddenSpace() {
      return this.news && this.news.hiddenSpace;
    },
    newsBody() {
      return this.news && this.newsBodyContent;
    },
    updaterFullName() {
      return this.news && this.news.updaterFullName;
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.news.updater}`;
    },
    newsUpdater() {
      return this.news && this.news.updater;
    },
    publicationDate() {
      return this.news && this.news.publicationDate && this.news.publicationDate.time && new Date(this.news.publicationDate.time);
    },
    updatedDate() {
      return this.news && this.news.updateDate && this.news.updateDate.time && new Date(this.news.updateDate.time);
    },
    newsSummary() {
      return this.news && this.newsSummaryContent;
    },
    spaceId() {
      return this.news && this.news.spaceId;
    },
    postedDate() {
      return this.news && this.news.postedDate;
    },
    summary() {
      return this.news && this.news.summary;
    },
    attachments() {
      return this.news && this.news.attachments;
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    notSameUpdater() {
      return this.news && this.news.updater !== this.news.author;
    },
    scheduleDate() {
      return this.news && this.news.schedulePostDate;
    },
  },
  methods: {
    setNewsTitle(title) {
      this.newsTitleContent = title;
    },
    setNewsSummary(content) {
      this.newsSummaryContent = content;
    },
    setNewsContent(translation) {
      this.newsBodyContent = translation;
    },
    refreshTranslationExtensions() {
      this.translateExtension = extensionRegistry.loadExtensions('news', 'translation-menu-extension')[0];
    },
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
    /**
     * Hack to hide the document preview comments panel because the document preview component
     * does not allow to hide it through its API
     * @returns {void} when the comments panel appeared and has been hidden
     */
    hideDocPreviewComments() {
      const intervalCheck = 100;

      const commentsPanel = document.querySelector('.uiDocumentPreview .commentArea');
      const collapsedCommentsButton = document.querySelector('.uiDocumentPreview .resizeButton');
      if (commentsPanel != null && collapsedCommentsButton != null) {
        commentsPanel.style.display = 'none';
        collapsedCommentsButton.style.display = 'none';
        document.querySelector('.uiDocumentPreview').classList += ' collapsed';
      } else {
        setTimeout(this.hideDocPreviewComments, intervalCheck);
      }
    }
  }
};
</script>