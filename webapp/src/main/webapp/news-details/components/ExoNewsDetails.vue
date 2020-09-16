<template>
  <div id="newsDetails">
    <a class="backBtn" @click="goBack()"><i class="uiIconBack"></i></a>
    <div v-if="news.archived && !news.canArchive">
      <div class="userNotAuthorized">
        <div class="notAuthorizedIconDiv">
          <img src="/news/images/notauthorized.png" class="iconNotAuthorized">
        </div>
        <h3>{{ $t('news.archive.text') }}</h3>
      </div>
    </div>
    <div v-else class="newsDetails-description">
      <div :class="[news.illustrationURL ? 'newsDetails-header' : '']" class="newsDetails-header">
        <div v-if="news.illustrationURL" class="illustration">
          <img :src="news.illustrationURL" class="newsDetailsImage illustrationPicture" alt="News"/>
        </div>
        <div class="newsDetails">
          <div class="newsDetailsIconsShared">
            <exo-news-share-activity v-if="showShareButton" :news="news"></exo-news-share-activity>
          </div>
          <div class="newsDetailsIcons">
            <exo-news-activity-edit-composer v-if="showEditButton" :news-id="newsId" :space-id="spaceId" :activity-id="activityId"></exo-news-activity-edit-composer>
            <exo-news-pin-activity v-if="showPinInput" :news-id="newsId" :news-pinned="news.pinned" :news-archived="news.archived" :news-title="news.title"></exo-news-pin-activity>
          </div>
          <div class="news-top-information">
            <div id="titleNews" class="newsTitle newsTitleMobile">
              <a class="activityLinkColor newsTitleLink">{{ news.title }}</a>
            </div>
            <div v-if="news.archived" class="newsArchived">
              <exo-news-archive v-if="news.archived" :news-id="newsId" :news-archived="news.archived" :news-title="news.title" :pinned="news.pinned" @update-archived-field="updateArchivedField"></exo-news-archive>
              <span class="newsArchiveLabel"> ( {{ $t('news.archive.label') }} ) </span>
            </div>
          </div>
          <div class="newsInformationBackground">
            <div :class="[showUpdateInfo ? 'news-update-details-header' : 'news-details-header']" class="news-header-content">
              <div :class="[ showUpdateInfo ? 'newsUpdateInfo' : '']">
                <div class="activityAvatar avatarCircle">
                  <a :href="news.authorProfileURL">
                    <img :src="news.profileAvatarURL" class="avatar">
                  </a>
                </div>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsAuthor">
                  <a :href="news.authorProfileURL" class="newsInformationValue newsAuthorName news-details-information"> {{ news.authorFullName }} </a>
                  <span v-if="!news.hiddenSpace" class="newsInformationLabel"> {{ $t('news.activity.in') }} </span>
                  <div v-if="!news.hiddenSpace" class="newsSpace">
                    <a :href="news.spaceUrl" class="newsInformationLabel news-details-information">{{ news.spaceDisplayName }}</a>
                  </div>
                  <span class="newsInformationValue newsPostedDate news-details-information">- {{ news.postedDate }}</span>
                </div>
                <div v-if="showUpdateInfo" class="newsUpdater">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdated') }} </span>
                  </div>
                  <div>
                    <span class="newsInformationValue newsUpdatedDate">{{ news.updatedDate }}</span>

                    <div v-if="news.authorFullName != news.updaterFullName ">
                      <span class="newsInformationLabel"> {{ $t('news.activity.by') }} </span>
                      <a :href="news.updaterProfileURL" class="newsInformationValue newsUpdaterName">{{ news.updaterFullName }}</a>
                    </div>

                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="news.summary" id="newsSummary" class="summary">
            <span v-html="linkifiedSummary"></span>
          </div>

          <div id="newsBody" :class="[!news.summary ? 'fullDetailsBodyNoSummary' : '']" class="fullDetailsBody clearfix">
            <span v-html="news.body"></span>
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachmentsTitle">
            {{ $t('news.details.attachments.title') }} ({{ news.attachments ? news.attachments.length : 0 }})
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachments">
            <div v-for="attachedFile in news.attachments" :key="attachedFile.id" class="newsAttachment" @click="openPreview(attachedFile)">
              <exo-attachment-item :file="attachedFile"></exo-attachment-item>
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
      spaceId: null,
      showUpdateInfo: this.news.updatedDate  !== 'null' ,
      BYTES_IN_MB: 1048576,
      spaceDisplayName: this.news.spaceDisplayName,
    };
  },
  computed: {
    linkifiedSummary : function() {
      return newsServices.linkifyText(newsServices.escapeHTML(this.news.summary));
    }
  },
  created() {
    newsServices.getNewsById(this.newsId).then(news => {
      this.spaceId = news.spaceId;
    });
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
    const linkContentElements = document.querySelectorAll('#newsDetails a');
    linkContentElements.forEach(function(linkContentElement) {
      if (linkContentElement && !linkContentElement.href.includes(`${document.location.host}${eXo.env.portal.context}`)) {
        linkContentElement.setAttribute('target', '_blank');
      }
    });
    
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
      if(commentsPanel != null && collapsedCommentsButton != null) {
        commentsPanel.style.display = 'none';
        collapsedCommentsButton.style.display = 'none';
        document.querySelector('.uiDocumentPreview').classList += ' collapsed';
      } else {
        setTimeout(this.hideDocPreviewComments, intervalCheck);
      }
    },
    goBack() {
      if( history.length > 1) {
        history.back();
      } else {
        window.open('/', '_self');
      }
    }
  }
};
</script>
