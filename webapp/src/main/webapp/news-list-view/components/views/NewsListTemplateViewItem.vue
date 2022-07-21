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
      <span v-if="showArticleSummary" class="article-title">{{ item.summary }}</span>
      <div class="d-flex">
        <span v-if="showArticleAuthor" class="article-preTitle">{{ item.authorDisplayName }}</span>
        <v-icon
          v-if="showArticleSpace && showArticleAuthor"
          class="mx-1"
          small>
          mdi-chevron-right
        </v-icon>
        <span v-if="showArticleSpace" class="article-preTitle">{{ item.spaceDisplayName }}</span>
      </div>
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
  }),
  computed: {
    displayDate() {
      return this.item?.publishDate?.time && new Date(this.item.publishDate.time);
    },
    showArticleImage() {
      return this.selectedOption?.showArticleImage;
    },
    showArticleAuthor() {
      return this.selectedOption?.showArticleAuthor;
    },
    showArticleSpace() {
      return this.selectedOption?.showArticleSpace;
    },
    showArticleDate() {
      return this.selectedOption?.showArticleDate;
    },
    showArticleTitle() {
      return this.selectedOption?.showArticleTitle;
    },
    showArticleSummary() {
      return this.selectedOption?.showArticleSummary;
    },
    showArticleReactions() {
      return this.selectedOption?.showArticleReactions;
    },
    articleImage() {
      return (this.showArticleImage && this.item?.illustrationURL) || '/news/images/news.png';
    },
  },
};
</script>