<template>
  <div id="newsActivityEditComposer" :class="{ completeWidth: showEditNews }" class="newsComposer" >
    <a v-show="!showEditNews" id="newsEditButton" :data-original-title="$t('activity.edit.news.edit')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="clickOnEditButton">
      <i class="uiIconEdit"></i>
    </a>
    <div v-show="showEditNews" class="completeWidth">
      <p class="createNews">{{ $t("activity.edit.news.editNews") }}</p>
      <form id="newsForm" :class="newsFormExtendedClass" class="newsForm">

        <div class="newsFormWrapper">
          <div class="newsFormInputAttachement">
            <div class="newsFormInput">

              <div class="formInputGroup">
                <label class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("activity.composer.news.title") }}
                  * : </label>
                <input id="newsTitle" v-model="news.title" :maxlength="titleMaxLength" :placeholder="$t('activity.composer.news.placeholderTitleInput')" class="newsFormInput" type="text">
              </div>

              <div class="formInputGroup">
                <label class="newsFormLabel newsFormSummaryLabel" for="newsSummary"> {{ $t("activity.composer.news.summary") }} : </label>
                <textarea id="newsSummary" v-model="news.summary" :maxlength="summaryMaxLength" :placeholder="$t('activity.composer.news.placeholderSummaryInput')" class="newsFormInput" type="text"/>
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
            {{ $t("activity.composer.news.summaryDescription") }}
          </p>
          <div class="formInputGroup formNewsContent">
            <label class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("activity.composer.news.content") }}
              * : </label>
            <textarea id="newsContent" v-model="news.body"
                      :placeholder="$t('activity.composer.news.placeholderContentInput')" type="text"
                      class="newsFormInput" name="newsContent"></textarea>
          </div>


          <div class="newsFormColumn newsFormInputs">
            <div class="newsFormButtons">
              <div class="newsFormActions">
                <button id="newsEdit" :disabled="updateDisabled" class="btn btn-primary" @click.prevent="updateNews"> {{ $t("activity.edit.news.update") }}
                </button>
                <button id="newsUpdateAndPost" :disabled="updateDisabled" class="btn" @click.prevent="updateAndPostNews"> {{ $t("activity.edit.news.update.post") }}
                </button>
                <button id="cancelEdit" class="btn cancelEdit" @click.prevent="cancelEdit"> {{ $t("activity.edit.news.cancel") }}
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
import * as  newsServices from '../newsServices';

export default {
  props: {
    activityId: {
      type: String,
      required: true
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
        spaceId: ''
      },
      originalNews: {
        id: '',
        activityId: '',
        title: '',
        body: '',
        summary: '',
        illustration: [],
        spaceId: ''
      },
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      titleMaxLength: 150,
      summaryMaxLength: 1000,
      newsFormContentHeight: 330,
      showEditNews: false,
      illustrationChanged: false
    };
  },
  computed: {
    updateDisabled: function () {
      const emptyMandatoryFields = !this.news.title || !this.news.title.trim() || !this.news.body || !this.news.body.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim();
      const noUpdatedField = !this.illustrationChanged && this.news.title === this.originalNews.title && this.news.summary === this.originalNews.summary && this.news.body === this.originalNews.body;
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
      let extraPlugins = 'simpleLink,selectImage,suggester,hideBottomToolbar';
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
      newsServices.clickOnEditButton(this.news.id);
    },
    updateNews: function () {
      this.doUpdateNews().then(() => {
        document.location.reload(true);
      });
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
      const updatedNews = {
        id: this.news.id,
        title: this.news.title,
        summary: this.news.summary != null ? this.news.summary : '',
        body: this.news.body
      };

      if(this.news.illustration != null && this.news.illustration.length > 0) {
        updatedNews.uploadId = this.news.illustration[0].uploadId;
      } else if(this.originalNews.illustrationURL !== null) {
        // an empty uploadId means the illustration must be deleted
        updatedNews.uploadId = '';
      }

      return newsServices.updateNews(updatedNews);
    },
    cancelEdit: function () {
      this.showEditNews = false;
      document.querySelector('#newsShareActivity').style.display = '';
      document.querySelector('.newsDetails').style.display = '';
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