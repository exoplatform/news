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
  <div class="newsSlider">
    <v-carousel
      cycle
      show-arrows-on-hover
      interval="10000"
      hide-delimiter-background
      class="sliderNewsItems fill-height">
      <v-carousel-item
        v-for="(item,i) in news"
        :key="i"
        class="carouselItem"
        eager
        dark>
        <v-img
          class="articleImage fill-height"
          :src="item.illustrationURL !== null ? item.illustrationURL : '/news/images/news.png'"
          eager />
        <v-container class="slide-text-container d-flex text-center body-2">
          <div class="flex flex-column carouselNewsInfo">
            <div class="flex flex-row" :class="!canPublishNews ? 'mt-9' : ''">
              <v-btn
                v-if="canPublishNews"
                icon
                @click="openDrawer"
                class="float-right settingNewsButton">
                <v-icon>mdi-cog</v-icon>
              </v-btn>
            </div>
            <a :href="item.url" class="flex flex-row flex-grow-1 align-center justify-center slider-header">
              <span class="articleTitle text-h4 font-weight-medium white--text">
                {{ item.title }}
              </span>
            </a>
            <div class="flex flex-row flex-grow-1 align-center mx-4 my-2">
              <span class="white--text articleSummary"> {{ item.summary }}</span>
              <news-slider-view-item
                :author="item.author"
                :author-display-name="item.authorDisplayName"
                :space-display-name="item.spaceDisplayName"
                :space-url="item.spaceUrl"
                :space-avatar-url="item.spaceAvatarUrl"
                :publish-date="item.publishDate"
                :author-avatar-url="item.authorAvatarUrl"
                :activity-id="item.activityId"
                :likes-count="item.likesCount"
                :comments-count="item.commentsCount"
                :views-count="item.viewsCount"
                :hidden-space="item.hiddenSpace"
                :space-member="item.spaceMember"
                class="d-flex flex-row newsSliderItem align-center justify-center pa-2 ms-2" />
            </div>
          </div>
        </v-container>
      </v-carousel-item>
    </v-carousel>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  props: {
    newsTarget: {
      type: String,
      required: false,
      default: 'snapshotSliderNews'
    },
  },
  data () {
    return {
      news: [],
      initialized: false,
      canPublishNews: false,
      limit: 4,
      offset: 0,
      fullDateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
    };
  },
  created() {
    this.getNewsList();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
  },
  methods: {
    openDrawer() {
      const overlayElement = document.getElementById('drawers-overlay');
      if (overlayElement) {
        overlayElement.style.display = 'block';
      }
      this.$refs.settingsDrawer.open();
    },
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.news = newsList.news;
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget){
      this.newsTarget = selectedTarget;
      this.getNewsList();
    }
  }
};
</script>