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
  <div id="news-latest-view" class="px-2 pb-2">
    <div class="article-container">
      <div
        v-for="(item, index) of newsInfo"
        :key="item"
        class="article"
        :id="`articleItem-${index}`">
        <a
          class="articleLink"
          target="_self"
          :href="item.url">
          <div class="articleImage">
            <img :src="item.illustrationURL" :alt="$t('news.latest.alt.articleImage')">
          </div>
          <div class="articleInfos">
            <div class="articleSpace">
              <img class="spaceImage" :src="spaceAvatarUrl" :alt="$t('news.latest.alt.spaceImage')">
              <span class="text-capitalize spaceName">{{ spaceDisplayName }}</span>
            </div>
            <span class="articleTitle">Welcome to your new digital workplace platform! Take this quick tour to discover its features</span>
            <div class="articlePostTitle">
              <span class="articleDate">{{ item.postDate }}</span>
              <div class="reactions">
                <v-icon
                  class="likeIconStyle me-1"
                  size="12">
                  fa-thumbs-up
                </v-icon>
                <div class="likesCount me-2">{{ likeSize }}</div>
                <v-icon
                  class="commentIconStyle me-1"
                  size="12">
                  fa-comment
                </v-icon>
                <div class="commentsCount">{{ commentsSize }}</div>
              </div>
            </div>
          </div>
        </a>
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
    limit: 0,
    commentsSize: 0,
    likeSize: 0,
    space: null,
    isHovered: false,
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
    this.getNewsList();
    this.retrieveComments();
    this.getActivityById();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.limit)
          .then(newsList => {
            this.newsInfo = newsList;
            if (this.newsInfo && this.newsInfo[0] && this.newsInfo[0].spaceId) {
              this.getSpaceById(this.newsInfo[0].spaceId);
            }
            this.initialized = true;
            this.newsInfo.forEach(function (item, i) {
              if (item.illustrationURL === null) {
                this.newsInfo[i].illustrationURL = '/news/images/news.png';
              }
            });
          })
          .finally(() => this.initialized = false);
      }
    },
    getActivityById() {
      this.loading = true;
      this.likeSize = 5;
    },
    retrieveComments() {
      this.loading = true;
      this.commentsSize = 3;
    },
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },
    openNews(url){
      if (url !== null){
        window.location.href = url;
      }
    },
  }
};
</script>
