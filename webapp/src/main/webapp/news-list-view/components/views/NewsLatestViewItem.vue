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
  <a
    class="articleLink"
    target="_self"
    :href="articleUrl"
    :arial-label="$t('news.space.icon.title',{ 0:item.spaceDisplayName })">
    <div class="articleImage" v-if="showImage">
      <img 
        :src="articleImg"
        :alt="$t('news.latest.alt.articleImage')">
    </div>
    <div class="articleInfos">
      <div class="articleSpace" v-if="!isHiddenSpace && showArticleSpace">
        <img
          class="spaceImage"
          :src="item.spaceAvatarUrl"
          :alt="$t('news.space.icon.alt',{ 0:item.spaceDisplayName })">
        <span class="spaceName">{{ item.spaceDisplayName }}</span>
      </div>
      <span v-if="showArticleTitle" class="articleTitle text-color text-body-1">{{ item.title }}</span>
      <div class="articlePostTitle">
        <div class="reactions">
          <v-icon
            v-if="showArticleDate"
            class="reactionIconStyle me-1"
            size="12">mdi-clock</v-icon>
          <div v-if="showArticleDate" class="postDate flex-column me-2 my-auto">
            <date-format
              :value="displayDate"
              :format="dateFormat" />
          </div>
          <div v-if="showArticleReactions" class="d-flex">
            <v-icon class="reactionIconStyle me-1" size="12">
              mdi-thumb-up
            </v-icon>
            <div class="likesCount me-2">{{ item.likesCount }}</div>
            <v-icon class="reactionIconStyle commentStyle me-1" size="12">
              mdi-comment
            </v-icon>
            <div class="commentsCount me-2">{{ item.commentsCount }}</div>
            <v-icon class="reactionIconStyle me-1" size="12">
              mdi-eye
            </v-icon>
            <div class="viewCount">{{ item.viewsCount }}</div>
          </div>
        </div>
      </div>
    </div>
  </a>
</template>
<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: null
    },
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
    index: {
      type: Number,
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
    showArticleSummary: false,
    showArticleImage: true,
    showArticleAuthor: false,
    showArticleSpace: true,
    showArticleDate: true,
    showArticleReactions: true,
  }),
  computed: {
    articleImg(){
      return this.showImage && this.img ;
    },
    showImage(){
      return  this.showArticleImage || (!this.showArticleImage && !this.index );
    },
    img() {
      return this.illustrationURL() || '/news/images/news.png';
    },
    displayDate() {
      return this.item.publishDate && this.item.publishDate.time && new Date(this.item.publishDate.time);
    },
    isHiddenSpace() {
      return this.item && !this.item.spaceMember && this.item.hiddenSpace;
    },
    articleUrl() {
      return eXo.env.portal.userName !== '' ? this.item.url : `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news-detail?newsId=${this.item.id}`;
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
    illustrationURL(){
      if (this.news.length > 1) {
        if (this.index === 0){
          return this.item.illustrationURL?.concat('&size=700x344').toString();
        } else {
          return this.item.illustrationURL?.concat('&size=107x107').toString();
        }
      } else {
        return this.item.illustrationURL?.concat('&size=1410x344').toString();
      }
    }
  }
};
</script>