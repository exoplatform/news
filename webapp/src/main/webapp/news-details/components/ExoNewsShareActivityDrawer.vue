<template>
  <v-app class="shareNewsDrawer">
    <exo-drawer
      ref="shareNewsDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        {{ $t('news.share.popupTitle') }}
      </template>
      <template slot="content">
        <v-form
          v-if="news"
          ref="newsShareFrom"
          class="flex news-form share-news"
          flat>
          <div class="d-flex flex-column flex-grow-1">
            <div class="d-flex flex-row newsInformationCard">
              <exo-news-information :news="news" class="ml-2 my-4"/>
            </div>
            <div class="d-flex flex-row">
              <span class="ml-2 mb-2">{{ $t('news.share.shareInSpaces') }} </span>
            </div>
            <div class="d-flex flex-row flex-grow-1 newsSpaceSuggester">
              <exo-news-space-suggester :spaces="spaces" class="ml-2" />
            </div>
            <div class="d-flex flex-row">
              <textarea v-model="description"
                        :placeholder="$t('news.share.sharedActivityPlaceholder')"
                        class="ml-2 ignore-vuetify-classes newsShareDescription">
              </textarea>
            </div>
          </div>
        </v-form>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="shareDisabled"
            class="btn btn-primary mr-2"
            @click="shareNews">
            {{ $t('news.share.share') }}
          </v-btn>
          <v-btn
            class="btn"
            @click="closeShareNewsDrawer">
            {{ $t('news.share.cancel') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
import * as newsServices from '../../services/newsServices';

export default {
  data:() => ({
    showShareNewsDrawer: false,
    news: null,
    spaces: [],
    spacesItems: [],
    description: '',
    message: '',
    showMessage: false,
    messageType: 'success',
    messageDisplayTime: 5000,
    newsIllustrationURL: '',
  }),
  computed:{
    newsTitleUnescaped: function() {
      return this.news.title ? this.news.title.replace(/&#39;/g, '\'') : this.news.title;
    },
    shareDisabled: function() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
  },
  created() {
    this.$root.$on('news-share-drawer-open', news => {
      this.news = news;
      this.open();
    });
  },
  methods: {
    open() {
      if (this.$refs.shareNewsDrawer) {
        this.$refs.shareNewsDrawer.open();
      }
    },
    shareNews() {
      newsServices.shareNews(this.news.newsId, this.news.activityId, this.description, this.spaces)
        .then(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          let successMessage = this.$t('news.share.message.success').replace('{0}', `<b>${escapedNewsTitle}</b>`);
          successMessage += '<ul>';
          this.spaces.forEach(space => successMessage += `<li>${space}</li>`);
          successMessage += '</ul>';
          this.message = successMessage;
          this.messageType = 'success';
          this.showMessage = true;
        })
        .then(() => {
          this.closeShareNewsDrawer();
          this.$emit('newsShared', this.news.newsId);
          this.$root.$emit('display-share-alert', this.showMessage);
        })
        .catch(() => {
          const escapedNewsTitle = this.escapeHTML(this.newsTitleUnescaped);
          this.message = this.$t('news.share.message.error').replace('{0}', `<b>${escapedNewsTitle}</b>`);
          this.messageType = 'error';
          this.showMessage = true;
        });
    },
    closeShareNewsDrawer() {
      this.$refs.shareNewsDrawer.close();
      this.spaces = [];
      this.description = '';
    },
    escapeHTML(html) {
      return document.createElement('div').appendChild(document.createTextNode(html)).parentNode.innerHTML;
    }
  }
};
</script>