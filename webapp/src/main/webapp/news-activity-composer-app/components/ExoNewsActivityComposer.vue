<template>
  <div id="newsActivityComposer" :class="newsFormExtendedClass" class="uiBox newsComposer">
    <p v-show="extendedForm" class="createNews" style="display: inline;">{{ $t("news.composer.createNews") }}</p>
    <div class="newsDrafts">
      <p class="draftSavingStatus">{{ draftSavingStatus }}</p>
      <exo-news-draft v-show="extendedForm" @draftSelected="onSelectDraft"/>
    </div>
    <form id="newsForm" :class="newsFormExtendedClass" class="newsForm" @submit.prevent="postNews">

      <div class="newsFormWrapper">
        <div class="newsFormInputAttachement">
          <div class="newsFormInput">

            <div class="formInputGroup">
              <label class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("news.composer.title") }}
                * : </label>
              <input id="newsTitle" v-model="newsActivity.title" :maxlength="titleMaxLength" :placeholder="$t('news.composer.placeholderTitleInput')" class="newsFormInput" type="text">
            </div>

            <div v-show="extendedForm" class="formInputGroup">
              <label class="newsFormLabel newsFormSummaryLabel" for="newsSummary"> {{ $t("news.composer.summary") }} : </label>
              <textarea id="newsSummary" v-model="newsActivity.summary" :maxlength="summaryMaxLength" :placeholder="$t('news.composer.placeholderSummaryInput')" class="newsFormInput" type="text"/>
            </div>
          </div>

          <div v-show="extendedForm" class="newsFormAttachement">
            <div class="control-group attachments">
              <div class="controls">
                <exo-file-drop v-model="newsActivity.illustration"/>
              </div>
            </div>
          </div>
        </div>
        <p v-show="extendedForm" id="UINewsSummaryDescription" class="UINewsSummaryDescription">
          <i class="uiIconInformation"></i>
          {{ $t("news.composer.summaryDescription") }}
        </p>
        <div class="formInputGroup formNewsContent">
          <label class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("news.composer.content") }}
            * : </label>
          <textarea id="newsContent" v-model="newsActivity.content"
                    :placeholder="$t('news.composer.placeholderContentInput')" type="text"
                    class="newsFormInput" name="newsContent"></textarea>
        </div>


        <div class="newsFormColumn newsFormInputs">
          <div class="newsFormButtons">
            <div v-if="showPinInput" class="pinArticleContent">
              <span class="uiCheckbox">
                <input id="pinArticle" v-model="pinArticle" type="checkbox" class="checkbox ">
                <span class="pinArticleLabel">{{ $t("news.composer.pinArticle") }}</span>
              </span>
            </div>
            <div class="newsFormActions">
              <a id="newsPlus" :data-original-title="extendFormButtonTooltip" class="btn btn-primary"
                 rel="tooltip" data-placement="bottom"
                 @click="extendedForm = !extendedForm;">
                <i :class="extendFormButtonClass"></i>
              </a>
              <button id="newsPost" :disabled="postDisabled" class="btn btn-primary"> {{ $t("news.composer.post") }}
              </button>
            </div>
          </div>
        </div>
      </div>

    </form>
  </div>
</template>

<script>
import * as newsServices from '../../services/newsServices';

