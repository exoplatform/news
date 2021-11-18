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
    <news-empty-slider-view v-if="!items" />
    <v-carousel
      v-else
      cycle
      hide-delimiter-background
      class="sliderNewsItems">
      <v-carousel-item
        v-for="(item,i) in items"
        :key="i"
        :src="item.src"
        reverse-transition="fade-transition"
        transition="fade-transition">
        <v-btn
          icon
          @click="openDrawer"
          class="me-4 float-right">
          <v-icon>mdi-cog</v-icon>
        </v-btn>
      </v-carousel-item>
    </v-carousel>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  data () {
    return {
      items: [
        { src: 'https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg', },
        { src: 'https://cdn.vuetifyjs.com/images/carousel/sky.jpg', },
        { src: 'https://cdn.vuetifyjs.com/images/carousel/bird.jpg', },
        { src: 'https://cdn.vuetifyjs.com/images/carousel/planet.jpg', },
      ],
    };
  },
  methods: {
    openDrawer() {
      const overlayElement = document.getElementById('drawers-overlay');
      overlayElement.style.display = 'block';
      this.$refs.settingsDrawer.open();
    },
  }
};
</script>