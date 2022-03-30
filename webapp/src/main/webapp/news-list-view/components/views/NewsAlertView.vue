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
  <div id="critical-alerts-slider" :class="canPublishNews ? 'mt-n13' : 'mt-n5'">
    <div class="alerts-header">
      <div class="alerts-icon">
        <v-icon>warning</v-icon>
      </div>
      <span v-if="!emptyTemplate">{{ $t('news.alerts.header') }}</span>
    </div>

    <div class="alerts-viewer ps-5 flex-grow-1">
      <v-carousel
        v-model="slider"
        hide-delimiters
        cycle
        :show-arrows="false"
        interval="10000"
        height="20"
        v-if="!emptyTemplate">
        <v-carousel-item
          v-for="(item,i) in news"
          :key="i">
          <a :href="item.url" class="article-link flex-grow-1">
            <div class="alerts-article">
              <span class="alerts-article-date">
                <date-format
                  :value="new Date(item.publishDate.time)"
                  :format="dateFormat" />
              </span>
              <span class="alerts-article-seperator">|</span>
              <span class="alerts-article-title">{{ item.title }} - {{ item.summary }}</span>
            </div>
          </a>
        </v-carousel-item>
      </v-carousel>
    </div>  

    <div class="slider-buttons d-flex pe-2">
      <v-btn
        @click="slider--"
        icon
        :disabled="emptyTemplate">
        <v-icon>chevron_left</v-icon>
      </v-btn>
      <v-btn
        @click="slider++"
        icon
        :disabled="emptyTemplate">
        <v-icon>chevron_right</v-icon>
      </v-btn>
      <v-btn
        v-if="canPublishNews"
        @click="openDrawer"
        icon>
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>

<script>
export default {
  props: {
    newsTarget: {
      type: String,
      required: false,
      default: null
    },
  },
  data () {
    return {
      canPublishNews: false,
      slider: 0,
      news: [],
      initialized: false,
      limit: 4,
      offset: 0,

      showHeader: false,
      showSeeAll: false,
      showArticleTitle: true,
      showArticleSummary: true,
      showArticleImage: true,
      showArticleAuthor: true,
      showArticleSpace: true,
      showArticleDate: true,
      showArticleReactions: true,
      seeAllUrl: '',
      selectedOption: null,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
    };
  },
  computed: {
    emptyTemplate() {
      return !(this.news && this.news.length);
    },
  },
  created() {
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
  },
  methods: {
    openDrawer() {
      this.$refs.settingsDrawer.open();
    },
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.news = newsList.news;
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget, selectedOption) {
      this.limit = selectedOption.limit;
      this.selectedOption = selectedOption;
      this.newsTarget = selectedTarget;
      this.getNewsList();
    }
  }
};
</script>
