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
    <news-empty-slider-view v-if="!news" />
    <v-carousel
      v-else
      hide-delimiter-background
      class="sliderNewsItems">
      <v-carousel-item
        v-for="(item,i) in news"
        :key="i"
        :src="item.illustrationURL"
        reverse-transition="fade-transition"
        transition="fade-transition">
        <v-btn
          icon
          @click="openDrawer"
          class="me-4 float-right">
          <v-icon>mdi-cog</v-icon>
        </v-btn>
        <div class="flex flex-column carouselNewsInfo">
          <div class="flex flex-row flex-grow-1 align-center">
            <span class="headline font-weight-bold white--text">
              {{ item.title }}
            </span>
          </div>
          <div class="flex flex-row flex-grow-1 align-center">
            <span class="subtitle-2 font-weight-bold white--text my-2">
              {{ item.body }}
            </span>
          </div>
          <news-slider-details-item
            :author="item.author"
            :author-full-name="item.authorFullName"
            :space-id="item.spaceId"
            :post-date="item.postDate"
            class="d-flex flex-row align-center pa-2 ms-2" />
        </div>
      </v-carousel-item>
    </v-carousel>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  data () {
    return {
      news: [],
      initialized: false,
    };
  },
  created() {
    this.getNewsList();
  },
  methods: {
    openDrawer() {
      const overlayElement = document.getElementById('drawers-overlay');
      overlayElement.style.display = 'block';
      this.$refs.settingsDrawer.open();
    },
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList()
          .then(newsList => {
            this.news = newsList;
            this.initialized = true;
            for (let i=0; i<this.news.length; i++) {
              if (this.news[i].illustrationURL === null) {
                this.news[i].illustrationURL = '/news/images/news.png';
              }
            }
          })
          .finally(() => this.initialized = false);
      }
    },
  }
};
</script>