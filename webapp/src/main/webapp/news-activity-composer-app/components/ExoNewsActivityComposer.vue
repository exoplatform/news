<template>
  <div id="newsActivityComposer" :class="newsFormExtendedClass" class="uiBox newsComposer">
    <div class="newsComposerActions">
      <div class="newsFormButtons">
        <div class="newsFormLeftActions">
          <a id="newsPlus" :data-original-title="extendFormButtonTooltip" :class="extendFormButtonClass"
             rel="tooltip" data-placement="bottom"
             @click="extendedForm = !extendedForm;">
            {{ extendFormButtonValue }}
            <i :class="extendFormIconClass"></i>
          </a>
          <div v-if="showPinInput" class="pinArticleContent">
            <span class="uiCheckbox">
              <input id="pinArticle" v-model="pinArticle" type="checkbox" class="checkbox ">
              <span class="pinArticleLabel">{{ $t("news.composer.pinArticle") }}</span>
            </span>
          </div>
        </div>
        <div class="newsFormRightActions">
          <p class="draftSavingStatus">{{ draftSavingStatus }}</p>
          <div v-show="extendedForm" class="newsDrafts">
            <exo-news-draft @draftSelected="onSelectDraft"/>
          </div>
          <button id="newsPost" :disabled="postDisabled" class="btn btn-primary" @click="postNews"> {{ $t("news.composer.post") }}
          </button>
        </div>
      </div>
      <div id="newsTop"></div>
    </div>
    <form id="newsForm" :class="newsFormExtendedClass" class="newsForm">
      <div class="newsFormInput">
        <div v-show="extendedForm" class="newsFormAttachment">
          <div class="control-group attachments">
            <div class="controls">
              <exo-file-drop v-model="newsActivity.illustration" @change="autoSave"/>
            </div>
          </div>
        </div>
        <div class="formInputGroup newsTitle">
          <label v-show="!extendedForm" class="newsFormLabel newsFormTitleLabel" for="newsTitle">{{ $t("news.composer.title") }}*</label>
          <input id="newsTitle" v-model="newsActivity.title" :maxlength="titleMaxLength" :placeholder="newsFormTitlePlaceholder" type="text">
        </div>
        <div v-show="extendedForm" class="formInputGroup">
          <label v-show="!extendedForm" class="newsFormLabel newsFormSummaryLabel" for="newsSummary"> {{ $t("news.composer.summary") }}</label>
          <textarea id="newsSummary"
                    v-model="newsActivity.summary"
                    :maxlength="summaryMaxLength"
                    :placeholder="newsFormSummaryPlaceholder"
                    class="newsFormInput">
          </textarea>
        </div>
        <div class="formInputGroup">
          <label v-show="!extendedForm" class="newsFormLabel newsFormContentLabel" for="newsContent">{{ $t("news.composer.content") }}*</label>
          <textarea id="newsContent"
                    v-model="newsActivity.content"
                    :placeholder="newsFormContentPlaceholder"
                    class="newsFormInput"
                    name="newsContent">
          </textarea>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import * as newsServices from '../../services/newsServices';
