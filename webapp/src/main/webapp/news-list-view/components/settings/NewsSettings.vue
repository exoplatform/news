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
  <div class="d-flex flex-row pa-2">
    <div class="d-flex latestNewsTitleContainer flex-column flex-grow-1 my-1">
      <span
        v-if="showHeader"
        class="headerLatestNews body-1 text-uppercase text-sub-title text-truncate text-center"
        :title="newsHeader">{{ newsHeader }}</span>
    </div>
    <div class="d-flex flex-column me-2">
      <v-btn
        v-if="$root.canPublishNews"
        icon
        @click="openDrawer">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
    <div v-if="showSeeAll" class="d-flex flex-column my-auto me-2">
      <v-btn
        depressed
        small
        class="caption text-uppercase grey--text my-auto me-2"
        @click="seeAllNews">
        {{ $t('news.published.seeAll') }}
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  data: () => ({
    newsHeader: '',
    seeAllUrl: 'news?filter=pinned',
    showHeader: false,
    showSeeAll: false,
  }),
  created() {
    this.$root.$on('saved-news-settings', (newsTarget, selectedOptions) => {
      this.newsHeader = selectedOptions.header;
      this.seeAllUrl = selectedOptions.seeAllUrl;
      this.showSeeAll = selectedOptions.showSeeAll;
      this.showHeader = selectedOptions.showHeader;
    });
    this.newsHeader = this.$root.header;
    this.seeAllUrl = this.$root.seeAllUrl;
    this.showSeeAll = this.$root.showSeeAll;
    this.showHeader = this.$root.showHeader;
  },
  methods: {
    openDrawer() {
      this.$root.$emit('news-settings-drawer-open');
    },
    seeAllNews() {
      window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}${this.seeAllUrl}`;
    }
  },
};
</script>