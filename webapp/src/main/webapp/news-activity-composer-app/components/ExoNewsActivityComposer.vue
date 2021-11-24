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
      <schedule-news-drawer
        :posting-news="postingNews"
        :news-id="newsId"
        @post-article="postNews" />
      <div class="newsComposerToolbar">
        <div id="composerToolbarInformation" class="d-flex flex-row py-2">
          <v-avatar
            height="40"
            min-height="40"
            width="40"
            min-width="40"
            max-width="40"
            size="40"
            class="mx-3 my-auto">
            <v-img src="/news/images/news.png" />
          </v-avatar>
          <div class="d-flex flex-grow-1 flex-column my-auto align-left">
            <div>
              <span class="flex-row text-truncate subtitle-1">{{ newsFormTitle }}</span>
              <span v-if="isMobile && editMode && draftSavingStatus" class="caption">({{ draftSavingStatus }})</span>
            </div>
            <span v-if="spaceDisplayName" class="flex-row subtitle-2 text-capitalize text-truncate text-light-color spaceName">{{ spaceDisplayName }}</span>
          </div>
          <div class="d-flex d-flex flex-grow-0 my-auto">
            <span v-if="!isMobile" class="my-auto me-4 flex-shrink-0">{{ draftSavingStatus }}</span>
            <div v-if="canUpdateNewVisibilty && !isMobile">
              <v-select
                ref="selectVisibility"
                v-model="news.draftVisible"
                v-if="isNewsAuthor"
                :items="items"
                item-value="value"
                label="text"
                item-text="text"
                hide-selected
                class="selectMenuClass mr-7"
                color="black"
                item-color="black"
                attach
                solo
                dense
                @change="updateDraftVisibility()">
                <template v-slot:selection="{ item }">
                  <v-icon
                    v-if="!item.value"
                    size="16"
                    color="black"
                    class="mr-3 mb-1">
                    mdi-lock
                  </v-icon>
                  <v-icon
                    v-else
                    size="16"
                    color="black"
                    class="mr-3 mb-1">
                    mdi-account-edit
                  </v-icon>
                  {{ item.text }}
                </template>
                <template slot="item" slot-scope="{ item }">
                  <v-icon
                    v-if="!item.value"
                    size="16"
                    color="black"
                    class="mr-3 mb-1">
                    mdi-lock
                  </v-icon>
                  <v-icon
                    v-else
                    size="16"
                    color="black"
                    class="mr-3 mb-1">
                    mdi-account-edit
                  </v-icon>
                  {{ item.text }}
                </template>
              </v-select>
              <div v-else class="d-flex mr-5 mt-1">
                <v-icon
                  v-if="!this.news.draftVisible"
                  size="16"
                  color="black"
                  class="mr-2">
                  mdi-lock
                </v-icon>
                <v-icon
                  v-else
                  size="16"
                  color="black"
                  class="mr-2">
                  mdi-account-edit
                </v-icon>
                <div
                  id="newsDraftVisible1"
                  class="font-weight-bold pa-0 mt-1"
                  disabled
                  v-text="newsLabel"></div>
              </div>
            </div>
            <div v-if="canUpdateNewVisibilty && isMobile" class="d-flex mr-2 mt-1">
              <v-btn
                primary
                icon
                text
                :disabled="!isNewsAuthor"
                @click="$root.$emit('open-draft-visibility', news.draftVisible)">
                <v-icon
                  v-if="!this.news.draftVisible"
                  size="20"
                  color="black"
                  class="mr-1">
                  mdi-lock
                </v-icon>
                <v-icon
                  v-else
                  size="20"
                  color="black"
                  class="mr-1">
                  mdi-account-edit
                </v-icon>
              </v-btn>
            </div>
            <v-btn
              v-show="!editMode"
              id="newsPost"
              :loading="postingNews"
              :disabled="postDisabled || postingNews"
              elevation="0"
              class="btn btn-primary my-auto me-4"
              @click="newsActions">
              {{ $t("news.composer.post") }}
            </v-btn>
            <div v-if="!isMobile && editMode">
              <div class="d-flex flex-row me-2">
                <v-btn
                  id="newsEdit"
                  :disabled="updateDisabled"
                  class="btn btn-primary"
                  @click.prevent="updateNews(false)">
                  {{ $t("news.edit.update") }}
                </v-btn>
                <v-btn
                  id="newsUpdateAndPost"
                  :disabled="news.archived ? true: updateDisabled"
                  :class="[news.archived ? 'unauthorizedPublish' : '']"
                  class="btn ms-2 me-2"
                  @click.prevent="updateNews(true)">
                  {{ $t("news.edit.update.post") }}
                </v-btn>
                <v-btn class="btn me-2" @click="goBack">
                  {{ $t("news.composer.btn.cancel") }}
                </v-btn>
              </div>
            </div>
            <div v-else-if="isMobile && editMode" class="d-flex flex-row me-2">
              <v-btn
                id="newsEdit"
                :disabled="updateDisabled"
                class="btn btn-primary"
                @click.prevent="updateNews(false)">
                {{ $t("news.edit.update") }}
              </v-btn>
            </div>
          </div>
        </div>
        <div id="newsTop"></div>
      </div>

      <form id="newsForm" class="newsForm">
        <div class="newsFormInput">
          <div id="newsFormAttachment" class="newsFormAttachment">
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
          <v-alert
            v-if="news.draftUpdaterUserName !== currentUser && news.publicationState === 'draft' && activityId"
            dismissible
            border="left"
            elevation="2"
            colored-border
            class="warningAlert">
            <i class="fas fa-exclamation-circle exclamationIcon"></i>
            <span class="warningText">{{ draftWarningText }}</span>
          </v-alert>
        </div>
      </form>
      <v-btn
        v-if="!isMobile"
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
      <exo-news-notification-alerts name="event-form" />
      <exo-news-draft-visibility-mobile
        ref="selectVisibilityDialog"
        :items="items" />
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
const USER_TIMEZONE_ID = new window.Intl.DateTimeFormat().resolvedOptions().timeZone;
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
  },
  data() {
    return {
      news: {
        id: '',
        activityId: '',
        author: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        attachments: [],
        spaceId: '',
        pinned: false,
        archived: false,
        draftVisible: false,
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
      spaceURL: null,
      currentUser: eXo.env.portal.userName,
      fullDateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
      items: [
        {key: 'private',value: false, text: this.$t('news.composer.private'), align: 'center', sortable: false},
        {key: 'shared',value: true, text: this.$t('news.composer.shared'), align: 'center', sortable: false},
      ],
      canScheduleNews: false,
      scheduleMode: '',
      switchView: false,
      spaceDisplayName: '',
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
                 && this.getString(this.news.body) === this.getString(this.originalNews.body)
                 && this.news.pinned === this.originalNews.pinned
                 && this.news.publicationState !== 'draft') {
        return true;
      }

      return false;
    },
    draftWarningText() {
      return this.$t('news.drafts.warning.youAreEditingDraft').replace('{0}', this.news.draftUpdaterDisplayName).replace('{1}', this.formatDate(this.news.draftUpdateDate.time));
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    canUpdateNewVisibilty() {
      return !this.editMode && this.news.id !== '' && this.currentSpace && this.currentSpace.redactorsCount > 0;
    },
    isNewsAuthor() {
      return this.news.author === this.currentUser;
    },
    newsLabel() {
      return  this.news.draftVisible ?  this.$t('news.composer.shared') : this.$t('news.composer.private');
    },
  },
  watch: {
    'news.title': function() {
      if (this.news.title !== this.originalNews.title) {
        this.autoSave();
      } },
    'news.draftVisible': function() {
      this.autoSave();
    },
    'news.summary': function() {
      if (this.news.summary !== this.originalNews.summary) {
        this.autoSave();
      } },
    'news.body': function() {
      if (this.getContent(this.news.body) !== this.getContent(this.originalNews.body)) {
        this.autoSave();
      } },
    'news.attachments': function() {
      if (this.initDone && this.news.attachments !== this.originalNews.attachments) {
        this.attachmentsChanged = true;
        this.autoSave();
      }
    },
    'news.illustration': function() {
      if (this.initIllustrationDone) {
        this.illustrationChanged = true;
        this.autoSave();
      }
    }
  },
  mounted() {
    const elementNewTop = document.getElementById('newsTop');
    elementNewTop.classList.add('darkComposerEffect');
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
              this.setToolBarEffect();
              this.news.draftVisible = false;
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
      this.$newsServices.canScheduleNews(this.currentSpace.id).then(canScheduleNews => {
        this.canScheduleNews = canScheduleNews;
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
  created() {
    document.addEventListener('switch-view-plugins', () => {
      this.changeView();
    });
    document.addEventListener('attach-file-plugins', () => {
      this.openApp();
    });
    this.$root.$on('update-visibility', this.updateVisibility);
  },
  methods: {
    initCKEditor: function() {
      if (CKEDITOR.instances['newsContent'] && CKEDITOR.instances['newsContent'].destroy) {
        CKEDITOR.instances['newsContent'].destroy(true);
      }
      CKEDITOR.plugins.addExternal('video','/news/js/ckeditor/plugins/video/','plugin.js');
      CKEDITOR.plugins.addExternal('switchView','/news/js/ckeditor/plugins/switchView/','plugin.js');
      CKEDITOR.plugins.addExternal('attachFile','/news/js/ckeditor/plugins/attachment/','plugin.js');
      CKEDITOR.dtd.$removeEmpty['i'] = false;
      let extraPlugins = 'sharedspace,simpleLink,selectImage,suggester,font,justify,widget,video,switchView,attachFile';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }
      CKEDITOR.addCss('.cke_editable { font-size: 14pt; font-family: Helvetica, regular, sans-serif; }');
      // this line is mandatory when a custom skin is defined

      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      const mobile = this.isMobile;
      const newsToolbar = [];
      if (this.isMobile) {
        newsToolbar.push(
          { name: 'switchView', items: ['switchView'] },
          { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'RemoveFormat'] },
          { name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'Blockquote' ] },
          { name: 'links', items: [ 'selectImage', 'Video'] },
          { name: 'attachFile', items: ['attachFile'] },
        );
      } else {
        newsToolbar.push(
          { name: 'format', items: ['Format'] },
          { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'RemoveFormat'] },
          { name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'Blockquote' ] },
          { name: 'fontsize', items: ['FontSize'] },
          { name: 'colors', items: [ 'TextColor' ] },
          { name: 'align', items: [ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },
          { name: 'links', items: [ 'simpleLink', 'selectImage', 'Video'] },
        );
      }

      $('textarea#newsContent').ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image,confirmBeforeReload,maximize,resize,embedsemantic',
        allowedContent: true,
        typeOfRelation: 'mention_activity_stream',
        spaceURL: self.spaceURL,
        toolbarLocation: 'top',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]; span(*)[*]{*}; span[data-atwho-at-query,data-atwho-at-value,contenteditable]; a[*];i[*]',
        removeButtons: 'Subscript,Superscript,Cut,Copy,Paste,PasteText,PasteFromWord,Undo,Redo,Scayt,Unlink,Anchor,Table,HorizontalRule,SpecialChar,Maximize,Source,Strike,Outdent,Indent,BGColor,About',
        toolbar: newsToolbar,
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
            if (mobile) {
              const numerotationGroupButton = document.getElementById('cke_16');
              const attachMediaButton = document.getElementById('cke_20');
              const attachFileButton = document.getElementById('cke_23');
              numerotationGroupButton.style.borderRight = 'none';
              attachMediaButton.style.display = 'none';
              attachFileButton.style.display = 'none';
              const spanBadge = document.createElement('span');
              spanBadge.setAttribute('class','badge');
              spanBadge.setAttribute('id','badge');
              spanBadge.innerHTML = '0';
              attachFileButton.appendChild(spanBadge);
            }
            self.news.body = evt.editor.getData();
            $(CKEDITOR.instances['newsContent'].document.$)
              .find('.atwho-inserted')
              .each(function() {
                $(this).on('click', '.remove', function() {
                  $(this).closest('[data-atwho-at-query]').remove();
                });
              });
            window.setTimeout(() => self.setFocus(), 50);
          },
          change: function (evt) {
            self.news.body = evt.editor.getData();
            self.autoSave();
          },
          drop: function (evt) {
            window.setTimeout(() => {
              self.news.body = evt.editor.getData();
              self.autoSave();
            }, 1000);
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
      this.$newsServices.getSpaceById(this.spaceId).then(space => {
        if (this.isMobile && !this.editMode) {
          this.newsFormTitle = `${this.$t('news.composer.mobile.createNews')}`;
          this.spaceDisplayName = space.displayName;
        } else if (!this.isMobile && !this.editMode) {
          this.newsFormTitle = this.newsFormTitle = this.$t('news.composer.createNews').replace('{0}', space.displayName);
        } else {
          this.newsFormTitle = this.$t('news.edit.editNews');
          this.spaceDisplayName = space.displayName;
        }
      });
    },

    initNewsComposerData: function(newsId) {
      const self = this;
      this.$newsServices.getNewsById(newsId, true)
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
            this.news.activityId = fetchedNode.activityId;
            this.news.updater = fetchedNode.updater;
            this.news.draftUpdaterDisplayName = fetchedNode.draftUpdaterDisplayName;
            this.news.draftUpdaterUserName = fetchedNode.draftUpdaterUserName;
            this.news.draftUpdateDate = fetchedNode.draftUpdateDate;
            this.news.author = fetchedNode.author;
            this.news.draftVisible = fetchedNode.draftVisible;
            this.news.activityPosted = fetchedNode.activityPosted;
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
            this.doUpdateNews('draft', false);
          } else {
            this.saveNewsDraft();
          }
        });
      }, this.autoSaveDelay);
    },
    newsActions() {
      if (this.canScheduleNews) {
        this.scheduleMode = 'postScheduledNews';
        this.$root.$emit('open-schedule-drawer', this.scheduleMode);
      } else {
        this.postNews();
      }
    },
    postNews: function (schedulePostDate, postArticleMode, publish, isActivityPosted) {
      this.news.activityPosted = isActivityPosted;
      this.news.pinned = publish;
      this.doPostNews(schedulePostDate);
    },
    doPostNews: function (schedulePostDate) {
      this.postingNews = true;
      // if the News draft is being saved, we have to wait until it is done before posting the News
      if (this.savingDraft) {
        this.$on('draftCreated', this.saveNews);
        this.$on('draftUpdated', this.saveNews);
      } else {
        this.saveNews(schedulePostDate);
      }
    },
    saveNews: function (schedulePostDate) {
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
        author: this.currentUser,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        draftVisible: this.news.draftVisible,
        spaceId: this.spaceId,
        publicationState: 'published',
        schedulePostDate: null,
        timeZoneId: null,
        activityPosted: this.news.activityPosted,
      };

      if (schedulePostDate != null){
        news.publicationState ='staged';
        news.schedulePostDate = schedulePostDate;
        news.timeZoneId = USER_TIMEZONE_ID;
      }

      if (this.news.illustration.length > 0) {
        news.uploadId = this.news.illustration[0].uploadId;
      }
      if (news.publicationState ==='staged') {
        this.$newsServices.scheduleNews(news).then((scheduleNews) => {
          if (scheduleNews) {
            window.location.href = scheduleNews.url;
          }
        });
      } else {
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
      }
    },
    saveNewsDraft: function () {
      const news = {
        title: this.news.title,
        summary: this.news.summary,
        body: this.replaceImagesURLs(this.news.body),
        author: this.currentUser,
        attachments: [],
        pinned: false,
        draftVisible: this.news.draftVisible,
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
          this.$newsServices.updateNews(news, false)
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
          if (!this.newsId) {
            this.newsId = createdNews.id;
          }
          this.news.author = createdNews.author;
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
    updateNews: function (post) {
      this.confirmAndUpdateNews(post).then(() => {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
      }).catch (function() {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${this.activityId}`;
      });
    },
    confirmAndUpdateNews: function(post) {
      return this.doUpdateNews('published', post);
    },
    doUpdateNews: function (publicationState, post) {
      const newsBody = this.replaceImagesURLs(this.getBody());
      const updatedNews = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary != null ? this.news.summary : '',
        body: this.getBody() ? newsBody : this.news.body,
        attachments: this.news.attachments,
        pinned: this.news.pinned,
        publicationState: publicationState,
        draftVisible: this.draftVisible,
        activityPosted: this.news.activityPosted,
      };
      if (this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if (this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }

      return this.$newsServices.updateNews(updatedNews, post).then((createdNews) => {
        if (this.news.body !== createdNews.body) {
          this.imagesURLs = this.extractImagesURLsDiffs(this.news.body, createdNews.body);
        }
      }).then(() => this.$emit('draftUpdated'))
        .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
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
    onHideAttachmentsDrawer: function(){
      const spanBadge = document.getElementById('badge');
      spanBadge.innerHTML = String(this.news.attachments.length);
    },
    setUploadingCount: function(uploadingCount) {
      this.uploading = uploadingCount > 0;
    },
    getBody: function() {
      const newData = CKEDITOR.instances['newsContent'].getData();
      return newData ? newData : null;
    },
    formatDate(time) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(time),this.fullDateFormat, eXo.env.portal.language);
    },
    getContent(body) {
      return new DOMParser().parseFromString(body, 'text/html').documentElement.textContent.replace(/&nbsp;/g, '').trim();
    },
    setFocus() {
      if (CKEDITOR.instances['newsContent']) {
        CKEDITOR.instances['newsContent'].status = 'ready';
        window.setTimeout(() => {
          this.$nextTick().then(() => CKEDITOR.instances['newsContent'].focus());
        }, 200);
      }
    },
    setToolBarEffect() {
      const element = CKEDITOR.instances['newsContent'] ;
      const elementNewTop = document.getElementById('newsTop');
      element.on('contentDom', function () {
        this.document.on('click', function(){
          elementNewTop.classList.add('darkComposerEffect');
        });
      });
      element.on('contentDom', function () {
        this.document.on('keyup', function() {
          elementNewTop.classList.add('darkComposerEffect');
        });
      });
      $('#newsSummary').parent().click(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#newsSummary').parent().keyup(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#newsTitle').parent().click(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#newsTitle').parent().keyup(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#composerToolbarInformation').click(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#composerToolbarInformation').keyup(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#newsFormAttachment').parent().click(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
      $('#newsFormAttachment').parent().keyup(() => {
        elementNewTop.classList.remove('darkComposerEffect');
        elementNewTop.classList.add('greyComposerEffect');
      });
    },
    getString(body) {
      return new DOMParser().parseFromString(body, 'text/html').documentElement.textContent.replace(/&nbsp;/g, '').trim();
    },
    updateDraftVisibility(){
      this.$root.$emit('update-draft-visibility', this.news.draftVisible);
    },
    updateVisibility(visibility){
      if (visibility !== this.news.draftVisible) {
        this.news.draftVisible = visibility;
        this.updateDraftVisibility();
      }
    },
    changeView() {
      const elementNewTop = document.getElementById('newsTop');
      elementNewTop.classList.remove('greyComposerEffect');
      elementNewTop.classList.add('darkComposerEffect');
      const switchViewButton = document.getElementById('cke_9');
      const fontStyleGroupButton = document.getElementById('cke_11');
      const numerotationGroupButton = document.getElementById('cke_16');
      const attachMediaButton = document.getElementById('cke_20');
      const attachFileButton = document.getElementById('cke_23');
      numerotationGroupButton.style.borderRight = 'none';
      attachMediaButton.style.display = 'none';
      if (!this.switchView) {
        switchViewButton.style.borderRight = 'none';
        fontStyleGroupButton.style.display = 'none';
        numerotationGroupButton.style.display = 'none';
        attachMediaButton.style.display = 'initial';
        attachMediaButton.style.borderRight = 'none';
        attachFileButton.style.display = 'initial';
        this.switchView = true;
      } else {
        switchViewButton.style.display = 'initial';
        fontStyleGroupButton.style.display = 'initial';
        numerotationGroupButton.style.display = 'initial';
        attachFileButton.style.display = 'none';
        this.switchView = false;
      }
    }
  }
};
</script>
