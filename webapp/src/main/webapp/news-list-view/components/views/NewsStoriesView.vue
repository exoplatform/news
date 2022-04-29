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
  <div id="top-news-stories">
    <card-carousel v-if="news.length">
      <news-stories-view-item
        v-for="(item, index) in news"
        :key="index"
        :item="item"
        :selected-option="selectedOption" />
      <a
        class="see-all-link"
        target="_self"
        :href="seeAllArticles">
        <div class="card" id="see-all">
          <div class="see-all-icon">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
              version="1.1"
              id="Layer_1"
              x="0px"
              y="0px"
              viewBox="0 0 330 330"
              style="enable-background:new 0 0 330 330;"
              xml:space="preserve">
              <path d="M165,0C74.019,0,0,74.019,0,165s74.019,165,165,165s165-74.019,165-165S255.981,0,165,0z M85,190  c-13.785,0-25-11.215-25-25s11.215-25,25-25s25,11.215,25,25S98.785,190,85,190z M165,190c-13.785,0-25-11.215-25-25  s11.215-25,25-25s25,11.215,25,25S178.785,190,165,190z M245,190c-13.785,0-25-11.215-25-25s11.215-25,25-25  c13.785,0,25,11.215,25,25S258.785,190,245,190z"></path>
            </svg>
          </div>
          <div class="see-all-text"> {{ $t('news.published.seeAll') }} </div>
        </div>
      </a>
    </card-carousel>
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
      limit: 10,
      offset: 0,

      showHeader: false,
      showSeeAll: false,
      showArticleTitle: true,
      showArticleSummary: false,
      showArticleImage: true,
      showArticleAuthor: true,
      showArticleSpace: false,
      showArticleDate: true,
      showArticleReactions: true,
      seeAllUrl: '',
      selectedOption: null,
    };
  },
  created() {
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
  },
  computed: {
    seeAllArticles() {
      return this.seeAllUrl && `${eXo.env.portal.context}/${eXo.env.portal.portalName}${this.seeAllUrl}`;
    }
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
    refreshNewsViews(selectedTarget, selectedOption) {
      this.showArticleSummary = selectedOption.showArticleSummary;
      this.showArticleTitle = selectedOption.showArticleTitle;
      this.showArticleImage = selectedOption.showArticleImage;
      this.seeAllUrl = selectedOption.seeAllUrl;
      this.limit = selectedOption.limit;
      this.selectedOption = selectedOption;
      this.newsTarget = selectedTarget;
      this.getNewsList();
    },
    reset() {
      this.limit = this.$root.limit;
      this.showHeader = this.$root.showHeader;
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
