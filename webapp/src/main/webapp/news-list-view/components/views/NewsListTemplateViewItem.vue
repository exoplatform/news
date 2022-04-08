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
  <a
    class="article-item-link"
    target="_self"
    :href="item.url">
    <div class="article-item-image">
      <img :src="articleImage" :alt="$t('news.latest.alt.articleImage')">
    </div>
    <div class="article-item-content">
      <span v-if="showArticleTitle" class="article-title">{{ item.title }}</span>
      <span v-if="showArticleAuthor" class="article-preTitle">{{ item.authorDisplayName }}</span>
      <div class="article-postTitle">
        <span class="article-date me-2">
          <div v-if="showArticleDate" class="flex-column">
            <date-format
              :value="displayDate"
              :format="dateFormat" />
          </div>
        </span>
        <div v-if="showArticleReactions" class="reactions">
          <v-icon class="reactions-icon-style me-1" size="12">
            mdi-thumb-up
          </v-icon>
          <div class="likes-count me-2">{{ item.likesCount }}</div>
          <v-icon class="reactions-icon-style me-1" size="12">
            mdi-comment
          </v-icon>
          <div class="comments-count me-2">{{ item.commentsCount }}</div>
        </div>
      </div>
    </div>
  </a>
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