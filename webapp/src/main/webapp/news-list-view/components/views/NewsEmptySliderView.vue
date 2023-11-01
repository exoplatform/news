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
  <v-hover v-slot="{ hover }">
    <div class="noNewsSlideContent">
      <v-btn
          v-if="canPublishNews && hover"
          icon
          @click="openDrawer"
          class="mt-2 me-4 float-right">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
      <div class="noNewsImage">
        <div class="bookImage"></div>
      </div>
      <div class="NewsSliderText">
        <div class="noNewsMsg">{{ $t('news.slider.yourArticleTitleGoesHere') }}</div>
      </div>
    </div>
  </v-hover>
</template>
<script>
export default {
  data () {
    return {
      canPublishNews: false,
    };
  },
  created() {
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
  },
  methods: {
    openDrawer() {
      this.$root.$emit('news-settings-drawer-open');
    },
  }
};
</script>