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
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <div v-else-if="content"><a :href="contentUrl">{{ contentTitle }}</a></div>
  <div v-else class="d-flex">
    <i :title="$t('analytics.errorRetrievingDataForValue', {0: value})" class="uiIconColorError my-auto"></i>
    <span class="text-no-wrap text-sub-title my-auto ml-1">
      {{ $t('analytics.deletedContent') }}
    </span>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: function () {
        return null;
      },
    }
  },
  data: () => ({
    loading: true,
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