export default {
  props: {
    showPinInput: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      newsActivity: {
        id: '',
        title: '',
        content: '',
        summary: '',
        illustration: []
      },
      file: {
        src: ''
      },
      pinArticle: false,
      SMARTPHONE_LANDSCAPE_WIDTH: 768,
      titleMaxLength: 150,
      summaryMaxLength: 1000,
      autoSaveDelay: 1000,
      extendedForm: this.$route.hash.split('/')['1']  === 'post' || this.$route.hash.split('/')['1']  === 'draft'  ? true : false ,
      extendFormButtonClass: 'uiIconSimplePlus',
      extendFormButtonTooltip: this.$t('news.composer.moreOptions'),
      newsFormExtendedClass: '',
      newsFormContentHeight: '',
      showDraftNews: false,
      postingNews: false,
      savingDraft: false,
      saveDraft : '',
      draftSavingStatus: ''
    };
  },
  computed: {
    postDisabled: function () {
      return !this.newsActivity.title || !this.newsActivity.title.trim() || !this.newsActivity.content || !this.newsActivity.content.replace(/<[^>]*>/g, '').replace(/&nbsp;/g, '').trim();
    }
  },
  watch: {
    extendedForm: function() {
      this.extendForm();
      if(this.$route.fullPath.split('/')['1']  !== 'draft'){
        this.$router.push({name: 'NewsComposer', params: {action: 'post'}});
      }
    },
    'newsActivity.title': function() { this.autoSave(); },
    'newsActivity.summary': function() { this.autoSave(); },
    'newsActivity.content': function() { this.autoSave(); },
    'newsActivity.illustration': function() { this.autoSave(); }
  },
  created() {
    const textarea = document.querySelector('#activityComposerTextarea');
    const shareButton = document.querySelector('#ShareButton');
    if (textarea && shareButton) {
      textarea.style.display = 'none';
      shareButton.style.display = 'none';
    }
    if(this.$route.hash.split('/')['2']) {
      this.initNewsComposerData(this.$route.hash.split('/')['2']);
    }
  },
  mounted() {
    $('[rel="tooltip"]').tooltip();
    this.initCKEditor();
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
        removePlugins: 'image, confirmBeforeReload',
        extraAllowedContent: 'img[style,class,src,referrerpolicy,alt,width,height]',
        toolbar : [
          ['FontSize'],
          ['Bold','Italic','RemoveFormat',],
          ['-','NumberedList','BulletedList','Blockquote'],
          ['-','simpleLink', 'selectImage'],
        ] ,
        height: this.newsFormContentHeight ,
        on: {
          change: function (evt) {
            self.newsActivity.content = evt.editor.getData();
          }
        }
      });
    },
    autoSave: function() {
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
    initNewsComposerData: function(draftId) {
      newsServices.getNewsById(draftId)
        .then((data) => {
          if (data.ok) {return data.json();}
        })
        .then(newsDraftNode => {
          if(newsDraftNode){
            this.newsActivity.id = newsDraftNode.id;
            this.newsActivity.title = newsDraftNode.title;
            this.newsActivity.content = newsDraftNode.body;
            this.newsActivity.summary = newsDraftNode.summary;
            CKEDITOR.instances['newsContent'].setData(newsDraftNode.body);
            if (newsDraftNode.illustrationURL) {
              newsServices.importFileFromUrl(newsDraftNode.illustrationURL)
                .then(resp => resp.blob())
                .then(fileData => {
                  const illustrationFile = new File([fileData], `illustration${draftId}`);
                  const fileDetails = {
                    id: null,
                    uploadId: null,
                    name: illustrationFile.name,
                    size: illustrationFile.size,
                    src: newsDraftNode.illustrationURL,
                    progress: null,
                    file: illustrationFile,
                    finished: true,
                  };
                  this.newsActivity.illustration.push(fileDetails);
                });
            }
          } else {
            this.$router.push({name: 'NewsComposer', params: {action: 'post'}});
          }
        });
    },
    postNews: function () {
      if(this.pinArticle === true) {
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
      [this.newsActivity.summary, this.newsActivity.content] = newsServices.linkify(this.newsActivity.summary, this.newsActivity.content);
      const news = {
        id: this.newsActivity.id,
        title: this.newsActivity.title,
        summary: this.newsActivity.summary,
        body: this.newsActivity.content,
        author: eXo.env.portal.userName,
        pinned: this.pinArticle,
        spaceId: eXo.env.portal.spaceId,
        publicationState: 'published'
      };

      if(this.newsActivity.illustration.length > 0) {
        news.uploadId = this.newsActivity.illustration[0].uploadId;
      }

      newsServices.saveNews(news).then(() => {
        // reset form
        this.resetNewsActivity();
        this.$emit('draftDeleted');
        this.extendedForm = false;
        // refresh activity stream
        const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
        if (refreshButton) {
          refreshButton.click();
        }
        this.$router.push({name: 'NewsComposer', params:{action : 'post'}});
        this.postingNews = false;
      });
    },
    extendForm: function(){
      this.extendFormButtonClass = this.extendedForm ? 'uiIconMinimize' : 'uiIconSimplePlus';
      this.extendFormButtonTooltip = this.extendedForm ? this.$t('news.composer.lessOptions') : this.$t('news.composer.moreOptions');
      document.getElementById('UISpaceMenu').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('ActivityComposerExt').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('UISpaceActivitiesDisplay').style.display = this.extendedForm ? 'none' : '';
      const spaceHomePortletColumn = document.getElementsByClassName('SpaceHomePortletsTDContainer');
      if(spaceHomePortletColumn.length > 0) {
        spaceHomePortletColumn[0].style.display = this.extendedForm ? 'none' : '';
      }
      const portletsContainers = document.getElementById('UIPage').getElementsByClassName('PORTLET-FRAGMENT');
      Array.from(portletsContainers).forEach(portletContainer => {
        if(portletContainer.getElementsByClassName('uiSpaceActivityStreamPortlet').length === 0) {
          portletContainer.style.display = this.extendedForm ? 'none' : '';
        }
      });
      this.newsFormExtendedClass = this.extendedForm ? 'extended' : '';
      this.newsFormContentHeight = this.extendedForm ? '250' : '110';
      CKEDITOR.instances['newsContent'].resize('100%', this.newsFormContentHeight);
    },
    saveNewsDraft: function () {
      const news = {
        title: this.newsActivity.title,
        summary: this.newsActivity.summary,
        body: this.newsActivity.content,
        author: eXo.env.portal.userName,
        pinned: false,
        spaceId: eXo.env.portal.spaceId,
        publicationState: ''
      };
      if (this.newsActivity.illustration.length > 0) {
        news.uploadId = this.newsActivity.illustration[0].uploadId;
      } else {
        news.uploadId = '';
      }
      const draftExists = this.$route.hash.split('/')['2'] || this.$route.fullPath.split('/')['1'] === 'draft';
      if (draftExists) {
        if(this.newsActivity.title || this.newsActivity.summary || this.newsActivity.content || this.newsActivity.illustration.length > 0) {
          news.id = this.newsActivity.id;
          newsServices.updateNews(news)
            .then(() => this.$emit('draftUpdated'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        } else {
          newsServices.deleteDraft(this.newsActivity.id)
            .then(() => this.$emit('draftDeleted'))
            .then(() => this.$router.push({name: 'NewsComposer', params: {action: 'post'}}))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        }
        this.savingDraft = false;
      } else if(this.newsActivity.title || this.newsActivity.content) {
        news.publicationState = 'draft';
        newsServices.saveNews(news).then((createdNews) => {
          this.$router.push({name: 'NewsDraft', params: {action: 'draft', nodeId: createdNews.id}});
          this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus');
          this.newsActivity.id = createdNews.id;
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
      this.$router.push({name: 'NewsDraft', params: {action: 'draft', nodeId: draftId}});
    },
    resetNewsActivity: function(){
      this.newsActivity.id = '';
      this.newsActivity.title = '';
      this.newsActivity.content = '';
      this.newsActivity.summary = '';
      this.newsActivity.illustration = [];
      CKEDITOR.instances['newsContent'].setData('');
      this.pinArticle = false;
    }
  }
};
</script>
