<template>
  <div>
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
        <div v-if="illustrationURL" class="illustration">
          <img
            :src="illustrationURL"
            class="newsDetailsImage illustrationPicture"
            alt="News">
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
          <div class="newsInformationBackground">
            <div :class="[showUpdateInfo ? 'news-update-details-header' : 'news-details-header']" class="news-header-content">
              <div :class="[ showUpdateInfo ? 'newsUpdateInfo' : '']">
                <div class="activityAvatar avatarCircle">
                  <a :href="authorProfileURL">
                    <img
                      :src="authorAvatarURL"
                      class="avatar"
                      role="presentation">
                  </a>
                </div>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsPosted">
                  <a :href="authorProfileURL" class="newsInformationValue newsAuthorName news-details-information"> {{ authorFullName }} </a>
                  <span v-if="!hiddenSpace" class="newsInformationLabel"> {{ $t('news.activity.in') }} </span>
                  <div v-if="!hiddenSpace" class="newsSpace">
                    <a :href="spaceUrl" class="newsInformationLabel news-details-information">{{ spaceDisplayName }}</a>
                  </div>
                  <template v-if="publicationDate">
                    -
                    <date-format
                      :value="publicationDate"
                      :format="dateFormat"
                      class="newsInformationValue newsPostedDate news-details-information" />
                  </template>
                  <span v-else-if="postedDate" class="newsInformationValue newsPostedDate news-details-information">- {{ postedDate }}</span>
                </div>
                <div class="newsUpdater">
                  <div v-if="publicationState !== 'staged' && showUpdateInfo">
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdated') }} </span>
                  </div>
                  <div v-else-if="publicationState === 'staged'">
                    <span class="newsInformationLabel">{{ $t('news.details.scheduled') }} </span>
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
                      <span class="newsInformationLabel"> {{ $t('news.activity.by') }} </span>
                      <a :href="updaterProfileURL" class="newsInformationValue newsUpdaterName">{{ updaterFullName }}</a>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="newsSummary"
            id="newsSummary"
            class="summary">
            <span v-html="newsSummary"></span>
          </div>

          <div
            id="newsBody"
            :class="[!summary ? 'fullDetailsBodyNoSummary' : '']"
            class="fullDetailsBody clearfix">
            <span v-sanitized-html="newsBody"></span>
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
  }),
  computed: {
    archivedNews() {
      return this.news && this.news.archived;
    },
    illustrationURL() {
      return this.news && this.news.illustrationURL;
    },
    newsTitle() {
      return this.news && this.news.title;
    },
    showUpdateInfo() {
      return this.news && this.news.updateDate && this.news.updater !=='__system' && this.news.updateDate !== 'null' && this.news.publicationDate && this.news.publicationDate !== 'null' && this.news.updateDate.time > this.news.publicationDate.time;
    },
    authorFullName() {
      return this.news && (this.news.authorFullName || this.news.authorDisplayName);
    },
    authorProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.author}`;
    },
    authorAvatarURL() {
      return this.news && (this.news.profileAvatarURL || this.news.authorAvatarUrl);
    },
    hiddenSpace() {
      return this.news && this.news.hiddenSpace;
    },
    newsBody() {
      return this.news && this.targetBlank(this.news.body);
    },
    updaterFullName() {
      return this.news && this.news.updaterFullName;
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.updater}`;
    },
    publicationDate() {
      return this.news && this.news.publicationDate && this.news.publicationDate.time && new Date(this.news.publicationDate.time);
    },
    updatedDate() {
      return this.news && this.news.updateDate && this.news.updateDate.time && new Date(this.news.updateDate.time);
    },
    newsSummary() {
      return this.news && this.targetBlank(this.news.summary);
    },
    spaceDisplayName() {
      return this.news && this.news.spaceDisplayName;
    },
    spaceUrl() {
      return this.news && this.news.spaceUrl;
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
    },
    targetBlank: function (content) {
      const internal = location.host + eXo.env.portal.context;
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      const links = docElement.getElementsByTagName('a');
      for (const link of links) {
        let href = link.href.replace(/(^\w+:|^)\/\//, '');
        if (href.endsWith('/')) {
          href = href.slice(0, -1);
        }
        if (href !== location.host && !href.startsWith(internal)) {
          link.setAttribute('target', '_blank');
          link.setAttribute('rel', 'noopener noreferrer');
        }
      }
      return docElement.innerHTML;
    },
  }
};
</script>