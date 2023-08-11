<!--
  Copyright (C) 2022 eXo Platform SAS.
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
  <div class="text--secondary">
    <a
      v-if="contentTitle"
      :title="contentTitle"
      :href="contentUrl"
      class="text-truncate"
      rel="nofollow"
      target="_blank">
      {{ contentTitle }}
    </a>
    <p>
      ({{ attrValue }})
    </p>
  </div>
</template>

<script>

export default {
  props: {
    attrValue: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    content: null,
  }),
  computed: {
    contentTitle() {
      return this.content?.title;
    },
    contentUrl() {
      return this.content?.url;
    },
  },
  created() {
    if (this.attrValue) {
      this.loading = true;
      this.$newsServices.getNewsById(this.attrValue).then(content => {
        this.content = content;
        this.$forceUpdate();
      })
        .finally(() => this.loading = false);
    }
  },
};
</script>