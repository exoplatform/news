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
  <v-app class="newsEmptyTemplate border-box-sizing" flat>
    <v-main class="white">
      <v-flex class="news-empty-header d-flex mx-3 my-2">
        <v-spacer />
        <div class="d-flex flex-row newsSettingButton justify-end">
          <v-btn
            v-if="$root.canPublishNews"
            icon
            @click="openDrawer">
            <v-icon>mdi-cog</v-icon>
          </v-btn>
        </div>
      </v-flex>
      <v-flex class="d-flex news-empty-template noNews">
        <div class="ma-auto py-5 d-flex flex-column align-center noNewsContent">
          <i class="uiNoNewsIcon"></i>
          <span class="title mt-5">
            {{ $t('news.latest.noNews') }}
          </span>
        </div>
      </v-flex>
    </v-main>
  </v-app>
</template>

<script>
export default {
  methods: {
    openDrawer() {
      this.$root.$emit('news-settings-drawer-open');
    },
  }
};
</script>