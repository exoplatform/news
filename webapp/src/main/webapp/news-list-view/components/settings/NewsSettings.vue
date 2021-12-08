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
  <div class="d-flex flex-row mx-3 my-1">
    <div class="d-flex flex-column flex-grow-1 body-1 text-uppercase text-sub-title text-truncate my-auto">{{ $t('news.latest.header') }}</div>
    <div class="d-flex flex-column me-2">
      <v-btn
        icon
        @click="openDrawer">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
    <div class="d-flex flex-column justify-end my-auto">
      <v-btn
        depressed
        small
        class="caption text-uppercase grey--text my-auto me-2"
        @click="seeAllNews">
        {{ $t('news.published.seeAll') }}
      </v-btn>
    </div>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  methods: {
    openDrawer() {
      this.$refs.settingsDrawer.open();
    },
    seeAllNews() {
      window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news?filter=pinned`;
    }
  },
};
</script>