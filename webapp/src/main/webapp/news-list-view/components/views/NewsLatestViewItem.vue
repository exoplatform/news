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
    :href="item.url">
    <div class="articleImage">
      <img :src="item.illustrationURL !== null ? item.illustrationURL : '/news/images/news.png'" :alt="$t('news.latest.alt.articleImage')">
    </div>
    <div class="articleInfos">
      <div class="articleSpace" v-if="!isHiddenSpace">
        <img
          class="spaceImage"
          :src="item.spaceAvatarUrl"
          :alt="$t('news.latest.alt.spaceImage')">
        <span class="text-capitalize spaceName">{{ item.spaceDisplayName }}</span>
      </div>
      <span class="articleTitle">{{ item.title }}</span>
      <div class="articlePostTitle">
        <div class="reactions">
          <v-icon class="reactionIconStyle me-1" size="12">mdi-clock</v-icon>
          <div class="postDate flex-column me-2 my-auto">
            <date-format
              :value="displayDate"
              :format="dateFormat" />
          </div>
          <div class="d-flex">
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
    item: {
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
    isHiddenSpace() {
      return this.item && !this.item.spaceMember && this.item.hiddenSpace;
    }
  }
};
</script>