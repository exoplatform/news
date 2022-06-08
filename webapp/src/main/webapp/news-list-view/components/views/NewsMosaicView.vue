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
  <div id="top-news-mosaic" ref="top-news-mosaic">
    <div :class="`mosaic-container ma-3 ${smallHeightClass}`">
      <div 
        v-for="(item, index) of news"
        :key="index"
        :class="isSmallWidth ? 'articleSmallWidth' : 'article'"
        :id="`articleItem-${index}`">
        <a
          class="articleLink d-block"
          target="_self"
          :href="item.url">
          <img :src="showArticleImage && item.illustrationURL !== null ? item.illustrationURL : '/news/images/news.png'" :alt="$t('news.latest.alt.articleImage')">
          <div class="titleArea">
            <div v-if="showArticleDate" class="articleDate">
              <date-format
                :value="new Date(item.publishDate.time)"
                :format="dateFormat" />
            </div>
            <div v-if="showArticleTitle" :class="'text-truncate' + isSmallWidth ? 'articleTitle text-truncate':''">
              {{ item.title }}
            </div>
          </div>
        </a>
      </div>
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
      news: [],
      initialized: false,
      limit: 4,
      offset: 0,

      showHeader: false,
      showSeeAll: true,
      showArticleTitle: true,
      showArticleSummary: false,
      showArticleImage: true,
      showArticleAuthor: true,
      showArticleSpace: true,
      showArticleDate: true,
      showArticleReactions: true,
      seeAllUrl: '',
      newsHeader: '',
      selectedOption: null,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
      isSmallWidth: false
    };
  },
  created() {
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
  },
  mounted() {
    this.isSmallWidth =  this.$refs && this.$refs['top-news-mosaic'] && this.$refs['top-news-mosaic']?.clientWidth *100 / window.screen.width  < 33;
  },
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    smallHeightClass() {
      return this.isMobile && this.news && this.news.length === 1 && 'small-mosaic-container';
    },
  },
  methods: {
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
    minLength(lengthNews){
      return lengthNews < 5 && lengthNews > 0 ? 100 / lengthNews : 25;
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
