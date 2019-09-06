<template>
  <div id="newsShareActivity">
    <transition name="fade">
      <div v-show="showMessage" :class="'alert-' + messageType" class="alert">
        <i :class="messageIconClass"></i> <span v-html="message"></span>
      </div>
    </transition>

    <a id="newsShareButton" :data-original-title="$t('activity.news.shareNews.share')" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="showShareNewsPopup = true">
      <i class="uiIconShare"></i>
    </a>
    <exo-news-modal :show="showShareNewsPopup" :title="$t('activity.news.shareNews.popupTitle')" @close="closeShareNewsPopup">
      <div class="newsShareForm">
        <label class="newsTitle"> {{ newsTitleUnescaped }}</label>
        <div class="shareSpaces">
          <label class="newsShareWith">{{ $t('activity.news.shareNews.shareWith') }}
            <div class="control-group">
              <div class="controls">
                <exo-suggester v-model="spaces" :options="suggesterOptions" :source-providers="[findSpaces]" :placeholder="$t('activity.news.shareNews.spaces.placeholder')" />
              </div>
            </div>
        </label></div>
        <textarea v-model="description" :placeholder="$t('activity.news.shareNews.sharedActivityPlaceholder')" class="newsShareDescription"></textarea>
        <div class="shareButtons">
          <button :disabled="shareDisabled" class="btn btn-primary" @click="shareNews">{{ $t('activity.news.shareNews.share') }}</button>
          <button class="btn" @click="closeShareNewsPopup">{{ $t('activity.news.shareNews.cancel') }}</button>
        </div>
      </div>
    </exo-news-modal>
  </div>
</template>

<script>
import * as newsServices from '../newsServices.js';

export default {
  props: {
    activityId: {
      type: String,
      required: true
    },
    newsId: {
      type: String,
      required: true
    },
    newsTitle: {
      type: String,
      required: true
    }
  },
  data() {
    const self = this;
    return {
      showShareNewsPopup: false,
      spaces: [],
      spacesItems: [],
      description: '',
      message: '',
      showMessage: false,
      messageType: 'success',
      messageDisplayTime: 5000,
      suggesterOptions: {
        valueField: 'value',
        labelField: 'text',
        searchField: ['text'],
        renderMenuItem: function(item, escape) {
          // workaround to keep spaces labels to display in the success message without fetching them on server
          self.spacesItems[item.value] = item.text;

          let avatar = item.avatarUrl;
          if (avatar == null) {
            avatar = '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
          }
          if(!item.text) {
            item.text = item.value;
          }
          return `<div class="optionItem" data-value="${item.text}"><div class="avatarSmall optionAvatar"><img src="${avatar}"></div><div class="optionName">${escape(item.text)}</div></div>`;
        },
      }
    };
  },
  computed:{
    newsTitleUnescaped: function() {
      return this.newsTitle ? this.newsTitle.replace(/&#39;/g, '\'') : this.newsTitle;
    },
    shareDisabled: function() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
    messageIconClass: function() {
      return `uiIcon${this.messageType.charAt(0).toUpperCase()}${this.messageType.slice(1)}`;
    }
  },
  watch: {
    showMessage(newValue) {
      if(newValue) {
        setTimeout(() => {this.showMessage = false;}, this.messageDisplayTime);
      }
    }
  },
  mounted(){
    $('[rel="tooltip"]').tooltip();
  },
  methods: {
    shareNews: function() {
      newsServices.shareNews(this.newsId, this.activityId, this.description, this.spaces)
        .then(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          let successMessage = this.$t('activity.news.shareNews.message.success').replace('{0}', `<b>${escapedNewsTitle}</b>`);
          successMessage += '<ul>';
          this.spaces.forEach(space => successMessage += `<li>${this.spacesItems[space]}</li>`);
          successMessage += '</ul>';
          this.message = successMessage;
          this.messageType = 'success';
          this.showMessage = true;
        })
        .then(() => this.closeShareNewsPopup())
        .catch(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          this.message = this.$t('activity.news.shareNews.message.error').replace('{0}', `<b>${escapedNewsTitle}</b>`);
          this.messageType = 'error';
          this.showMessage = true;
        });
    },
    findSpaces: function(query, callback) {
      if (!query || !query.length) {
        callback([]);
      } else {
        newsServices.findUserSpaces(query).then(spaces => {
          if(spaces) {
            callback(spaces);
          }
        });
      }
    },
    closeShareNewsPopup: function(){
      this.showShareNewsPopup = false;
      this.spaces = [];
      this.description = '';
    },
    escapeHTML: function(html) {
      return document.createElement('div').appendChild(document.createTextNode(html)).parentNode.innerHTML;
    }
  }
};
</script>