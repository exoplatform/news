<template>
  <div id="newsActivityComposer" class="newsComposer">

    <div class="newsComposerActions">
      <div class="newsFormButtons">
        <div class="newsFormLeftActions">
          <img src="/news/images/newsImageDefault.png" />
          <span class="newsFormTitle">{{ newsFormTitle }}</span>
        </div>
        <div v-if="showPinInput" class="pinArticleContent " @click="news.pinned">
          <a id="newsPinButton" :data-original-title=" news.pinned ? $t('news.unpin.action') : $t('news.pin.action')" :class="[news.archived ? 'unauthorizedPin' : '']" class="pinArticle"
             rel="tooltip"
             data-placement="bottom"
             @click="!news.archived ? news.pinned = !news.pinned : null">
            <i :class="[news.pinned ? '' : 'unpinned']" class="uiIconPin" > </i>
          </a>
        </div>
        <div v-if="!editMode" class="newsFormRightActions">
          <p class="draftSavingStatus">{{ draftSavingStatus }}</p>
          <div class="newsDrafts">
            <exo-news-draft :space-id="spaceId" @draftSelected="onSelectDraft"/>
          </div>
          <button id="newsPost" :disabled="postDisabled" class="btn btn-primary" @click="postNews"> {{ $t("news.composer.post") }}
          </button>
        </div>
        <div v-if="editMode" class="newsFormRightActions">
          <button id="newsEdit" :disabled="updateDisabled" class="btn btn-primary" @click.prevent="updateNews"> {{ $t("news.edit.update") }}
          </button>
          <button id="newsUpdateAndPost" :disabled="news.archived ? true: updateDisabled" :class="[news.archived ? 'unauthorizedPin' : '']" class="btn" @click.prevent="updateAndPostNews"> {{ $t("news.edit.update.post") }}
          </button>
        </div>
      </div>
      <div id="newsTop"></div>
    </div>

    <form id="newsForm" class="newsForm">
      <div class="newsFormInput">
        <div class="newsFormAttachment">
          <div class="control-group attachments">
            <div class="controls">
              <exo-file-drop v-model="news.illustration" @change="autoSave"/>
            </div>
          </div>
        </div>
        <div class="formInputGroup newsTitle">
          <input id="newsTitle" v-model="news.title" :maxlength="titleMaxLength" :placeholder="newsFormTitlePlaceholder" type="text">
        </div>
        <div class="formInputGroup">
          <textarea id="newsSummary"
                    v-model="news.summary"
                    :maxlength="summaryMaxLength"
                    :placeholder="newsFormSummaryPlaceholder"
                    class="newsFormInput">
          </textarea>
        </div>
        <div class="formInputGroup">
          <textarea id="newsContent"
                    v-model="news.content"
                    :placeholder="newsFormContentPlaceholder"
                    class="newsFormInput"
                    name="newsContent">
          </textarea>
        </div>
      </div>
    </form>
    <exo-news-attachments :space-id="news.spaceId" v-model="news.attachments"></exo-news-attachments>
    <!-- The following bloc is needed in order to display the pin confirmation popup -->
    <!--begin -->
    <div class="uiPopupWrapper UISocialConfirmation" style="display: none;">
      <div class="UIPopupWindow UIDragObject uiPopup " style="width: 550px;">
        <div class="popupHeader clearfix">
          <a class="uiIconClose pull-right" title="Close"></a>
          <span class="PopupTitle popupTitle"></span>
        </div>
        <div class="PopupContent popupContent">
          <ul class="singleMessage popupMessage resizable">
            <li>
              <span class="confirmationIcon contentMessage"></span>
            </li>
          </ul>
          <div class="uiAction uiActionBorder"></div>
        </div>
      </div>
    </div>
    <!-- end -->
  </div>
</template>

