<template>
  <div class="newsEditComponent">
    <a v-show="!showEditNews" id="newsEditButton" :data-original-title="$t('news.edit.edit')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="clickOnEditButton">
      <i class="uiIconEdit"></i>
    </a>
    <div id="newsActivityEditComposer" :class="{ completeWidth: showEditNews }" class="newsComposer extended" >
      <div v-show="showEditNews" class="newsComposerActions">
        <div class="newsFormButtons">
          <div class="newsFormLeftActions">
            <button id="cancelEdit" class="btn cancelEdit" @click.prevent="cancelEdit"> {{ $t("news.edit.cancel") }}
            </button>
            <div v-if="showPinInput" class="pinArticleContent">
              <span class="uiCheckbox">
                <input id="pinArticle" v-model="news.pinned" type="checkbox" class="checkbox ">
                <span class="pinArticleLabel">{{ $t("news.composer.pinArticle") }}</span>
              </span>
            </div>
          </div>
          <div class="newsFormRightActions">
            <button id="newsEdit" :disabled="updateDisabled" class="btn btn-primary" @click.prevent="updateNews"> {{ $t("news.edit.update") }}
            </button>
            <button id="newsUpdateAndPost" :disabled="updateDisabled" class="btn" @click.prevent="updateAndPostNews"> {{ $t("news.edit.update.post") }}
            </button>
          </div>
        </div>
        <div id="newsTop"></div>
      </div>
      <form v-show="showEditNews" id="newsForm" class="newsForm">
        <div class="newsFormInput">
          <div class="newsFormAttachment">
            <div class="control-group attachments">
              <div class="controls">
                <exo-file-drop v-model="news.illustration"/>
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
                      v-model="news.body"
                      :placeholder="newsFormContentPlaceholder"
                      class="newsFormInput"
                      name="newsContent">
            </textarea>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';
import autosize from 'autosize';

