<template>
  <div id="newsActivityEditComposer" :class="{ completeWidth: showEditNews }" class="newsComposer" >
    <a v-show="!showEditNews" id="newsEditButton" :data-original-title="$t('news.edit.edit')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="clickOnEditButton">
      <i class="uiIconEdit"></i>
    </a>
    <div v-show="showEditNews" class="completeWidth">
      <p class="createNews">{{ $t("news.edit.editNews") }}</p>
      <form id="newsForm" :class="newsFormExtendedClass" class="newsForm">

        <div class="newsFormWrapper">
          <div class="newsFormInputAttachement">
            <div class="newsFormInput">

              <div class="formInputGroup">
                <label class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("news.composer.title") }}
                  * : </label>
                <input id="newsTitle" v-model="news.title" :maxlength="titleMaxLength" :placeholder="$t('news.composer.placeholderTitleInput')" class="newsFormInput" type="text">
              </div>

              <div class="formInputGroup">
                <label class="newsFormLabel newsFormSummaryLabel" for="newsSummary"> {{ $t("news.composer.summary") }} : </label>
                <textarea id="newsSummary" v-model="news.summary" :maxlength="summaryMaxLength" :placeholder="$t('news.composer.placeholderSummaryInput')" class="newsFormInput" type="text"/>
              </div>
            </div>

            <div class="newsFormAttachement">
              <div class="control-group attachments">
                <div class="controls">
                  <exo-file-drop v-model="news.illustration"/>
                </div>
              </div>
            </div>
          </div>
          <p id="UINewsSummaryDescription" class="UINewsSummaryDescription">
            <i class="uiIconInformation"></i>
            {{ $t("news.composer.summaryDescription") }}
          </p>
          <div class="formInputGroup formNewsContent">
            <label class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("news.composer.content") }}
              * : </label>
            <textarea id="newsContent" v-model="news.body"
                      :placeholder="$t('news.composer.placeholderContentInput')" type="text"
                      class="newsFormInput" name="newsContent"></textarea>
          </div>


          <div class="newsFormColumn newsFormInputs">
            <div class="newsFormButtons">
              <div v-if="showPinInput" class="pinArticleContent">
                <span class="uiCheckbox">
                  <input id="pinArticle" v-model="news.pinned" type="checkbox" class="checkbox ">
                  <span class="pinArticleLabel">{{ $t("news.composer.pinArticle") }}</span>
                </span>
              </div>
              <div class="newsFormActions">
                <button id="newsEdit" :disabled="updateDisabled" class="btn btn-primary" @click.prevent="updateNews"> {{ $t("news.edit.update") }}
                </button>
                <button id="newsUpdateAndPost" :disabled="updateDisabled" class="btn" @click.prevent="updateAndPostNews"> {{ $t("news.edit.update.post") }}
                </button>
                <button id="cancelEdit" class="btn cancelEdit" @click.prevent="cancelEdit"> {{ $t("news.edit.cancel") }}
                </button>
              </div>
            </div>
          </div>

        </div>

      </form>
    </div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';

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
    this.initCKEditor();
  },
  methods: {
    initCKEditor: function () {
      let extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar,font';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }
      // this line is mandatory when a custom skin is defined
      CKEDITOR.basePath = '/commons-extension/ckeditor/';
      const self = this;
      const composerInput = $('textarea#newsContent');
      composerInput.ckeditor({
        customConfig: '/commons-extension/ckeditorCustom/config.js',
        extraPlugins: extraPlugins,
        removePlugins: 'image,confirmBeforeReload',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        toolbar : [
          ['FontSize'],
          ['Bold','Italic','RemoveFormat',],
          ['-','NumberedList','BulletedList','Blockquote'],
          ['-','simpleLink', 'selectImage'],
        ] ,
        height: this.newsFormContentHeight,
        autoGrow_minHeight: this.newsFormContentHeight,
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
      CKEDITOR.instances['newsContent'].document.$.body.innerHTML = this.news.body;
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
        });
    }
  },
};
</script>
