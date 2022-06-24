<!--
  This file is part of the Meeds project (https://meeds.io/).
  Copyright (C) 2022 Meeds Association
  contact@meeds.io
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <div class="text--secondary">
    {{ value }}
    (<a
      v-if="contentTitle"
      :title="contentTitle"
      :href="contentUrl"
      class="text-truncate"
      rel="nofollow"
      target="_blank">
      {{ contentTitle }}
    </a>)
  </div>
</template>

<script>

export default {
  props: {
    value: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    content: null,
  }),
  computed: {
    contentTitle() {
      return this.content && this.content.title;
    },
    contentUrl() {
      return this.content && this.content.url;
    },
  },
  created() {
    if (this.value) {
      this.loading = true;
      this.$newsServices.getNewsById(this.value).then(content => {
        this.content = content;
        this.$forceUpdate();
      })
        .finally(() => this.loading = false);
    }
  },
};
</script>