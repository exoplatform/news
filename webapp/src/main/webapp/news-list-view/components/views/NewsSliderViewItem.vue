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
  <v-row class="d-flex justify-center">
    <div v-if="showArticleAuthor" class="newsSliderOwner">
      <v-avatar size="24">
        <v-img
          class="author-image"
          :src="authorAvatarUrl"
          :alt="$t('news.avatar.author.alt',{0:authorDisplayName})" />
      </v-avatar>
      <span class="text-capitalize text--white my-auto ml-2">{{ authorDisplayName }}</span>
    </div>
    <v-icon
      v-if="!isHiddenSpace && showArticleSpace && showArticleAuthor"
      class="mx-1"
      small>
      mdi-chevron-right
    </v-icon>
    <div v-if="!isHiddenSpace && showArticleSpace" class="newsSpaceInfos me-2 my-auto">
      <v-avatar size="23" rounded>
        <v-img
          class="spaceImage"
          :src="spaceAvatarUrl"
          :alt="$t('news.space.icon.alt',{ 0:spaceDisplayName })" />
      </v-avatar>
      <a 
        :href="spaceUrl" 
        class="my-auto"
        :arial-label="$t('news.space.icon.title',{ 0:spaceDisplayName })">
        <span class="my-auto spaceName ml-2">{{ spaceDisplayName }}</span>
      </a>
    </div>
    <div v-if="showArticleDate" class="date-container d-flex">
      <v-icon class="me-1 ms-2" size="15">mdi-clock</v-icon>
      <div class="postDate flex-column me-1 my-auto">
        <date-format
          :value="displayDate"
          :format="dateFormat" />
      </div>
    </div>
    <div v-if="showArticleReactions" class="reactions-container d-flex ml-4">
      <div class="likes-container mb-1">
        <v-icon class="likeIconStyle" size="14">mdi-thumb-up</v-icon>
        <span class="counterStyle ml-1">{{ likesCount }}</span>
      </div>
      <div class="comments-container ml-2">
        <v-icon
          class="commentIconStyle mt-1"
          size="14">
          mdi-comment
        </v-icon>
        <span class="counterStyle ml-1">{{ commentsCount }}</span>
      </div>
      <div class="views-container ml-2">
        <v-icon class="views-icon" size="16">mdi-eye</v-icon>
        <span class="counterStyle">{{ viewsCount }}</span>
      </div>
    </div>
  </v-row>
</template>

<script>
export default {
  props: {
    author: {
      type: String,
      default: ''
    },
    authorDisplayName: {
      type: String,
      default: ''
    },
    spaceDisplayName: {
      type: String,
      default: ''
    },
    spaceUrl: {
      type: String,
      default: ''
    },
    spaceAvatarUrl: {
      type: String,
      default: ''
    },
    authorAvatarUrl: {
      type: String,
      default: ''
    },
    publishDate: {
      type: String,
      default: ''
    },
    activityId: {
      type: String,
      default: ''
    },
    viewsCount: {
      type: Number,
      default: 0
    },
    likesCount: {
      type: Number,
      default: 0
    },
    commentsCount: {
      type: Number,
      default: 0
    },
    spaceMember: {
      type: Boolean,
      default: false
    },
    hiddenSpace: {
      type: Boolean,
      default: false
    },
    selectedOption: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
  }),
  computed: {
    displayDate() {
      return this.publishDate && this.publishDate.time && new Date(this.publishDate.time);
    },
    isHiddenSpace() {
      return !this.spaceMember && this.hiddenSpace;
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
  }
};
</script>