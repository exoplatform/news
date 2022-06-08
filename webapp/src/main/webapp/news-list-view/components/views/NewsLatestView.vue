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
  <div id="news-latest-view"  ref="news-latest-view" class="px-2 pb-2">
    <div :class="hasSmallWidthContainer ? 'article-small-container':'article-container'">
      <div
        v-for="(item, index) of newsInfo"
        :key="item"
        :class="hasSmallWidthContainer ? 'smallWidthContainer' : 'article'"
        :id="`articleItem-${index}`">
        <news-latest-view-item
          :item="item"
          :selected-option="selectedOption"
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
  },
  data: ()=> ({
    initialized: false,
    newsInfo: null,
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
    hasSmallWidthContainer: false
  }),
  computed: {
    spaceAvatarUrl() {
      return this.space && this.space.avatarUrl;
    },
    spaceDisplayName() {
      return this.space && this.space.displayName;
    }
  },
  created() {
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
    this.hasSmallWidthContainer = (this.$refs['news-latest-view']?.clientWidth *100 / window.screen.width) < 33;
  },
  methods: {
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.newsInfo = newsList.news.filter(news => !!news);
            if (this.newsInfo && this.newsInfo[0] && this.newsInfo[0].spaceId) {
              this.getSpaceById(this.newsInfo[0].spaceId);
            }
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },
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