import autosize from 'autosize';

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
      extendedForm: this.$route.hash.split('/')['1'] === 'post' || this.$route.hash.split('/')['1'] === 'draft' ? true : false,
      extendFormButtonClass: 'btn btn-primary',
      extendFormIconClass: 'uiIconSimplePlus',
      extendFormButtonTooltip: this.$t('news.composer.moreOptions'),
      extendFormButtonValue: '',
      newsFormExtendedClass: '',
      newsFormTitlePlaceholder: `${this.$t('news.composer.placeholderTitleInput')}*`,
      newsFormSummaryPlaceholder: this.$t('news.composer.placeholderSummaryInput'),
      newsFormContentPlaceholder: `${this.$t('news.composer.placeholderContentInput')}*`,
      newsFormContentHeight: '250',
      newsFormSummaryHeight: '80',
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
      const currentLocation = this.$route.fullPath.split('/')['1'];
      if(currentLocation !== 'draft' && currentLocation !== 'post' && this.extendedForm){
        this.$router.push({name: 'NewsComposer', params: {action: 'post'}});
      }
    },
    'newsActivity.title': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } },
    'newsActivity.summary': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } },
    'newsActivity.content': function(newValue, oldValue) { if(newValue !== oldValue) { this.autoSave(); } }
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

      let extraPlugins = 'simpleLink,selectImage,suggester,font,justify';
      const windowWidth = $(window).width();
      const windowHeight = $(window).height();
      if (windowWidth > windowHeight && windowWidth < this.SMARTPHONE_LANDSCAPE_WIDTH) {
        // Disable suggester on smart-phone landscape
        extraPlugins = 'simpleLink,selectImage';
      }

      if(this.extendedForm) {
        extraPlugins = 'sharedspace,simpleLink,selectImage,suggester,font,justify';
        CKEDITOR.addCss('.cke_editable { font-size: 18px; }');
      } else {
        CKEDITOR.addCss('.cke_editable { font-size: 13px; }');
      }

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
        format_tags: 'p;h1;h2;h3',
        autoGrow_minHeight: this.newsFormContentHeight,
        height: this.newsFormContentHeight,
        bodyClass: 'newsContent',
        sharedSpaces: {
          top: 'newsTop'
        },
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
            this.newsActivity.summary = newsDraftNode.summary;
            this.newsActivity.content = newsDraftNode.body;
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
            Vue.nextTick(() => autosize.update(document.querySelector('#newsSummary')));
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
      this.extendFormIconClass = this.extendedForm ? '' : 'uiIconSimplePlus';
      this.extendFormButtonClass = this.extendedForm ? 'btn' : 'btn btn-primary';
      this.extendFormButtonValue = this.extendedForm ? 'Cancel' : '';
      this.extendFormButtonTooltip = this.extendedForm ? this.$t('news.composer.lessOptions') : this.$t('news.composer.moreOptions');
      document.getElementById('UISpaceMenu').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('ActivityComposerExt').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('UISpaceActivitiesDisplay').style.display = this.extendedForm ? 'none' : '';
      document.getElementById('UISpaceActivityStreamPortlet').style.padding = this.extendedForm ? '0' : '20px';
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
      document.getElementsByClassName('LeftNavigationTDContainer')[0].style.display = this.extendedForm ? 'none' : '';
      document.getElementsByClassName('UIToolbarContainer')[0].style.zIndex = this.extendedForm ? 'unset' : '1030';
      document.getElementById('UIToolbarContainer').style.left = this.extendedForm ? '0px' : '250px';
      this.newsFormExtendedClass = this.extendedForm ? 'extended' : '';
      this.newsFormContentHeight = this.extendedForm ? '250' : '110';
      document.body.style.overflow = this.extendedForm ? 'hidden' : 'auto';
      this.initCKEditor();
      if (!this.extendedForm){
        this.$router.push('/');
      }
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
            .then((updatedNews) => {
              this.newsActivity.title = updatedNews.title;
              if(this.newsActivity.summary !== updatedNews.summary) {
                this.newsActivity.summary = updatedNews.summary;
              }
              if(this.newsActivity.content !== updatedNews.body) {
                this.newsActivity.content = updatedNews.body;
                CKEDITOR.instances['newsContent'].setData(updatedNews.body);
              }
            })
            .then(() => this.$emit('draftUpdated'))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        } else {
          newsServices.deleteDraft(this.newsActivity.id)
            .then(() => this.$emit('draftDeleted'))
            .then(() => this.$router.push({name: 'NewsComposer', params: {action: 'post'}}))
            .then(() => this.draftSavingStatus = this.$t('news.composer.draft.savedDraftStatus'));
        }
        this.savingDraft = false;
      } else if(this.newsActivity.title || this.newsActivity.summary || this.newsActivity.content || this.newsActivity.illustration.length > 0) {
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
      this.newsActivity.summary = '';
      this.newsActivity.content = '';
      this.newsActivity.illustration = [];
      this.pinArticle = false;
    }
  }
};
</script>
