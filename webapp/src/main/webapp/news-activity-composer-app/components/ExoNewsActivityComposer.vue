<template>
  <v-app>
    <div v-show="loading">
      <div class="loadingComposer">
        <v-progress-circular
          indeterminate
          color="primary" />
      </div>
    </div>
    <div
      v-show="canCreatNews && !loading"
      id="newsActivityComposer"
      class="newsComposer">
      <exo-news-publish-drawer ref="publishNewsDrawer" @post-article="postNews" />
      <div class="newsComposerActions">
        <div class="newsFormButtons">
          <div class="newsFormLeftActions">
            <img src="/news/images/newsImageDefault.png">
            <span class="newsFormTitle">{{ newsFormTitle }}</span>
          </div>
          <div
            v-show="showPinInput"
            class="pinArticleContent "
            @click="news.pinned">
            <a
              id="newsPinButton"
              :data-original-title="originalTitle"
              :class="[news.archived ? 'unauthorizedPin' : '']"
              class="pinArticle"
              rel="tooltip"
              data-placement="bottom"
              @click="!news.archived ? news.pinned = !news.pinned : null">
              <v-icon
                :class="broadcastArticleClass"
                class="fas fa-bullhorn" />
            </a>
          </div>
          <div class="newsFormRightActions">
            <p class="draftSavingStatus">{{ draftSavingStatus }}</p>
            <v-btn
              v-show="!editMode"
              id="newsPost"
              :loading="postingNews"
              :disabled="postDisabled || postingNews"
              elevation="0"
              class="btn btn-primary"
              @click="openDrawer">
              {{ $t("news.composer.post") }}
            </v-btn>
          </div>
          <div v-show="editMode">
            <div class="d-flex flex-row">
              <v-btn
                id="newsEdit"
                :disabled="updateDisabled"
                class="btn btn-primary mr-2 "
                @click.prevent="updateNews">
                {{ $t("news.edit.update") }}
              </v-btn>
              <v-btn
                id="newsUpdateAndPost"
                :disabled="news.archived ? true: updateDisabled"
                :class="[news.archived ? 'unauthorizedPin' : '']"
                class="btn mr-2"
                @click.prevent="updateAndPostNews">
                {{ $t("news.edit.update.post") }}
              </v-btn>
              <v-btn class="btn mr-6" @click="goBack">
                {{ $t("news.composer.btn.cancel") }}
              </v-btn>
            </div>
          </div>
        </div>
        <div id="newsTop"></div>
      </div>

      <form id="newsForm" class="newsForm">
        <div class="newsFormInput">
          <div class="newsFormAttachment">
            <div class="control-group attachments">
              <div class="controls">
                <exo-news-file-drop v-model="news.illustration" @change="autoSave" />
              </div>
            </div>
          </div>
          <div class="formInputGroup newsTitle">
            <input
              id="newsTitle"
              v-model="news.title"
              :maxlength="titleMaxLength"
              :placeholder="newsFormTitlePlaceholder"
              type="text">
          </div>
          <div class="formInputGroup">
            <textarea
              id="newsSummary"
              v-model="news.summary"
              :maxlength="summaryMaxLength"
              :placeholder="newsFormSummaryPlaceholder"
              class="newsFormInput">
            </textarea>
          </div>
          <div class="formInputGroup">
            <textarea
              id="newsContent"
              v-model="news.content"
              :placeholder="newsFormContentPlaceholder"
              class="newsFormInput"
              name="newsContent">
            </textarea>
          </div>
        </div>
      </form>
      <v-btn
        class="attachmentsButton"
        fixed
        bottom
        right
        fab
        x-large
        @click="openApp()">
        <i class="uiIconAttachment"></i>
        <v-progress-circular
          :class="uploading ? 'uploading' : ''"
          indeterminate>
          {{ news.attachments.length }}
        </v-progress-circular>
      </v-btn>
      <exo-attachments
        ref="attachmentsComponent"
        :space-id="news.spaceId"
        v-model="news.attachments"
        @HideAttachmentsDrawer="onHideAttachmentsDrawer"
        @uploadingCountChanged="setUploadingCount" />
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
    
    <div v-show="!canCreatNews && !loading" class="newsComposer">
      <div id="form_msg_error" class="alert alert-error">
        <span data-dismiss="alert">
          <i class="uiIconColorError pull-left"></i>
        </span>
        <div class="msg_error">
          <div>
            <span class="msg_permission_denied">{{ $t("news.permission.denied") }}</span>
          </div>
          <div>
            <span class="msg_permission">{{ $t("news.permission.msg") }}</span>
          </div>
        </div>
      </div>
    </div>
  </v-app>
