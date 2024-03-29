<!--
Copyright (C) 2021 eXo Platform SAS.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
-->
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
      v-show="canCreateNews && !loading"
      id="newsActivityComposer"
      class="newsComposer">
      <schedule-news-drawer
        :posting-news="postingNews"
        :news-id="newsId"
        @post-article="postNews" />
      <div class="newsComposerToolbar">
        <div id="composerToolbarInformation" class="d-flex flex-row py-2">
          <v-btn 
            v-if="isMobile"
            icon
            fab
            class="my-auto"
            @click="close()">
            <v-icon> mdi-arrow-left </v-icon>
          </v-btn>
          <v-avatar
            height="40"
            min-height="40"
            width="40"
            min-width="40"
            max-width="40"
            size="40"
            :class="isMobile ? 'me-3' : 'mx-3'"
            class="my-auto">
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
            <v-btn
              v-show="!editMode"
              id="newsPost"
              :loading="postingNews"
              :disabled="postDisabled || postingNews"
              :aria-label="$t('news.composer.post')"
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
                <v-tooltip bottom :disabled="!news.activityPosted">
                  <template #activator="{ on, attrs }">
                    <span v-on="on">
                      <v-btn
                        id="newsUpdateAndPost"
                        :disabled="news.archived || news.activityPosted ? true: updateDisabled"
                        :class="[news.archived ? 'unauthorizedPublish' : '']"
                        class="btn ms-2 me-2"
                        v-bind="attrs"
                        v-on="on"
                        @click.prevent="updateNews(true)">
                        {{ $t("news.edit.update.post") }}
                      </v-btn>
                    </span>
                  </template>
                  <span>{{ $t("news.edit.disable.update.postButton") }}</span>
                </v-tooltip>
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

      <form
        id="newsForm"
        class="newsForm"
        onsubmit="event.preventDefault(); return false;">
        <div class="newsFormInput singlePageApplication transparent">
          <div class="white">
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
            <div class="formInputGroup newsSummary">
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
        </div>
      </form>
    </div>

    <div v-show="(!canCreateNews && !loading) || unAuthorizedAccess" class="newsComposer">
      <div class="articleNotFound">
        <i class="iconNotFound"></i>
        <h3 class="restrictedAction">{{ $t('news.details.restricted') }}</h3>
      </div>
    </div>
  </v-app>
</template>

