<template>
  <div id="newsDetails">
    <a class="backBtn" :href="news.spaceUrl"><i class="uiIconBack"></i></a>
    <exo-news-details-action-menu
      v-if="showEditButton"
      :news="news"
      :show-edit-button="showEditButton"
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
          <div class="news-top-information d-flex">
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
                  <a :href="authorProfileURL">
                    <img :src="authorAvatarURL" class="avatar">
                  </a>
                </div>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsAuthor">
                  <a :href="authorProfileURL" class="newsInformationValue newsAuthorName news-details-information"> {{ authorFullName }} </a>
                  <span v-if="!news.hiddenSpace" class="newsInformationLabel"> {{ $t('news.activity.in') }} </span>
                  <div v-if="!news.hiddenSpace" class="newsSpace">
                    <a :href="news.spaceUrl" class="newsInformationLabel news-details-information">{{ news.spaceDisplayName }}</a>
                  </div>
                  <template v-if="publicationDate">
                    -
                    <date-format
                      :value="publicationDate"
                      :format="dateFormat"
                      class="newsInformationValue newsPostedDate news-details-information" />
                  </template>
                  <span v-else-if="news.postedDate" class="newsInformationValue newsPostedDate news-details-information">- {{ news.postedDate }}</span>
                </div>
                <div v-if="showUpdateInfo" class="newsUpdater">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdated') }} </span>
                  </div>
                  <div>
                    <template v-if="updatedDate">
                      <date-format
                        :value="updatedDate"
                        :format="dateFormat"
                        class="newsInformationValue newsUpdatedDate" />
                    </template>
                    <div v-else-if="news.updatedDate" class="newsInformationValue newsUpdatedDate">{{ news.updatedDate }}</div>

                    <div v-if="notSameUpdater">
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
            <span v-html="news.summary"></span>
          </div>

          <div
            id="newsBody"
            :class="[!news.summary ? 'fullDetailsBodyNoSummary' : '']"
            class="fullDetailsBody clearfix">
            <span v-html="newsBody"></span>
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
    showPinButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  data() {
    return {
      spaceId: null,
      updaterIdentity: null,
      BYTES_IN_MB: 1048576,
      spaceDisplayName: this.news.spaceDisplayName,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
    };
  },
  computed: {
    newsBody() {
      return this.targetBlank(this.news.body);
    },
    showUpdateInfo() {
      return this.updatedDate || (this.news && this.news.updatedDate && this.news.updatedDate  !== 'null');
    },
    authorFullName() {
      return this.news && (this.news.authorFullName || this.news.authorDisplayName);
    },
    authorProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.updater}`;
    },
    authorAvatarURL() {
      return this.news && (this.news.profileAvatarURL || this.news.authorAvatarUrl);
    },
    updaterFullName() {
      return (this.news && this.news.updaterFullName) || (this.updaterIdentity && this.updaterIdentity.profile && this.updaterIdentity.profile.fullname);
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.updater}`;
    },
    notSameUpdater() {
      return this.news && (this.news.updater !== this.news.author || this.news.authorFullName !== this.news.updaterFullName);
    },
    publicationDate() {
      return this.news && this.news.publicationDate && this.news.publicationDate.time && new Date(this.news.publicationDate.time);
    },
    updatedDate() {
      return this.news && this.news.updateDate && this.news.updateDate.time && new Date(this.news.updateDate.time);
    },
    newsSummary() {
      return this.targetBlank(this.news.summary);
    }
  },
  created() {
    if (!this.news || !this.news.spaceId) {
      this.$newsServices.getNewsById(this.newsId)
        .then(news => {
          this.spaceId = news.spaceId;
          return this.$nextTick();
        })
        .finally(() => {
          this.$root.$emit('application-loaded');
        });
    } else {
      this.spaceId = this.news && this.news.spaceId;
      if (this.notSameUpdater && this.news && this.news.updater) {
        this.$identityService.getIdentityByProviderIdAndRemoteId('organization', this.news.updater)
          .then(identity => this.updaterIdentity = identity);
      }
      this.$root.$emit('application-loaded');
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
    },
    targetBlank: function (content) {
      const internal = location.host + eXo.env.portal.context;
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      const links = docElement.getElementsByTagName('a');
      links.forEach(function (link) {
        let href = link.href.replace(/(^\w+:|^)\/\//, '');
        if (href.endsWith('/')) {
          href = href.slice(0, -1);
        }
        if (link && href !== location.host && !href.startsWith(internal)) {
          link.setAttribute('target', '_blank');
          link.setAttribute('rel', 'noopener noreferrer');
        }
      });
      return docElement.innerHTML;
    },
  }
};
</script>
