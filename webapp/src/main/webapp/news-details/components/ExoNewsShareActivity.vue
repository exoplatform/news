<template>
  <div id="newsShareActivity">
    <transition name="fade">
      <div v-show="showMessage" :class="'alert-' + messageType" class="alert">
        <i :class="messageIconClass"></i> <span v-html="message"></span>
      </div>
    </transition>

    <a id="newsShareButton" :data-original-title="$t('news.share.share')" :class="[newsArchived ? 'unauthorizedPin' : '']" class="btn"
       rel="tooltip"
       data-placement="bottom"
       @click="!newsArchived ? showShareNewsDrawer = true : null">
      <i :class="[newsArchived ? 'disabledIcon' : '']" class="uiIconShare"></i>
    </a>

    <div :class="showShareNewsDrawer ? 'open' : '' " class="drawer">
      <div class="header">
        <span>{{ $t('news.share.popupTitle') }}</span>
        <a class="closebtn" href="javascript:void(0)" @click="closeShareNewsDrawer()">×</a>
      </div>
      <div class="content">
        <div class="newsPreview">
          <a :href="news.url">
            <img :src="newsIllustrationURL" class="newsItemIllustration"/>
          </a>
          <div class="newsItemContent">
            <div class="newsItemContentHeader">
              <h3>
                <div class="previewNewsTitle">
                  <div>
                    <a :href="news.url">{{ news.title }}
                    </a>
                  </div>
                </div>
              </h3>
            </div>
            <div class="newsInfo">
              <p class="newsOwner">
                <a :href="news.authorProfileURL">
                  <img :src="news.profileAvatarURL">
                  <span>{{ news.authorFullName }}</span>
                </a>
              </p>
              <i class="uiIconArrowNext"></i>
              <p class="newsSpace">
                <a :href="news.spaceUrl" class="newsSpaceName">
                  <img :src="news.spaceAvatarUrl">
                  <span>{{ news.spaceDisplayName }}</span>
                </a>
              </p>
            </div>
          </div>
        </div>
        <div class="shareInfo">
          <div class="shareSpaces">
            <label class="newsShareWith">{{ $t('news.share.shareInSpaces') }}
              <div class="control-group">
                <div class="controls">
                  <exo-suggester v-model="spaces" :options="suggesterOptions" :source-providers="[findSpaces]"
                                 :placeholder="$t('news.share.spaces.placeholder')"/>
                </div>
              </div>
            </label>
          </div>
          <textarea v-model="description" :placeholder="$t('news.share.sharedActivityPlaceholder')"
                    class="newsShareDescription"></textarea>
        </div>
      </div>
      <div class="footer">
        <div class="shareButtons">
          <button :disabled="shareDisabled" class="btn btn-primary" @click="shareNews">{{ $t('news.share.share') }}</button>
          <button class="btn" @click="closeShareNewsDrawer">{{ $t('news.share.cancel') }}</button>
        </div>
      </div>
    </div>
    <div v-show="showShareNewsDrawer" class="drawer-backdrop" @click="closeShareNewsDrawer()"></div>
  </div>
</template>

<script>
import * as newsServices from '../../services/newsServices';

export default {
  props: {
    news: {
      type: Object,
      required: true,
      default: function() { return new Object(); }
    }
  },
  data() {
    const self = this;
    return {
      showShareNewsDrawer: false,
      spaces: [],
      spacesItems: [],
      description: '',
      message: '',
      showMessage: false,
      messageType: 'success',
      messageDisplayTime: 5000,
      newsIllustrationURL: '',
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
      return this.news.title ? this.news.title.replace(/&#39;/g, '\'') : this.news.title;
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
    this.newsIllustrationURL = !this.news.illustrationURL || this.news.illustrationURL === 'null' ? '/news/images/newsImageDefault.png' : this.news.illustrationURL;
  },
  methods: {
    shareNews: function() {
      newsServices.shareNews(this.news.newsId, this.news.activityId, this.description, this.spaces)
        .then(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          let successMessage = this.$t('news.share.message.success').replace('{0}', `<b>${escapedNewsTitle}</b>`);
          successMessage += '<ul>';
          this.spaces.forEach(space => successMessage += `<li>${this.spacesItems[space]}</li>`);
          successMessage += '</ul>';
          this.message = successMessage;
          this.messageType = 'success';
          this.showMessage = true;
        })
        .then(() => {
          this.closeShareNewsDrawer();
          this.$emit('newsShared', this.news.newsId);
        })
        .catch(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          this.message = this.$t('news.share.message.error').replace('{0}', `<b>${escapedNewsTitle}</b>`);
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
    closeShareNewsDrawer: function(){
      this.showShareNewsDrawer = false;
      this.spaces = [];
      this.description = '';
    },
    escapeHTML: function(html) {
      return document.createElement('div').appendChild(document.createTextNode(html)).parentNode.innerHTML;
    }
  }
};
</script>