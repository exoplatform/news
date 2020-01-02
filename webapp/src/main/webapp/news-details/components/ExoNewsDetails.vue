<template>
  <div id="newsDetails">
    <div v-if="news.archived && !news.canArchive">
      <div class="userNotAuthorized">
        <div class="notAuthorizedIconDiv">
          <img src="/news/images/notauthorized.png" class="iconNotAuthorized">
        </div>
        <h3>{{ $t('news.archive.text') }}</h3>
      </div>
    </div>
    <div v-else class="newsDetails-description">
      <div class="newsDetails-header">
        <exo-news-share-activity v-if="showShareButton" :activity-id="activityId" :news-id="newsId" :news-title="news.title" :news-archived="news.archived"></exo-news-share-activity>
        <exo-news-activity-edit-composer v-if="showEditButton" :news-id="newsId" :activity-id="activityId"></exo-news-activity-edit-composer>
        <exo-news-pin-activity v-if="showPinInput" :news-id="newsId" :news-pinned="news.pinned" :news-title="news.title"></exo-news-pin-activity>
        <exo-news-archive v-if="news.archived" :news-id="newsId" :news-archived="news.archived" :news-title="news.title" :pinned="news.pinned" @update-archived-field="updateArchivedField"></exo-news-archive>
        
        <div class="newsDetails">
          <img :src="news.illustrationURL" class="newsImage illustrationPicture" alt="News"/>

          <div class="news-top-information">
            <div class="news-header-content">
              <div id="titleNews" class="newsTitle">
                <a :href="news.titleLink" class="activityLinkColor newsTitleLink">{{ news.title }}</a>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsAuthor">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.postedBy') }} :</span>
                    <a :href="news.authorProfileURL" class="newsInformationValue newsAuthorName"> {{ news.authorFullName }}</a>
                  </div>
                  <div>
                    <span class="newsInformationLabel"> {{ $t('news.activity.publicationDate') }} :</span>
                    <span class="newsInformationValue newsPostedDate"> {{ news.postedDate }}</span>
                  </div>
                </div>
                <div v-if="showUpdateInfo" class="newsUpdater">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdatedBy') }} :</span>
                    <a :href="news.updaterProfileURL" class="newsInformationValue newsUpdaterName">{{ news.updaterFullName }}</a>
                  </div>
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdatedDate') }} :</span>
                    <span class="newsInformationValue newsUpdatedDate">{{ news.updatedDate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="news.summary" id="newsSummary" class="summary">
            <span v-html="linkifiedSummary"></span>
          </div>

          <div id="newsBody" class="fullDetailsBody clearfix">
            <span v-html="news.body"></span>
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachmentsTitle">
            {{ $t('news.details.attachments.title') }} ({{ news.attachments ? news.attachments.length : 0 }})
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachments">
            <div v-for="attachedFile in news.attachments" :key="attachedFile.id" class="newsAttachment" @click="openPreview(attachedFile)">
              <div class="fileType">
                <i :class="getIconClassFromFileMimeType(attachedFile.mimetype)" class="uiIconFileTypeDefault"></i>
              </div>
              <div class="fileDetails">
                <div class="fileDetails1">
                  <div class="fileNameLabel" data-toggle="tooltip" rel="tooltip" data-placement="top">{{ attachedFile.name }}</div>
                </div>
                <div class="fileDetails2">
                  <div class="fileSize">{{ getFormattedFileSize(attachedFile.size) }} {{ $t('news.file.size.mega') }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';

export default {

  props: {
    news: {
      type: Object,
      required: false,
      default: function() { return new Object(); }
    },
    newsId: {
      type: String,
      required: false,
      default: null
    },
    activityId: {
      type: String,
      required: false,
      default: null
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showShareButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showPinInput: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      showUpdateInfo: this.news.postedDate !== this.news.updatedDate || this.news.authorFullName !== this.news.updaterFullName,
      BYTES_IN_MB: 1048576,
    };
  },
  computed: {
    linkifiedSummary : function() {
      return newsServices.linkifyText(newsServices.escapeHTML(this.news.summary));
    }
  },
  mounted() {
    this.updateViewsCount();
    window.require(['SHARED/social-ui-profile'], function(socialProfile) {
      const labels = {
        StatusTitle: 'Loading...',
        Connect: 'Connect',
        Confirm: 'Confirm',
        CancelRequest: 'Cancel Request',
        RemoveConnection: 'Remove Connection',
        Ignore: 'Ignore'
      };
      socialProfile.initUserProfilePopup('newsDetails', labels);
    });
    const linkContentElements = document.querySelector('#newsDetails #newsBody a');
    if (linkContentElements) {
      linkContentElements.setAttribute('target', '_blank');
    }
    if(this.showPinInput) {
      document.querySelector('#pinNewsActivity').style.display = '';
    }
  },
  methods: {
    updateArchivedField() {
      this.news.archived = false;
    },
    updateViewsCount: function () {
      newsServices.incrementViewsNumberOfNews(this.newsId);
    },
    getIconClassFromFileMimeType: function(fileMimeType) {
      if(fileMimeType) {
        const fileMimeTypeClass = fileMimeType.replace(/\./g, '').replace('/', '').replace('\\', '');
        return `uiIconFileType${fileMimeTypeClass}`;
      } else {
        return '';
      }
    },
    getFormattedFileSize(fileSize) {
      const formattedSizePrecision = 2;
      const sizeMB = fileSize / this.BYTES_IN_MB;
      return sizeMB.toFixed(formattedSizePrecision);
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
            downloadUrl: `/rest/v1/news/attachments/${attachedFile.id}/file`,
            openUrl: `/rest/v1/news/attachments/${attachedFile.id}/open`
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
      if(commentsPanel != null && collapsedCommentsButton != null) {
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
