<!--
Copyright (C) 2022 eXo Platform SAS.

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
  <div id="critical-alerts-slider">
    <div class="alerts-header">
      <div class="alerts-icon">
        <v-icon>warning</v-icon>
      </div>
      <span class="d-none d-md-block" v-if="!emptyTemplate && showHeader">{{ newsHeader }}</span>
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
              <span v-if="showArticleDate" class="alerts-article-date">
                <date-format
                  :value="new Date(item.publishDate.time)"
                  :format="dateFormat" />
              </span>
              <span v-if="showArticleDate && showArticleTitle" class="alerts-article-seperator">|</span>
              <span v-if="showArticleTitle" class="alerts-article-title">{{ item.title }}</span>
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
        v-if="$root.canPublishNews"
        icon
        @click="openDrawer">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
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
      slider: 0,
      news: [],
      initialized: false,
      limit: 4,
      offset: 0,

      showHeader: true,
      showSeeAll: false,
      showArticleTitle: true,
      showArticleSummary: false,
      showArticleImage: false,
      showArticleAuthor: false,
      showArticleSpace: true,
      showArticleDate: true,
      showArticleReactions: false,
      seeAllUrl: '',
      newsHeader: '',
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
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
  },
  methods: {
    openDrawer() {
      this.$root.$emit('news-settings-drawer-open');
    },
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.news = newsList.news.filter(news => !!news);
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget, selectedOption) {
      this.showArticleTitle = selectedOption.showArticleTitle;
      this.showArticleDate = selectedOption.showArticleDate;
      this.showHeader = selectedOption.showHeader;
      this.selectedOption = selectedOption;
      this.newsHeader = selectedOption.header;
      this.seeAllUrl = selectedOption.seeAllUrl;
      this.limit = selectedOption.limit;
      this.newsTarget = selectedTarget;
      this.getNewsList();
    },
    reset() {
      this.limit = this.$root.limit;
      this.showHeader = this.$root.showHeader;
      this.newsHeader = this.$root.header;
      this.showSeeAll = this.$root.showSeeAll;
      this.showArticleTitle = this.$root.showArticleTitle;
      this.showArticleImage = this.$root.showArticleImage;
      this.showArticleSummary = this.$root.showArticleSummary;
      this.showArticleAuthor = this.$root.showArticleAuthor;
      this.showArticleSpace = this.$root.showArticleSpace;
      this.showArticleDate = this.$root.showArticleDate;
      this.showArticleReactions = this.$root.showArticleReactions;
      this.seeAllUrl = this.$root.seeAllUrl;
      this.selectedOption = {
        limit: this.limit,
        showHeader: this.showHeader,
        showSeeAll: this.showSeeAll,
        showArticleTitle: this.showArticleTitle,
        showArticleSummary: this.showArticleSummary,
        showArticleAuthor: this.showArticleAuthor,
        showArticleSpace: this.showArticleSpace,
        showArticleDate: this.showArticleDate,
        showArticleReactions: this.showArticleReactions,
        showArticleImage: this.showArticleImage,
        seeAllUrl: this.seeAllUrl,
      };
    },
  }
};
</script>
