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
  <div id="article-list-view">
    <v-row>
      <v-col
        class="flex-grow-0"
        v-for="(item, index) of newsInfo"
        :key="item">
        <div
          class="article"
          :id="`article-item-${index}`">
          <news-list-template-view-item
            :item="item"
            :selected-option="selectedOption"
            :key="index" />
        </div>
      </v-col>
    </v-row>
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
  },
  data: () => ({
    initialized: false,
    newsInfo: null,
    limit: 4,
    offset: 0,
    space: null,
    seeAllUrl: '',
    selectedOption: null,
    showHeader: true,
    showSeeAll: true,
    showArticleTitle: true,
    showArticleSummary: true,
    showArticleImage: true,
    showArticleAuthor: true,
    showArticleSpace: true,
    showArticleDate: true,
    showArticleReactions: true,
  }),
  created() {
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.newsInfo = newsList.news.filter(news => !!news);
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget, selectedOption) {
      this.showArticleSummary = selectedOption.showArticleSummary;
      this.showArticleTitle = selectedOption.showArticleTitle;
      this.showArticleImage = selectedOption.showArticleImage;
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