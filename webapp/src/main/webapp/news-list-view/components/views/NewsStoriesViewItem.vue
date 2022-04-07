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
  <div class="card">
    <a
      class="articleLink"
      target="_self"
      href="$itemLink">
      <img
        class="article-img"
        :src="articleImage"
        :alt="$t('news.latest.alt.articleImage')">
      <div class="author-date-container">
        <img
          class="author-photo"
          :src="item.authorAvatarUrl"
          alt="Author image">
        <div v-if="showArticleDate" class="author-date">
          <date-format
            :value="displayDate"
            :format="dateFormat" />
        </div>
      </div>
    </a>
    <a
      class="articleLink"
      target="_self"
      :href="item.url">
      <div class="title-container">
        <div v-if="showArticleTitle" class="article-title">{{ item.title }}</div>
        <div v-if="showArticleReactions" class="article-counters d-flex">
          <div class="likes-container mb-1">
            <v-icon class="counters-icons" size="14">mdi-thumb-up</v-icon>
            <span class="counterStyle ml-1">{{ item.likesCount }}</span>
          </div>
          <div class="comments-container ml-2">
            <v-icon
              class="counters-icons mt-1"
              size="14">
              mdi-comment
            </v-icon>
            <span class="counterStyle ml-1">{{ item.commentsCount }}</span>
          </div>
          <div class="views-container ml-2">
            <v-icon class="counters-icons" size="16">mdi-eye</v-icon>
            <span class="counterStyle">{{ item.viewsCount }}</span>
          </div>
        </div>
      </div>
    </a>
  </div>
</template>

<script>
export default {
  props: {
    item: {
      type: Object,
      required: false,
      default: null
    },
    selectedOption: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: ()=> ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    showArticleTitle: true,
    showArticleSummary: true,
    showArticleImage: true,
    showArticleAuthor: true,
    showArticleSpace: true,
    showArticleDate: true,
    showArticleReactions: true,
  }),
  computed: {
    displayDate() {
      return this.item.publishDate && this.item.publishDate.time && new Date(this.item.publishDate.time);
    },
    showArticleImage() {
      return this.selectedOption && this.selectedOption.showArticleImage;
    },
    showArticleAuthor() {
      return this.selectedOption && this.selectedOption.showArticleAuthor;
    },
    showArticleSpace() {
      return this.selectedOption && this.selectedOption.showArticleSpace;
    },
    showArticleDate() {
      return this.selectedOption && this.selectedOption.showArticleDate;
    },
    showArticleReactions() {
      return this.selectedOption && this.selectedOption.showArticleReactions;
    },
    articleImage() {
      return this.showArticleImage && this.item.illustrationURL !== null ? this.item.illustrationURL : '/news/images/news.png';
    }
  },
  created() {
    this.reset();
  },
  methods: {
    reset() {
      this.viewTemplate = this.$root.viewTemplate;
      this.viewExtensions = this.$root.viewExtensions;
      this.newsTarget = this.$root.newsTarget;
      this.newsHeader = this.$root.header;
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
    },
  }
};
</script>
