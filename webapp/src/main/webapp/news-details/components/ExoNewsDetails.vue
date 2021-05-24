<template>
  <div id="newsDetails">
    <a class="backBtn" @click="goBack()"><i class="uiIconBack"></i></a>
    <exo-news-details-action-menu
      v-if="showShareButton || showEditButton"
      :news="news"
      :show-edit-button="showEditButton"
      :show-share-button="showShareButton"
      :show-delete-button="showDeleteButton"
      @delete="deleteConfirmDialog"
      @edit="editLink" />
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="$t('news.message.confirmDeleteNews')"
      :title="$t('news.title.confirmDeleteNews')"
      :ok-label="$t('news.button.ok')"
      :cancel-label="$t('news.button.cancel')"
      @ok="deleteNews" />
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
          <img
            :src="news.illustrationURL"
            class="newsDetailsImage illustrationPicture"
            alt="News">
        </div>
        <div class="newsDetails">
          <div class="newsDetailsIcons">
            <exo-news-pin
              v-if="showPinButton"
              :news-id="newsId"
              :news-pinned="news.pinned"
              :news-archived="news.archived"
              :news-title="news.title" />
          </div>
          <div class="news-top-information">
            <div id="titleNews" class="newsTitle newsTitleMobile">
              <a class="activityLinkColor newsTitleLink">{{ news.title }}</a>
            </div>
            <div v-if="news.archived" class="newsArchived">
              <exo-news-archive
                v-if="news.archived"
                :news-id="newsId"
                :news-archived="news.archived"
                :news-title="news.title"
                :pinned="news.pinned"
                @update-archived-field="updateArchivedField" />
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

          <div
            v-if="news.summary"
            id="newsSummary"
            class="summary">
            <span v-sanitized-html="news.summary"></span>
          </div>

          <div
            id="newsBody"
            :class="[!news.summary ? 'fullDetailsBodyNoSummary' : '']"
            class="fullDetailsBody clearfix">
            <span v-sanitized-html="news.body"></span>
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachmentsTitle">
            {{ $t('news.details.attachments.title') }} ({{ news.attachments ? news.attachments.length : 0 }})
          </div>

          <div v-show="news.attachments && news.attachments.length" class="newsAttachments">
            <div
              v-for="attachedFile in news.attachments"
              :key="attachedFile.id"
              class="newsAttachment"
              @click="openPreview(attachedFile)">
              <exo-attachment-item :file="attachedFile" />
            </div>
          </div>
        </div>
      </div>
    </div>
    <v-app>
      <share-activity-drawer
        ref="shareNewsDrawer"
        class="shareNewsDrawer"
        :activity-id="activityId"
        @share-activity="shareNews" />
    </v-app>
    <exo-news-notification-alerts />
  </div>
</template>
<script>
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
    showPinButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data() {
    return {
      spaceId: null,
      showUpdateInfo: this.news.updatedDate  !== 'null' ,
      BYTES_IN_MB: 1048576,
      spaceDisplayName: this.news.spaceDisplayName,
    };
  },
  created() {
    this.$newsServices.getNewsById(this.newsId)
      .then(news => {
        this.spaceId = news.spaceId;
        return this.$nextTick();
      })
      .finally(() => {
        this.$root.$emit('application-loaded');
      });
    this.$root.$on('news-share-drawer-open', params => {
      this.newsId = params.news.newsId;
      this.open();
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
    
    if (this.showPinInput) {
      const pinButton = this.$root.$el.querySelector('#pinNewsActivity');
      if (pinButton) {
        pinButton.style.display = '';
      }
    }
  },
  methods: {
    updateArchivedField() {
      // eslint-disable-next-line vue/no-mutating-props
      this.news.archived = false;
    },
    updateViewsCount: function () {
      this.$newsServices.incrementViewsNumberOfNews(this.newsId);
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
    },
    open() {
      if (this.$refs.shareNewsDrawer) {
        this.$refs.shareNewsDrawer.open();
      }
    },
    close() {
      this.$refs.shareNewsDrawer.close();
    },
    shareNews(spaces, description) {
      const spacesList = [];
      spaces.forEach(space => {
        this.$spaceService.getSpaceByPrettyName(space,'identity').then(data => {
          spacesList.push(data.displayName);
        });
      });
      this.$newsServices.shareNews(this.news.newsId, this.news.activityId, description, spaces)
        .then(() => {
          this.$root.$emit('news-shared', this.news, spacesList);
          this.close();
        });
    },
    goBack() {
      if ( history.length > 1) {
        history.back();
      } else {
        window.open('/', '_self');
      }
    },
    editLink() {
      const editUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?spaceId=${this.spaceId}&newsId=${this.news.newsId}&activityId=${this.activityId}`;
      window.open(editUrl, '_self');
    },
    deleteConfirmDialog() {
      this.$refs.deleteConfirmDialog.open();
    },
    deleteNews() {
      const deleteDelay = 6;
      const redirectionTime = 6100;
      this.$newsServices.deleteNews(this.newsId, deleteDelay)
        .then(() => {
          this.$root.$emit('confirm-news-deletion', this.news);
        });
      setTimeout(() => {
        const deletedNews = localStorage.getItem('deletedNews');
        if (deletedNews != null) {
          window.location.href = this.news.spaceUrl;
        }
      }, redirectionTime);
    }
  }
};
</script>
