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
  <div
    id="news-latest-view"
    ref="news-latest-view"
    class="px-2 py-2"
    :class="extraClass">
    <div :class="hasSmallWidthContainer ? 'article-small-container':'article-container'">
      <v-progress-circular
        v-if="loading"
        :size="50"
        class="loader"
        color="primary"
        indeterminate />
      <div
        v-for="(item, index) of newsInfo"
        :key="item"
        :class="hasSmallWidthContainer ? 'smallWidthContainer' : 'article'"
        :id="`articleItem-${index}`">
        <news-latest-view-item
          :news="newsInfo"
          :item="item"
          :selected-option="selectedOption"
          :index="index"
          :key="index" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    newsTarget: {
      type: Object,
      required: false,
      default: null
    },
    newsList: {
      type: Array,
      default: () => {
        return [];
      }
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data: ()=> ({
    initialized: false,
    limit: 4,
    offset: 0,
    space: null,
    isHovered: false,
    commentsSize: 0,
    likeSize: 0,
    fullDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
    seeAllUrl: '',
    selectedOption: null,
    showHeader: false,
    showSeeAll: false,
    showArticleTitle: false,
    showArticleSummary: false,
    showArticleImage: false,
    showArticleAuthor: false,
    showArticleSpace: false,
    showArticleDate: false,
    showArticleReactions: false,
    hasSmallWidthContainer: false,
    canPublishNews: false,
  }),
  computed: {
    newsInfo() {
      return this.newsList && this.newsList.filter(news => !!news);
    },
    extraClass() {
      return (!this.showHeader && !this.showSeeAll && !this.canPublishNews ) && 'mt-5' || ' ';
    }
  },
  created() {
    this.reset();
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    this.hasSmallWidthContainer = (this.$refs['news-latest-view']?.clientWidth *100 / window.screen.width) < 33;
  },
  methods: {
    refreshNewsViews(selectedTarget, selectedOption){
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