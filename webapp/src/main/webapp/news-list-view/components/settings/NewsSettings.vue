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
  <div v-if="showSettingsContainer" class="settings-container d-flex flex-row px-2 pt-2 pb-1">
    <div class="d-flex latestNewsTitleContainer flex-column flex-grow-1 my-1">
      <span
        v-if="showHeader"
        class="news-text-header text-capitalize-first-letter text-truncate"
        :title="newsHeader">{{ newsHeader }}</span>
    </div>
    <div :class="[showHeader && newsHeader ? 'd-flex flex-column me-2 mt-1' : 'd-flex flex-column me-2']">
      <v-icon
        class="button-open-settings"
        v-if="canPublishNews && showSettingsIcon"
        size="24"
        icon
        @click="openDrawer">
        mdi-cog
      </v-icon>
    </div>
    <div v-if="showSeeAll && !hideSeeAllButton" class="d-flex flex-column my-auto me-2">
      <v-btn
        depressed
        small
        class="button-see-all-news caption text-uppercase grey--text my-auto me-2"
        @click="seeAllNews">
        {{ $t('news.published.seeAll') }}
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    isHovering: {
      type: Boolean,
      required: false,
      default: true
    },
    hideOpenSettingButton: {
      type: Boolean,
      required: false,
      default: false
    },
    hideSeeAllButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data: () => ({
    newsHeader: '',
    seeAllUrl: 'news?filter=pinned',
    showHeader: false,
    showSeeAll: false,
    canPublishNews: false,
  }),
  computed: {
    showSettingsContainer(){
      return this.showHeader || this.showSeeAll || this.canPublishNews ;
    },
    showSettingsIcon() {
      return this.isHovering && !this.hideOpenSettingButton;
    }
  },
  created() {
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
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