<script>
import autosize from 'autosize';
const USER_TIMEZONE_ID = new window.Intl.DateTimeFormat().resolvedOptions().timeZone;
const UNAUTHORIZED_CODE = 401;
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
    newsType: {
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
        targets: [],
        spaceId: '',
        published: false,
        archived: false,
        audience: null,
      },
      originalNews: {
        id: '',
        activityId: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        spaceId: '',
        published: false,
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
      canCreateNews: false,
      loading: true,
      currentSpace: {},
      spaceURL: null,
      spaceGroupId: null,
      currentUser: eXo.env.portal.userName,
      fullDateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
      canScheduleNews: false,
      scheduleMode: '',
      switchView: false,
      spaceDisplayName: '',
      unAuthorizedAccess: false,
      newsBody: null,
      desktopToolbar: null,
      oembedMinWidth: 300,
      spaceUrl: null,
    };
  },
  computed: {
    editMode: function() {
      return this.activityId && this.activityId !== '';
    },
    isEmptyNewsBody() {
      return this.news.body === '' || Array.from(new DOMParser().parseFromString(this.news.body, 'text/html').body.childNodes).every(node => (node.nodeName === 'P' && !node.textContent.trim() && node.children.length === 0) || (node.nodeType === Node.TEXT_NODE && !node.textContent.trim()));
    },
    isSameNewsBody() {
      // check if news body composed only by text and then compare only the text content
      const containOnlyText = Array.from(new DOMParser().parseFromString(this.news.body, 'text/html').body.childNodes).every(node => (node.nodeName === 'P' && node.textContent.trim()) || node.nodeType === Node.TEXT_NODE)
          && Array.from(new DOMParser().parseFromString(this.originalNews.body, 'text/html').body.childNodes).every(node => (node.nodeName === 'P' && node.textContent.trim()) || node.nodeType === Node.TEXT_NODE);
      if (containOnlyText) {
        return this.getString(this.news.body) === this.getString(this.originalNews.body);
      }
      // get nodes and remove all empty paragraph elements then compare.
      const originalNewsBody = Array.from(new DOMParser().parseFromString(this.originalNews.body, 'text/html').body.childNodes).filter(node => !(node.nodeName === 'P' && !node.textContent.trim() && !node.children.length > 0));
      let index = originalNewsBody.length - 1;
      //remove all empty text nodes at the end.
      while (index >= 0 && originalNewsBody[index].nodeType === Node.TEXT_NODE && !originalNewsBody[index].textContent.trim() ) {
        originalNewsBody.pop();
        index--;
      }
      const currentNewsBody = Array.from(new DOMParser().parseFromString(this.news.body, 'text/html').body.childNodes).filter(node => !(node.nodeName === 'P' && !node.textContent.trim() && !node.children.length > 0));
      index = currentNewsBody.length - 1;
      while (index >= 0 && currentNewsBody[index].nodeType === Node.TEXT_NODE && !currentNewsBody[index].textContent.trim() ) {
        currentNewsBody.pop();
        index--;
      }
      // isEqualNode : Two nodes are equal when they have the same type and the same content.
      return  originalNewsBody.length === currentNewsBody.length && originalNewsBody.every((node, index) => node.isEqualNode(currentNewsBody[index]));
    },
    postDisabled: function () {
      return !this.news.title || !this.news.title.trim() || this.isEmptyNewsBody;
    },
    updateDisabled: function () {
      if (!this.news.title.trim() || this.isEmptyNewsBody ) {
        return true;
      }
      // disable update button nothing has changed
      if (!this.illustrationChanged
                 && this.news.title === this.originalNews.title
                 && this.news.summary === this.originalNews.summary
                 && this.isSameNewsBody
                 && this.news.published === this.originalNews.published
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
  },
  watch: {
    'news.title': function() {
      if (this.news.title !== this.originalNews.title) {
        this.autoSave();
      } 
    },
    'news.summary': function() {
      if (this.news.summary !== this.originalNews.summary) {
        this.autoSave();
      } 
    },
    'news.body': function() {
      if (this.getContent(this.news.body) !== this.getContent(this.originalNews.body)) {
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
      this.spaceGroupId = this.currentSpace.groupId;
      this.displayFormTitle();

      this.$newsServices.canUserCreateNews(this.currentSpace.id).then(canCreateNews => {
        this.canCreateNews = canCreateNews || this.newsId;
        this.$nextTick(() => {
          if (this.canCreateNews) {
            if (this.newsId) {
              this.initNewsComposerData(this.newsId);
            } else {
              this.initCKEditor();
              this.setToolBarEffect();
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
    this.$root.$on('news-space-url', (spaceUrl)=>this.spaceUrl = spaceUrl);
  },
  methods: {
    initCKEditor: function() {
      if (CKEDITOR.instances['newsContent'] && CKEDITOR.instances['newsContent'].destroy) {
        CKEDITOR.instances['newsContent'].destroy(true);
      }
      CKEDITOR.dtd.$removeEmpty['i'] = false;

      CKEDITOR.on('dialogDefinition', function (e) {
        if (e.data.name === 'link') {
          const informationTab = e.data.definition.getContents('target');
          const targetField = informationTab.get('linkTargetType');
          targetField['default'] = '_self';
          targetField.items = targetField.items.filter(t => ['_self', '_blank'].includes(t[1]));
        }
      });

      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      const mobile = this.isMobile;

      $('textarea#newsContent').ckeditor({
        customConfig: `${eXo.env.portal.context}/${eXo.env.portal.rest}/richeditor/configuration?type=news&v=${eXo.env.client.assetsVersion}`,
        allowedContent: true,
        typeOfRelation: 'mention_activity_stream',
        spaceURL: self.spaceURL,
        spaceGroupId: self.spaceGroupId,
        imagesDownloadFolder: 'DRIVE_ROOT_NODE/News/images',
        toolbarLocation: 'top',
        extraAllowedContent: 'table[summary];img[style,class,src,referrerpolicy,alt,width,height];span(*)[*]{*}; span[data-atwho-at-query,data-atwho-at-value,contenteditable]; a[*];i[*];',
        removeButtons: '',
        bodyClass: 'newsContent',
        dialog_noConfirmCancel: true,
        indentBlock: {
          offset: 40,
          unit: 'px'
        },
        copyFormatting_allowedContexts: true,
        colorButton_enableMore: true,
        enterMode: CKEDITOR.ENTER_P,
        shiftEnterMode: CKEDITOR.ENTER_BR,
        sharedSpaces: {
          top: 'newsTop'
        },
        on: {
          instanceReady: function(evt) {
            CKEDITOR.instances['newsContent'].removeMenuItem('selectImageItem');
            if (mobile) {
              const paragraph = document.getElementById('cke_20');
              const linkSection = document.getElementById('cke_25');
              const blockSection = document.getElementById('cke_28');
              paragraph.style.borderRight = 'none';
              linkSection.style.display = 'none';
              blockSection.style.display = 'none';
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
          afterInsertHtml: function (evt) {
            self.newsBody = evt.editor.getData();
          },
          fileUploadResponse: function() {
            self.news.body = self.newsBody;
            self.autoSave();
          },
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
      this.$newsServices.getNewsById(newsId, true, this.newsType)
        .then(fetchedNode => {
          if (fetchedNode === UNAUTHORIZED_CODE){
            this.unAuthorizedAccess = true;
            self.initDone = true;
          } else {
            this.news.id = fetchedNode.id;
            this.news.title = fetchedNode.title;
            this.news.summary = fetchedNode.summary;
            this.news.body = fetchedNode.body;
            this.news.published = fetchedNode.published;
            this.news.archived = fetchedNode.archived;
            this.news.spaceId = fetchedNode.spaceId;
            this.news.publicationState = fetchedNode.publicationState;
            this.news.activityId = fetchedNode.activityId;
            this.news.updater = fetchedNode.updater;
            this.news.draftUpdaterDisplayName = fetchedNode.draftUpdaterDisplayName;
            this.news.draftUpdaterUserName = fetchedNode.draftUpdaterUserName;
            this.news.draftUpdateDate = fetchedNode.draftUpdateDate;
            this.news.activityPosted = fetchedNode.activityPosted;
            this.news.audience = fetchedNode.audience;
            this.initCKEditor();
            this.initCKEditorData(this.getContentToEdit(fetchedNode.body));

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
            this.originalNews = JSON.parse(JSON.stringify(this.news));
            Vue.nextTick(() => {
              autosize.update(document.querySelector('#newsSummary'));
              self.initDone = true;
            });
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
    postNews: function (schedulePostDate, postArticleMode, publish, isActivityPosted, selectedTargets, selectedAudience) {
      this.news.activityPosted = isActivityPosted;
      this.news.published = publish;
      this.news.targets = selectedTargets;
      if (selectedAudience !== null) {
        this.news.audience = selectedAudience === this.$t('news.composer.stepper.audienceSection.allUsers') ? 'all' : 'space';
      }
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
        published: this.news.published,
        targets: this.news.targets,
        spaceId: this.spaceId,
        publicationState: 'published',
        schedulePostDate: null,
        timeZoneId: null,
        activityPosted: this.news.activityPosted,
        audience: this.news.audience,
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
            history.replaceState(null,'',scheduleNews.spaceUrl);
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
            history.replaceState(null,'',this.spaceUrl);
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${createdNewsActivity}`;
          } else {
            window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}`;
          }
        });
      }
    },
    saveNewsDraft: function () {
      this.news.body = this.replaceImagesURLs(this.news.body);
      const newsBody = this.replaceImagesURLs(this.getBody());
      const news = {
        title: this.news.title,
        summary: this.news.summary,
        body: this.getBody() ? newsBody : this.news.body,
        author: this.currentUser,
        published: false,
        spaceId: this.spaceId,
        publicationState: ''
      };
      if (this.news.illustration.length > 0) {
        news.uploadId = this.news.illustration[0].uploadId;
      } else {
        news.uploadId = '';
      }


      if (this.news.id) {
        if (this.news.title || this.news.summary || this.news.body || this.news.illustration.length > 0) {
          news.id = this.news.id;
          this.$newsServices.updateNews(news, false, this.newsType)
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
      this.news.published = false;
      CKEDITOR.instances['newsContent'].setData('');
      this.news.illustration = [];
    },
    updateNews: function (post) {
      this.confirmAndUpdateNews(post).then(() => {
        history.replaceState(null,'',this.spaceUrl);
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}`;
      }).catch (function() {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${this.activityId}`;
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
        published: this.news.published,
        publicationState: publicationState,
        activityPosted: this.news.activityPosted,
        audience: this.news.audience,
      };
      if (this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if (this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }

      return this.$newsServices.updateNews(updatedNews, post).then((createdNews) => {
        this.spaceUrl = createdNews.spaceUrl;
        if (this.news.body !== createdNews.body) {
          this.imagesURLs = this.extractImagesURLsDiffs(this.news.body, createdNews.body);
        }
      }).then(() => this.$emit('draftUpdated'))
        .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
    },
    close() {
      window.close();
    },
    goBack() {
      if ( history.length > 1) {
        history.back();
      } else {
        window.open('/', '_self');
      }
    },
    getContentToEdit(content) {
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      this.restoreOembed(docElement);
      this.restoreUnHighlightedCode(docElement);
      return docElement?.children[1].innerHTML;
    },
    restoreUnHighlightedCode(documentElement) {
      documentElement.querySelectorAll('code.hljs').forEach(code => {
        code.innerHTML = code.innerText;
        code.classList.remove('hljs');
      });
    },
    restoreOembed(documentElement) {
      documentElement.querySelectorAll('div.embed-wrapper').forEach(wrapper => {
        const oembed = document.createElement('oembed');
        oembed.innerHTML = wrapper.dataset.url;
        wrapper.replaceWith(oembed);
      });
    },
    preserveEmbedded(body, documentElement) {
      const iframes = body.querySelectorAll('[data-widget="embedSemantic"] div iframe');
      if (iframes.length) {
        documentElement.querySelectorAll('oembed').forEach((oembed, index) => {
          const wrapper = document.createElement('div');
          wrapper.dataset.url = decodeURIComponent(oembed.innerHTML);
          wrapper.innerHTML = iframes[index]?.parentNode?.innerHTML;
          const width = iframes[index]?.parentNode?.offsetWidth;
          const height = iframes[index]?.parentNode?.offsetHeight;
          const aspectRatio = width / height;
          const minHeight = parseInt(this.oembedMinWidth) / aspectRatio;
          const style = `
            min-height: ${minHeight}px;
            min-width: ${this.oembedMinWidth}px;
            width: 100%;
            margin-bottom: 10px;
            aspect-ratio: ${aspectRatio};
          `;
          wrapper.setAttribute('style', style);
          wrapper.setAttribute('class', 'embed-wrapper d-flex position-relative ml-auto mr-auto');
          oembed.replaceWith(wrapper);
        });
      }
    },
    preserveHighlightedCode(body, documentElement) {
      const codes = body.querySelectorAll('pre[data-widget="codeSnippet"] code');
      if (codes.length) {
        documentElement.querySelectorAll('code').forEach((code, index) => {
          code.innerHTML = codes[index]?.innerHTML;
          code.setAttribute('class', codes[index]?.getAttribute('class'));
        });
      }
    },
    getBody: function () {
      const domParser = new DOMParser();
      const newData = CKEDITOR.instances['newsContent'].getData();
      const body = CKEDITOR.instances['newsContent'].document.getBody().$;
      const documentElement = domParser.parseFromString(newData, 'text/html').documentElement;
      this.preserveEmbedded(body, documentElement);
      this.preserveHighlightedCode(body, documentElement);
      return documentElement?.children[1].innerHTML;
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
    changeView() {
      const elementNewTop = document.getElementById('newsTop');
      elementNewTop.classList.remove('greyComposerEffect');
      elementNewTop.classList.add('darkComposerEffect');
      const basicStyles = document.getElementById('cke_14');
      const paragraph = document.getElementById('cke_20');
      const switchViewButton = document.getElementById('cke_12');
      const linkSection = document.getElementById('cke_25');
      const blockSection = document.getElementById('cke_28');
      if (this.switchView) {
        basicStyles.style.display = 'inline';
        paragraph.style.display = 'inline';
        switchViewButton.style.borderRight = '1px solid #b6b6b6';
        linkSection.style.display = 'none';
        blockSection.style.display = 'none';
        this.switchView = false;
      } else {
        basicStyles.style.display = 'none';
        paragraph.style.display = 'none';
        switchViewButton.style.borderRight = 'none';
        switchViewButton.style.display = 'initial';
        linkSection.style.display = 'initial';
        blockSection.style.display = 'initial';
        this.switchView = true;
      }
    }
  }
};
</script>