<script>
import * as newsServices from '../../services/newsServices';
import autosize from 'autosize';
export default {
  props: {
    newsId: {
      type: String,
      required: false,
      default: null
    },
    spaceId: {
      type: String,
      required: true
    },
    activityId: {
      type: String,
      required: false,
      default: null
    },
    showPinInput: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      news: {
        id: '',
        activityId: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        attachments: [],
        spaceId: '',
        pinned: false,
        archived: false,
      },
      originalNews: {
        id: '',
        activityId: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        attachments: [],
        spaceId: '',
        pinned: false,
      },
      file: {
        src: ''
      },
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      titleMaxLength: 150,
      summaryMaxLength: 1000,
      autoSaveDelay: 1000,
      newsFormTitle: '',
      newsFormTitlePlaceholder: `${this.$t('news.composer.placeholderTitleInput')}*`,
      newsFormSummaryPlaceholder: this.$t('news.composer.placeholderSummaryInput'),
      newsFormContentPlaceholder: `${this.$t('news.composer.placeholderContentInput')}*`,
      newsFormContentHeight: '250',
      newsFormSummaryHeight: '80',
      initDone: false,
      initIllustrationDone: false,
      showDraftNews: false,
      postingNews: false,
      savingDraft: false,
      saveDraft : '',
      draftSavingStatus: '',
      illustrationChanged: false,
      attachmentsChanged: false,
      imagesURLs: new Map(),
      showAttachments: false,
      uploading: false
    };
  },
  computed: {
    editMode: function() {
      return this.newsId !== null;
    },
    postDisabled: function () {
      return this.uploading || !this.news.title || !this.news.title.trim() || !this.news.body || !this.news.body.replace(/&nbsp;/g, '').trim();
    },
    updateDisabled: function () {
      // disable update button while uploading an attachment
      if(this.uploading) {
        return true;
      }
      // disable update button if a mandatory field is empty
      if(!this.news.title || !this.news.title.trim() || !this.news.body || !this.news.body.replace(/&nbsp;/g, '').trim()) {
        return true;
      }
      // disable update button nothing has changed
      if(!this.illustrationChanged && !this.attachmentsChanged
                 && this.news.title === this.originalNews.title
                 && this.news.summary === this.originalNews.summary
                 && this.news.body === this.originalNews.body
                 && this.news.pinned === this.originalNews.pinned) {
        return true;
      }

      return false;
    }
  },
  watch: {
    'news.title': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } },
    'news.summary': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } },
    'news.body': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } },
    'news.attachments': function() {
      if(this.initDone) {
        this.attachmentsChanged = true;
      }
      this.autoSave();
    },
    'news.illustration': function() {
      if(this.initIllustrationDone) {
        this.illustrationChanged = true;
        this.autoSave();
      }
    }
  },
  created() {
    if(this.newsId) {
      this.initNewsComposerData(this.newsId);
    } else {
      this.initDone = true;
    }
    this.displayFormTitle();

  },
  mounted() {
    $('[rel="tooltip"]').tooltip();
    autosize(document.querySelector('#newsSummary'));
    this.extendForm();
  },
  beforeDestroy() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if (textarea && shareButton) {
      textarea.style.display = 'block';
      shareButton.style.display = 'block';
    }
  },
  methods: {
    initCKEditor: function() {
      if (typeof CKEDITOR.instances['newsContent'] !== 'undefined') {
        CKEDITOR.instances['newsContent'].destroy(true);
      }
      CKEDITOR.plugins.addExternal('video','/news/js/ckeditor/plugins/video/','plugin.js');
      let extraPlugins = 'sharedspace,simpleLink,selectImage,suggester,font,justify,widget,video';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }
      CKEDITOR.addCss('.cke_editable { font-size: 18px; }');

      // this line is mandatory when a custom skin is defined

      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      $('textarea#newsContent').ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image,confirmBeforeReload,maximize,resize',
        allowedContent: true,
        toolbarLocation: 'top',
        removeButtons: 'Subscript,Superscript,Cut,Copy,Paste,PasteText,PasteFromWord,Undo,Redo,Scayt,Unlink,Anchor,Table,HorizontalRule,SpecialChar,Maximize,Source,Strike,Outdent,Indent,BGColor,About',
        toolbar: [
          { name: 'format', items: ['Format'] },
          { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'RemoveFormat'] },
          { name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'Blockquote' ] },
          { name: 'fontsize', items: ['FontSize'] },
          { name: 'colors', items: [ 'TextColor' ] },
          { name: 'align', items: [ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },
          { name: 'links', items: [ 'simpleLink', 'selectImage', 'Video'] },
        ],
        format_tags: 'p;h1;h2;h3',
        autoGrow_minHeight: this.newsFormContentHeight,
        height: this.newsFormContentHeight,
        bodyClass: 'newsContent',
        dialog_noConfirmCancel: true,
        sharedSpaces: {
          top: 'newsTop'
        },
        on: {
          change: function (evt) {
            self.news.body = evt.editor.getData();
          }
        }
      });
    },
    displayFormTitle: function() {
      if(!this.editMode) {
        newsServices.getSpaceById(this.spaceId).then(space => {
          this.newsFormTitle = this.$t('news.composer.createNews').replace('{0}', space.displayName);
        });
      } else {
        this.newsFormTitle = this.$t('news.edit.editNews');
      }
    },

    initNewsComposerData: function(newsId) {
      const self = this;
      newsServices.getNewsById(newsId)
        .then(fetchedNode => {
          if(fetchedNode){
            this.news.id = fetchedNode.id;
            this.news.title = fetchedNode.title;
            this.news.summary = fetchedNode.summary;
            this.news.body = fetchedNode.body;
            this.news.pinned = fetchedNode.pinned;
            this.news.archived = fetchedNode.archived;
            this.news.spaceId = fetchedNode.spaceId;
            CKEDITOR.instances['newsContent'].setData(fetchedNode.body);
            if (fetchedNode.illustrationURL) {
              newsServices.importFileFromUrl(fetchedNode.illustrationURL)
                .then(resp => resp.blob())
                .then(fileData => {
                  const illustrationFile = new File([fileData], `illustration${newsId}`);
                  const fileDetails = {
                    id: null,
                    uploadId: null,
                    name: illustrationFile.name,
                    size: illustrationFile.size,
                    src: fetchedNode.illustrationURL,
                    progress: null,
                    file: illustrationFile,
                    finished: true,
                  };
                  this.news.illustration.push(fileDetails);
                  this.originalNews.illustration.push(fileDetails);
                })
                .then(() => this.initIllustrationDone = true);
            } else {
              this.initIllustrationDone = true;
            }
            this.news.attachments = fetchedNode.attachments;
            this.originalNews = JSON.parse(JSON.stringify(this.news));
            Vue.nextTick(() => {
              autosize.update(document.querySelector('#newsSummary'));
              self.initDone = true;
            });
          } else {
            self.initDone = true;
          }
        });
    },

    autoSave: function() {
      // No draft saving if init not done or in edit mode for the moment
      if(!this.initDone || this.editMode) {
        return;
      }
      // if the News is being posted, no need to autosave anymore
      if(this.postingNews) {
        return;
      }
      clearTimeout(this.saveDraft);
      this.saveDraft = setTimeout(() => {
        this.savingDraft = true;
        this.draftSavingStatus = this.$t('news.composer.draft.savingDraftStatus');
        Vue.nextTick(() => this.saveNewsDraft());
      }, this.autoSaveDelay);
    },

    postNews: function () {
      if(this.news.pinned === true) {
        const confirmText = this.$t('news.pin.confirm');
        const captionText = this.$t('news.pin.action');
        const confirmButton = this.$t('news.pin.btn.confirm');
        const cancelButton = this.$t('news.edit.cancel');
        eXo.social.PopupConfirmation.confirm('createdPinnedNews', [{action: this.doPostNews, label : confirmButton}], captionText, confirmText, cancelButton);
      } else {
        this.doPostNews();
      }
    },
    doPostNews: function () {
      this.postingNews = true;
      // if the News draft is being saved, we have to wait until it is done before posting the News
      if(this.savingDraft) {
        this.$on('draftCreated', this.saveNews);
        this.$on('draftUpdated', this.saveNews);
      } else {
        this.saveNews();
      }
    },
    saveNews: function () {
      clearTimeout(this.saveDraft);
      this.$off('draftCreated', this.saveNews);
      this.$off('draftUpdated', this.saveNews);

      this.news.body = newsServices.linkifyHTML(this.news.body, 'newsContent');
      this.news.body = this.replaceImagesURLs(this.news.body);
      const news = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary,
        body: this.news.body,
        author: eXo.env.portal.userName,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        spaceId: this.spaceId,
        publicationState: 'published'
      };

      if(this.news.illustration.length > 0) {
        news.uploadId = this.news.illustration[0].uploadId;
      }

      newsServices.saveNews(news).then((createdNews) => {
        this.postingNews = false;
        let createdNewsActivity = null;
        if(createdNews.activities) {
          const createdNewsActivities = createdNews.activities.split(';')[0].split(':');
          if(createdNewsActivities.length > 1) {
            createdNewsActivity = createdNewsActivities[1];
          }
        }
        if(createdNewsActivity) {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${createdNewsActivity}`;
        } else {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}`;
        }
      });
    },
    extendForm: function(){
      document.body.style.overflow = 'hidden';
      this.initCKEditor();
    },
    saveNewsDraft: function () {
      const news = {
        title: this.news.title,
        summary: this.news.summary,
        body: this.replaceImagesURLs(this.news.body),
        author: eXo.env.portal.userName,
        attachments: [],
        pinned: false,
        spaceId: this.spaceId,
        publicationState: ''
      };
      if (this.news.illustration.length > 0) {
        news.uploadId = this.news.illustration[0].uploadId;
      } else {
        news.uploadId = '';
      }

      const uploadedPercent = 100;
      this.news.attachments.forEach(attachment => {
        if(attachment.id || attachment.uploadProgress === uploadedPercent) {
          news.attachments.push({
            id: attachment.id,
            uploadId: attachment.uploadId,
            name: attachment.name
          });
        }
      });

      if (this.news.id) {
        if(this.news.title || this.news.summary || this.news.body || this.news.illustration.length > 0) {
          news.id = this.news.id;
          newsServices.updateNews(news)
            .then((updatedNews) => {
              if(this.news.body !== updatedNews.body) {
                // Images URLs may have been updated in body to transform temporary URLs to permanent URLs.
                // Updated URLs are extracted here and re-applied on save.
                // The goal is to avoid modifying the editor data, which can impact the editing experience.
                this.imagesURLs = this.extractImagesURLsDiffs(this.news.body, updatedNews.body);
              }
            })
            .then(() => this.$emit('draftUpdated'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        } else {
          newsServices.deleteDraft(this.news.id)
            .then(() => this.$emit('draftDeleted'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        }
        this.savingDraft = false;
      } else if(this.news.title || this.news.summary || this.news.body || this.news.illustration.length > 0) {
        news.publicationState = 'draft';
        newsServices.saveNews(news).then((createdNews) => {
          this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus');
          this.news.id = createdNews.id;
          this.savingDraft = false;
          this.$emit('draftCreated');
        });
      } else {
        this.draftSavingStatus = '';
      }
    },
    onSelectDraft: function(draftId){
      this.resetNewsActivity();
      this.initNewsComposerData(draftId);
    },

    extractImagesURLsDiffs: function(originalHTMLString, updatedHTMLString) {
      const imagesURLs = new Map();

      const originalHTML = $(originalHTMLString);
      const updatedHTML = $(updatedHTMLString);
      const originalImages = originalHTML.find('img');
      const updatedImages = updatedHTML.find('img');

      originalImages.each(function(index, element) {
        const originalImageURL = $(element).attr('src');
        const updatedImageURL = $(updatedImages[index]).attr('src');
        if(updatedImageURL !== originalImageURL) {
          imagesURLs.set(originalImageURL, updatedImageURL);
        }
      });
      return imagesURLs;
    },
    replaceImagesURLs: function(content) {
      let updatedContent = content;
      const specialCharactersRegex = /[-/\\^$*+?.()|[\]{}]/g;

      this.imagesURLs.forEach(function(value, key) {
        const escapedKey = key.replace(specialCharactersRegex, '\\$&');
        const regex = new RegExp(`src="${escapedKey}"`);
        updatedContent = updatedContent.replace(regex, `src="${value}"`);
      });

      return updatedContent;
    },
    resetNewsActivity: function(){
      this.news.id = '';
      this.news.title = '';
      this.news.summary = '';
      this.news.body = '';
      this.news.pinned = false;
      CKEDITOR.instances['newsContent'].setData('');
      this.news.illustration = [];
    },
    updateNews: function () {
      this.confirmAndUpdateNews().then(() => {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
      }).catch (function() {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
      });
    },
    updateAndPostNews: function () {
      if (!this.news.archived) {
        const self = this;
        this.confirmAndUpdateNews().then(() => {
          if(self.activityId)
          {
            const activity = {
              id: self.activityId,
              title: '',
              body: '',
              type: 'news',
              templateParams: {
                newsId: self.news.id,
              },
              updateDate: new Date().getTime()
            };

            return newsServices.updateAndPostNewsActivity(activity);
          }
        }).then(() => {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
        }).catch (function() {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
        });
      }
    },
    confirmAndUpdateNews: function() {
      if(this.news.pinned !== this.originalNews.pinned) {
        let confirmText = this.$t('news.pin.confirm');
        let captionText = this.$t('news.pin.action');
        const confirmButton = this.$t('news.pin.btn.confirm');
        const cancelButton = this.$t('news.pin.btn.cancel');
        if(this.news.pinned === false) {
          confirmText = this.$t('news.unpin.confirm').replace('{0}', this.news.title);
          captionText = this.$t('news.unpin.action');
        }
        const self = this;
        return new Promise(function(resolve) {
          eXo.social.PopupConfirmation.confirm('createdPinnedNews', [{action: function() { self.doUpdateNews().then(resolve()); }, label : confirmButton}], captionText, confirmText, cancelButton);
        });
      } else {
        return this.doUpdateNews();
      }
    },
    doUpdateNews: function () {
      this.news.body = newsServices.linkifyHTML(this.news.body, 'newsContent');
      const updatedNews = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary != null ? this.news.summary : '',
        body: this.news.body,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        publicationState: 'published'
      };
      if(this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if(this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }

      return newsServices.updateNews(updatedNews);
    }
  }
};
</script>