<template>
  <div id="newsDetails">
    <a class="backBtn" :href="backURL"><i class="uiIconBack my-4"></i></a>
    <v-btn
      v-if="publicationState === 'staged'"
      class="btn newsDetailsActionMenu mt-6 mr-2 pull-right"
      @click="$root.$emit('open-schedule-drawer','editScheduledNews')">
      {{ $t("news.composer.btn.scheduleArticle") }}
    </v-btn>
    <schedule-news-drawer
      @post-article="postNews"
      :news-id="newsId" />
    <exo-news-details-action-menu
      v-if="showEditButton"
      :news="news"
      :show-edit-button="showEditButton"
      :show-delete-button="showDeleteButton"
      @delete="deleteConfirmDialog"
      @edit="editLink" />
    <div class="newsDetailsIcons">
      <exo-news-pin
        v-if="showPinButton"
        :news-id="newsId"
        :news-pinned="news.pinned"
        :news-archived="archivedNews"
        :news-title="newsTitle" />
    </div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="$t('news.message.confirmDeleteNews')"
      :title="$t('news.title.confirmDeleteNews')"
      :ok-label="$t('news.button.ok')"
      :cancel-label="$t('news.button.cancel')"
      @ok="deleteNews" />
    <div v-if="archivedNews && !news.canArchive">
      <div class="userNotAuthorized">
        <div class="notAuthorizedIconDiv">
          <img src="/news/images/notauthorized.png" class="iconNotAuthorized">
        </div>
        <h3>{{ $t('news.archive.text') }}</h3>
      </div>
    </div>
    <div v-else class="newsDetails-description">
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
                    <img :src="authorAvatarURL" class="avatar">
                  </a>
                </div>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsAuthor">
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
                <div v-if="showUpdateInfo" class="newsUpdater">
                  <div v-if="publicationState !== 'staged'">
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdated') }} </span>
                  </div>
                  <div v-else>
                    <span class="newsInformationLabel">{{ $t('news.details.scheduled') }} </span>
                  </div>
                  <div>
                    <template v-if="publicationState !== 'staged' && updatedDate">
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
            <span v-html="newsSummary"></span>
          </div>

          <div
            id="newsBody"
            :class="[!summary ? 'fullDetailsBodyNoSummary' : '']"
            class="fullDetailsBody clearfix">
            <span v-html="newsBody"></span>
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
    <exo-news-notification-alerts />
  </div>
</template>
<script>
const USER_TIMEZONE_ID = new window.Intl.DateTimeFormat().resolvedOptions().timeZone;
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
      default: ''
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showPinButton: {
      type: Boolean,
      required: false,
      default: false
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
      updaterIdentity: null,
      BYTES_IN_MB: 1048576,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
      dateTimeFormat: {
        hour: '2-digit',
        minute: '2-digit',
      },
    };
  },
  computed: {
    newsBody() {
      return this.news && this.targetBlank(this.news.body);
    },
    showUpdateInfo() {
      return this.updatedDate || (this.news && this.news.updatedDate && this.news.updatedDate  !== 'null');
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
    backURL() {
      return this.news && this.news.spaceMember ? this.news.spaceUrl : `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
    },
    updaterFullName() {
      return (this.news && this.news.updaterFullName) || (this.updaterIdentity && this.updaterIdentity.profile && this.updaterIdentity.profile.fullname);
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.updater}`;
    },
    notSameUpdater() {
      return this.news && this.news.updater !=='__system' && (this.news.updater !== this.news.author || this.news.authorFullName !== this.news.updaterFullName);
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
    archivedNews() {
      return this.news && this.news.archived;
    },    
    illustrationURL() {
      return this.news && this.news.illustrationURL;
    },
    newsTitle() {
      return this.news && this.news.title;
    },
    hiddenSpace() {
      return this.news && this.news.hiddenSpace;
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
    scheduleDate() {
      return this.news && this.news.schedulePostDate;
    },
  },
  created() {
    if (!this.news || !this.news.spaceId) {
      this.$newsServices.getNewsById(this.newsId)
        .then(news => {
          this.spaceId = news.spaceId;
          if (!this.news) {
            this.news = news;
          }
          // TODO news.newsId should be converted to use news.id
          // to correspond to retruned object by REST
          if (!this.news.newsId) {
            this.news.newsId = this.newsId;
          }
          return this.$nextTick();
        })
        .finally(() => {
          document.title = this.$t('news.window.title', {0: this.news.title});
          this.$root.$emit('application-loaded');
        });
    } else {
      this.spaceId = this.news.spaceId;
      if (!this.news.newsId) {
        this.news.newsId = this.newsId;
      }
      if (this.notSameUpdater && this.news && this.news.updater) {
        this.$identityService.getIdentityByProviderIdAndRemoteId('organization', this.news.updater)
          .then(identity => this.updaterIdentity = identity);
      }
      this.$root.$emit('application-loaded');
    }
  },
  mounted() {
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
      const editUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?spaceId=${this.spaceId}&newsId=${this.newsId}&activityId=${this.activityId}`;
      window.open(editUrl, '_target');
    },
    deleteConfirmDialog() {
      this.$refs.deleteConfirmDialog.open();
    },
    deleteNews() {
      const deleteDelay = 6;
      const redirectionTime = 6100;
      this.$newsServices.deleteNews(this.newsId, false, deleteDelay)
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
      for (let i=0; i < links.length ; i++) {
        let href = links[i].href.replace(/(^\w+:|^)\/\//, '');
        if (href.endsWith('/')) {
          href = href.slice(0, -1);
        }
        if (href !== location.host && !href.startsWith(internal)) {
          links[i].setAttribute('target', '_blank');
          links[i].setAttribute('rel', 'noopener noreferrer');
        }
      }
      return docElement.innerHTML;
    },
    postNews(schedulePostDate, postArticleMode, publish) {
      this.news.timeZoneId = USER_TIMEZONE_ID;
      this.news.pinned = publish;
      if (postArticleMode === 'later') {
        this.news.schedulePostDate = schedulePostDate;
        this.$newsServices.scheduleNews(this.news).then((scheduleNews) => {
          if (scheduleNews) {
            window.location.href = scheduleNews.url;
          }
        });
      } else if (postArticleMode === 'immediate') {
        this.news.publicationState = 'published';
        this.$newsServices.saveNews(this.news).then((createdNews) => {
          let createdNewsActivity = null;
          if (createdNews.activities) {
            const createdNewsActivities = createdNews.activities.split(';')[0].split(':');
            if (createdNewsActivities.length > 1) {
              createdNewsActivity = createdNewsActivities[1];
            }
          }
          if (createdNewsActivity) {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${createdNewsActivity}`;
          } else {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
          }
        });
      } else {
        this.news.publicationState = 'draft';
        this.$newsServices.saveNews(this.news).then((createdNews) => {
          this.news.id = createdNews.id;
          this.$emit('draftCreated');
          if (createdNews) {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news?filter=drafts`;
          }
        });
      }
    },
  }
};
</script>
