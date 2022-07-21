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
  <div
    class="card">
    <a
      class="articleLink"
      target="_self"
      :href="item.url">
      <div class="imgContainer">
        <img
          class="article-illustration-img"
          :src="articleImage"
          :alt="$t('news.latest.alt.articleImage')">
      </div>
    </a>
    <div class="text-area">
      <div class="upper-row">
        <a
          class="space-link"
          id="space-link"
          target="_self"
          v-if="!isHiddenSpace && showArticleSpace"
          :href="item.spaceUrl">
          <div class="article-space">
            <img
              class="space-icon"
              :src="item.spaceAvatarUrl"
              alt="Space icon">
            <div class="space-name">{{ item.spaceDisplayName }}</div>
          </div>
        </a>
        <a
          class="articleLink"
          target="_self"
          :href="item.url">
          <div v-if="showArticleTitle" class="article-title">{{ item.title }}</div>
          <div class="article-author-date">
            <div v-if="showArticleAuthor" class="author-name">{{ item.authorDisplayName }}</div>
                
            <span v-if="showArticleAuthor && showArticleDate">,&nbsp;</span>
            <div v-if="showArticleDate" class="date">
              <date-format
                :value="displayDate"
                :format="dateFormat" />
            </div>
                
          </div>
          <div v-if="showArticleSummary" class="article-summary"> {{ item.summary }} </div>
          <div class="read-more">{{ $t('news.cards.readMore') }}</div>
        </a>
      </div>
      <div v-if="showArticleReactions" class="bottom-row">
        <div class="article-counters">
          <a
            class="reactionsLink"
            target="_self"
            :href="activityReactionsLink">
            <div class="reactions">
              <v-icon
                class="counters-icons"
                size="14">mdi-thumb-up</v-icon>
              <div class="likes-count">{{ item.likesCount }}</div>
              <v-icon
                class="counters-icons mt-1 ml-2"
                size="14">
                mdi-comment
              </v-icon>
              <div class="comments-count">{{ item.commentsCount }}</div>
            </div>
          </a>
          <a
            class="articleLink"
            target="_self"
            :href="activityReactionsLink">
            <div class="views">
              <v-icon
                class="counters-icons"
                size="16">mdi-eye</v-icon>
              <div class="views-count">{{ item.viewsCount }}</div>
            </div>
          </a>
        </div>
      </div>
    </div>
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
  }),
  computed: {
    displayDate() {
      return this.item.publishDate && this.item.publishDate.time && new Date(this.item.publishDate.time);
    },
    showArticleAuthor() {
      return this.selectedOption && this.selectedOption.showArticleAuthor;
    },
    showArticleDate() {
      return this.selectedOption && this.selectedOption.showArticleDate;
    },
    showArticleTitle() {
      return this.selectedOption && this.selectedOption.showArticleTitle;
    },
    showArticleReactions() {
      return this.selectedOption && this.selectedOption.showArticleReactions;
    },
    showArticleImage() {
      return this.selectedOption && this.selectedOption.showArticleImage;
    },
    showArticleSummary() {
      return this.selectedOption && this.selectedOption.showArticleSummary;
    },
    showArticleSpace() {
      return this.selectedOption && this.selectedOption.showArticleSpace;
    },
    articleImage() {
      return (this.showArticleImage && this.item?.illustrationURL) || '/news/images/news.png';
    },
    isHiddenSpace() {
      return this.item && !this.item.spaceMember && this.item.hiddenSpace;
    },
    activityReactionsLink() {
      return this.item && `${this.item.url}#activityReactions`;
    }
  }
};
</script>