export default {
  props: {
    activityId: {
      type: String,
      required: true
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
        spaceId: '',
        pinned: false,
      },
      originalNews: {
        id: '',
        activityId: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        spaceId: '',
        pinned: false,
      },
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      titleMaxLength: 150,
      summaryMaxLength: 1000,
      newsFormContentHeight: 330,
      newsFormTitlePlaceholder: `${this.$t('news.composer.placeholderTitleInput')}*`,
      newsFormSummaryPlaceholder: this.$t('news.composer.placeholderSummaryInput'),
      newsFormContentPlaceholder: `${this.$t('news.composer.placeholderContentInput')}*`,
      showEditNews: false,
      illustrationChanged: false,
    };
  },
  computed: {
    updateDisabled: function () {
      const emptyMandatoryFields = !this.news.title || !this.news.title.trim() || !this.news.body || !this.news.body.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim();
      const noUpdatedField = !this.illustrationChanged && this.news.title === this.originalNews.title && this.news.summary === this.originalNews.summary && this.news.body === this.originalNews.body && this.news.pinned === this.originalNews.pinned;
      return emptyMandatoryFields || noUpdatedField;
    }
  },
  watch: {
    'news.illustration': function() {
      this.illustrationChanged = true;
    }
  },
  created() {
    this.importActivityDetails();
  },
  mounted() {
    $('[rel="tooltip"]').tooltip();

    autosize(document.querySelector('#newsSummary'));

    this.initCKEditor();
  },
  methods: {
    initCKEditor: function () {
      let extraPlugins = 'sharedspace,simpleLink,selectImage,suggester,font,justify';
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
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        toolbarLocation: 'top',
        removeButtons: 'Subscript,Superscript,Cut,Copy,Paste,PasteText,PasteFromWord,Undo,Redo,Scayt,Unlink,Anchor,Table,HorizontalRule,SpecialChar,Maximize,Source,Strike,Outdent,Indent,BGColor,About',
        toolbar: [
          { name: 'format', items: ['Format'] },
          { name: 'basicstyles', items: [ 'Bold', 'Italic', 'Underline', 'Strike', '-', 'RemoveFormat'] },
          { name: 'paragraph', items: [ 'NumberedList', 'BulletedList', '-', 'Blockquote' ] },
          { name: 'fontsize', items: ['FontSize'] },
          { name: 'colors', items: [ 'TextColor' ] },
          { name: 'align', items: [ 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'] },
          { name: 'links', items: [ 'simpleLink', 'selectImage'] },
        ],
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
    clickOnEditButton : function () {
      this.illustrationChanged = false;
      this.showEditNews = true;
      document.querySelector('#newsShareActivity').style.display = 'none';
      document.querySelector('.newsDetails').style.display = 'none';
      if (this.showPinInput) {
        document.querySelector('#pinNewsActivity').style.display = 'none';
      }
      newsServices.clickOnEditButton(this.news.id);
      CKEDITOR.instances['newsContent'].setData(this.news.body);
      document.body.style.overflow= 'hidden';
      document.getElementsByClassName('LeftNavigationTDContainer')[0].style.display ='none';
      document.getElementsByClassName('UIToolbarContainer')[0].style.zIndex ='unset';
      document.getElementById('UIToolbarContainer').style.left ='0px';
    },
    updateNews: function () {
      this.doUpdateNews();
    },
    updateAndPostNews: function () {
      this.doUpdateNews().then(() => {

        const activity = {
          id: this.activityId,
          title: '',
          body: '',
          type: 'news',
          templateParams: {
            newsId: this.news.id,
          },
          updateDate: new Date().getTime()
        };

        return newsServices.updateAndPostNewsActivity(activity);
      }).then(() => {
        document.location.reload(true);
      });
    },
    doUpdateNews: function() {
      if(this.news.pinned !== this.originalNews.pinned) {
        let confirmText = this.$t('news.pin.confirm');
        let captionText = this.$t('news.pin.action');
        const confirmButton = this.$t('news.pin.btn.confirm');
        const cancelButton = this.$t('news.pin.btn.cancel');
        if(this.news.pinned === false) {
          confirmText = this.$t('news.unpin.confirm').replace('{0}', this.news.title);
          captionText = this.$t('news.unpin.action');
        }
        eXo.social.PopupConfirmation.confirm('createdPinnedNews', [{action: this.editNews, label : confirmButton}], captionText, confirmText, cancelButton);
      } else {
        this.editNews();
      }
    },
    editNews: function () {
      [this.news.summary, this.news.body] = newsServices.linkify(this.news.summary, this.news.body);
      const updatedNews = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary != null ? this.news.summary : '',
        body: this.news.body,
        pinned: this.news.pinned,
        publicationState: 'published'
      };

      if(this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if(this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }
      return newsServices.updateNews(updatedNews).then(() => {
        document.location.reload(true);
      })
        .catch (function() {
          document.location.reload(true);
        });
    },
    cancelEdit: function () {
      this.showEditNews = false;
      document.querySelector('#newsShareActivity').style.display = '';
      document.querySelector('.newsDetails').style.display = '';
      if (this.showPinInput) {
        document.querySelector('#pinNewsActivity').style.display = '';
      }
      this.news = JSON.parse(JSON.stringify(this.originalNews));
      CKEDITOR.instances['newsContent'].setData(this.news.body);
      document.body.style.overflow= 'auto';
      document.getElementsByClassName('LeftNavigationTDContainer')[0].style.display ='table-cell';
      document.getElementsByClassName('UIToolbarContainer')[0].style.zIndex ='1030';
      document.getElementById('UIToolbarContainer').style.left ='250px';
    },
    importActivityDetails: function () {
      this.news.activityId = this.activityId;
      newsServices.getActivityById(this.news.activityId)
        .then(resp => resp.json())
        .then(data => {
          this.news.id = data.templateParams.newsId;
          return newsServices.getNewsById(this.news.id);
        })
        .then(resp => resp.json())
        .then(newsData => {
          this.news.title = newsData.title;
          this.news.summary = newsData.summary;
          this.news.body = newsData.body;
          this.news.pinned = newsData.pinned;
          this.originalNews = JSON.parse(JSON.stringify(this.news));
          if(newsData.illustrationURL) {
            newsServices.importFileFromUrl(newsData.illustrationURL)
              .then(resp => resp.blob())
              .then(fileData => {
                const illustrationFile = new File([fileData],`illustration${this.news.id}`);
                const fileDetails = {
                  id: null,
                  uploadId: null,
                  name: illustrationFile.name,
                  size: illustrationFile.size,
                  src: newsData.illustrationURL,
                  progress: null,
                  file: illustrationFile,
                  finished: true,
                };
                this.news.illustration.push(fileDetails);
                this.originalNews = JSON.parse(JSON.stringify(this.news));
              });
          }
          Vue.nextTick(() => autosize.update(document.querySelector('#newsSummary')));
        });
    }
  },
};
</script>
