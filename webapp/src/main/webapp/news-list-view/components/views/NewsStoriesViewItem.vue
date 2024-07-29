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
  <v-hover v-slot="{ hover }">
    <div class="card">
      <news-settings
        v-if="!showSeeAll && lastItem && hover"
        hide-see-all-button
        class-button-open-settings="settingNewsButton"/>
      <a
        class="articleLink"
        target="_self"
        :href="articleUrl">
        <img
          class="article-img"
          :src="articleImage"
          :alt="$t('news.latest.alt.articleImage')">
        <div class="author-date-container">
          <img
            v-if="showArticleAuthor"
            class="author-photo"
            :src="item.authorAvatarUrl"
            :alt="$t('news.avatar.author.alt',{0:item.authorDisplayName})">
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
        :href="articleUrl">
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
  </v-hover>
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
    lastItem: {
      type: Boolean,
      default: false
    },
  },
  data: ()=> ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      showAll:false,
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
    articleImage() {
      return this.showArticleImage && this.item.illustrationURL !== null ? this.item.illustrationURL.concat('&size=140x210').toString() : '/news/images/news.png';
    },
    articleUrl() {
      return eXo.env.portal.userName !== '' ? this.item.url : `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news-detail?newsId=${this.item.id}`;
    }
  },
  created() {
    this.showSeeAll = this.$root.showSeeAll;
  }
};
</script>