</template>

<script>
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
      saveDraft: '',
      draftSavingStatus: '',
      illustrationChanged: false,
      attachmentsChanged: false,
      imagesURLs: new Map(),
      uploading: false,
      canCreatNews: false,
      loading: true,
      currentSpace: {},
      spaceURL: null
    };
  },
  computed: {
    editMode: function() {
      return this.activityId && this.activityId !== '';
    },
    postDisabled: function () {
      return this.uploading || !this.news.title || !this.news.title.trim() || !this.news.body || !new DOMParser().parseFromString(this.news.body, 'text/html').documentElement.textContent.replace(/&nbsp;/g, '').trim();
    },
    updateDisabled: function () {
      // disable update button while uploading an attachment
      if (this.uploading) {
        return true;
      }
      // disable update button if a mandatory field is empty
      if (!this.news.title || !this.news.title.trim() || !this.news.body || !this.news.body.replace(/&nbsp;/g, '').trim()) {
        return true;
      }
      // disable update button nothing has changed
      if (!this.illustrationChanged && !this.attachmentsChanged
                 && this.news.title === this.originalNews.title
                 && this.news.summary === this.originalNews.summary
                 && this.news.body === this.originalNews.body
                 && this.news.pinned === this.originalNews.pinned
                 && this.news.publicationState !== 'draft') {
        return true;
      }

      return false;
    },
    broadcastArticleClass() {
      return this.news.pinned ? 'broadcastArticle' : 'unbroadcastArticle';
    },
    originalTitle() {
      return this.news.pinned ? this.$t('news.unbroadcast.action') : this.$t('news.broadcast.action');
    },
  },
  watch: {
    'news.title': function(newValue, oldValue) { if (newValue !== oldValue) { this.autoSave(); } },
    'news.summary': function(newValue, oldValue) { if (newValue !== oldValue) { this.autoSave(); } },
    'news.body': function(newValue, oldValue) { if (newValue !== oldValue) { this.autoSave(); } },
    'news.attachments': function() {
      if (this.initDone) {
        this.attachmentsChanged = true;
      }
      this.autoSave();
    },
    'news.illustration': function() {
      if (this.initIllustrationDone) {
        this.illustrationChanged = true;
        this.autoSave();
      }
    }
  },
  mounted() {
    document.body.style.overflow = 'hidden';
    autosize(document.querySelector('#newsSummary'));
    
    this.$newsServices.getSpaceById(this.spaceId).then(space => {
      this.currentSpace = space;
      this.spaceURL = this.currentSpace.prettyName;
      this.displayFormTitle();

      this.$newsServices.canUserCreateNews(this.currentSpace.id).then(canCreateNews => {
        this.canCreatNews = canCreateNews;
        this.$nextTick(() => {
          if (this.canCreatNews) {
            if (this.newsId) {
              this.initNewsComposerData(this.newsId);
            } else {
              this.initCKEditor();
              const message = localStorage.getItem('exo-activity-composer-message');
              if (message) {
                this.initCKEditorData(message);
                localStorage.removeItem('exo-activity-composer-message');
              }
              this.initDone = true;
            }
          }
          this.loading = false;
        });
      });
    });
    
    this.$nextTick(() => {
      const attachmentsComposer = JSON.parse(localStorage.getItem('exo-activity-composer-attachments'));
      if (attachmentsComposer) {
        this.news.attachments = attachmentsComposer;
        localStorage.removeItem('exo-activity-composer-attachments');
      }
    });
    $('[rel="tooltip"]').tooltip();
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
      if (CKEDITOR.instances['newsContent'] && CKEDITOR.instances['newsContent'].destroy) {
        CKEDITOR.instances['newsContent'].destroy(true);
      }
      CKEDITOR.plugins.addExternal('video','/news/js/ckeditor/plugins/video/','plugin.js');
      CKEDITOR.dtd.$removeEmpty['i'] = false;
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
        typeOfRelation: 'mention_activity_stream',
        spaceURL: self.spaceURL,
        toolbarLocation: 'top',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]; span(*)[*]{*}; span[data-atwho-at-query,data-atwho-at-value,contenteditable]; a[*];i[*]',
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
        autoGrow_minHeight: self.newsFormContentHeight,
        height: self.newsFormContentHeight,
        bodyClass: 'newsContent',
        dialog_noConfirmCancel: true,
        sharedSpaces: {
          top: 'newsTop'
        },
        on: {
          instanceReady: function(evt) {
            self.news.body = evt.editor.getData();
            $(CKEDITOR.instances['newsContent'].document.$)
              .find('.atwho-inserted')
              .each(function() {
                $(this).on('click', '.remove', function() {
                  $(this).closest('[data-atwho-at-query]').remove();
                });
              });
          },
          change: function (evt) {
            self.news.body = evt.editor.getData();
          }
        }
      });
    },
    initCKEditorData: function(message) {
      if (message) {
        const tempdiv = $('<div class=\'temp\'/>').html(message);
        tempdiv.find('a[href*="/profile"]')
          .each(function() {
            $(this).replaceWith(function() {
              return $('<span/>', {
                class: 'atwho-inserted',
                html: `<span class="exo-mention">${$(this).text()}<a data-cke-survive href="#" class="remove"><i data-cke-survive class="uiIconClose uiIconLightGray"></i></a></span>`
              }).attr('data-atwho-at-query',`@${  $(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1)}`)
                .attr('data-atwho-at-value',$(this).attr('href').substring($(this).attr('href').lastIndexOf('/')+1))
                .attr('contenteditable','false');
            });
          });
        message = `${tempdiv.html()  }&nbsp;`;
      }
      CKEDITOR.instances['newsContent'].setData(message);
    },
    displayFormTitle: function() {
      if (!this.editMode) {
        this.$newsServices.getSpaceById(this.spaceId).then(space => {
          this.newsFormTitle = this.$t('news.composer.createNews').replace('{0}', space.displayName);
        });
      } else {
        this.newsFormTitle = this.$t('news.edit.editNews');
      }
    },

    initNewsComposerData: function(newsId) {
      const self = this;
      this.$newsServices.getNewsById(newsId)
        .then(fetchedNode => {
          if (fetchedNode){
            this.news.id = fetchedNode.id;
            this.news.title = fetchedNode.title;
            this.news.summary = fetchedNode.summary;
            this.news.body = fetchedNode.body;
            this.news.pinned = fetchedNode.pinned;
            this.news.archived = fetchedNode.archived;
            this.news.spaceId = fetchedNode.spaceId;
            this.news.publicationState = fetchedNode.publicationState;
            this.initCKEditor();
            this.initCKEditorData(fetchedNode.body);

            if (fetchedNode.illustrationURL) {
              this.$newsServices.importFileFromUrl(fetchedNode.illustrationURL)
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
      if (!this.initDone) {
        return;
      }
      // if the News is being posted, no need to autosave anymore
      if (this.postingNews) {
        return;
      }
      clearTimeout(this.saveDraft);
      this.saveDraft = setTimeout(() => {
        this.savingDraft = true;
        this.draftSavingStatus = this.$t('news.composer.draft.savingDraftStatus');
        Vue.nextTick(() => {
          if (this.activityId) {
            this.doUpdateNews('draft');
          } else {
            this.saveNewsDraft();
          }
        });
      }, this.autoSaveDelay);
    },
    openDrawer() {
      if (this.$refs.publishNewsDrawer) {
        this.$refs.publishNewsDrawer.open();
      }
    },
    postNews: function () {
      if (this.news.pinned === true) {
        const confirmText = this.$t('news.broadcast.confirm');
        const captionText = this.$t('news.broadcast.action');
        const confirmButton = this.$t('news.broadcast.btn.confirm');
        const cancelButton = this.$t('news.edit.cancel');
        eXo.social.PopupConfirmation.confirm('createdPinnedNews', [{action: this.doPostNews, label: confirmButton}], captionText, confirmText, cancelButton);
      } else {
        this.doPostNews();
      }
    },
    doPostNews: function () {
      this.postingNews = true;
      // if the News draft is being saved, we have to wait until it is done before posting the News
      if (this.savingDraft) {
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

      this.news.body = this.replaceImagesURLs(this.news.body);
      const newsBody = this.replaceImagesURLs(this.getBody());
      const news = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary,
        body: this.getBody() ? newsBody : this.news.body,
        author: eXo.env.portal.userName,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        spaceId: this.spaceId,
        publicationState: 'published'
      };

      if (this.news.illustration.length > 0) {
        news.uploadId = this.news.illustration[0].uploadId;
      }

      this.$newsServices.saveNews(news).then((createdNews) => {
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
        if (attachment.id || attachment.uploadProgress === uploadedPercent) {
          news.attachments.push({
            id: attachment.id,
            uploadId: attachment.uploadId,
            name: attachment.name
          });
        }
      });

      if (this.news.id) {
        if (this.news.title || this.news.summary || this.news.body || this.news.illustration.length > 0) {
          news.id = this.news.id;
          this.$newsServices.updateNews(news)
            .then((updatedNews) => {
              if (this.news.body !== updatedNews.body) {
                // Images URLs may have been updated in body to transform temporary URLs to permanent URLs.
                // Updated URLs are extracted here and re-applied on save.
                // The goal is to avoid modifying the editor data, which can impact the editing experience.
                this.imagesURLs = this.extractImagesURLsDiffs(this.news.body, updatedNews.body);
              }
            })
            .then(() => this.$emit('draftUpdated'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        } else {
          this.$newsServices.deleteDraft(this.news.id)
            .then(() => this.$emit('draftDeleted'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
          this.news.id = null;
        }
        this.savingDraft = false;
      } else if (this.news.title || this.news.summary || this.news.body || this.news.illustration.length > 0) {
        news.publicationState = 'draft';
        this.$newsServices.saveNews(news).then((createdNews) => {
          this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus');
          this.news.id = createdNews.id;
          this.savingDraft = false;
          this.$emit('draftCreated');
        });
      } else {
        this.draftSavingStatus = '';
      }
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
        if (updatedImageURL !== originalImageURL) {
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
          if (self.activityId)
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

            return this.$newsServices.updateAndPostNewsActivity(activity);
          }
        }).then(() => {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
        }).catch (function() {
          window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
        });
      }
    },
    confirmAndUpdateNews: function() {
      if (this.news.pinned !== this.originalNews.pinned) {
        let confirmText = this.$t('news.broadcast.confirm');
        let captionText = this.$t('news.broadcast.action');
        const confirmButton = this.$t('news.broadcast.btn.confirm');
        const cancelButton = this.$t('news.broadcast.btn.cancel');
        if (this.news.pinned === false) {
          confirmText = this.$t('news.unbroadcast.confirm').replace('{0}', this.news.title);
          captionText = this.$t('news.unbroadcast.action');
        }
        const self = this;
        return new Promise(function(resolve) {
          eXo.social.PopupConfirmation.confirm('createdPinnedNews', [{action: function() { self.doUpdateNews('published').then(resolve()); }, label: confirmButton}], captionText, confirmText, cancelButton);
        });
      } else {
        return this.doUpdateNews('published');
      }
    },
    doUpdateNews: function (publicationState) {
      const newsBody = this.replaceImagesURLs(this.getBody());
      const updatedNews = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary != null ? this.news.summary : '',
        body: this.getBody() ? newsBody : this.news.body,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        publicationState: publicationState
      };
      if (this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if (this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }

      return this.$newsServices.updateNews(updatedNews).then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
    },
    goBack() {
      if ( history.length > 1) {
        history.back();
      } else {
        window.open('/', '_self');
      }
    },
    openApp() {
      this.$refs.attachmentsComponent.toggleAttachmentsDrawer();
    },
    onHideAttachmentsDrawer: function(showAttachments){
      this.showAttachmentsDrawer = showAttachments;
    },
    setUploadingCount: function(uploadingCount) {
      this.uploading = uploadingCount > 0;
    },
    getBody: function() {
      const newData = CKEDITOR.instances['newsContent'].getData();
      return newData ? newData : null;
    }
  }
};
</script>
