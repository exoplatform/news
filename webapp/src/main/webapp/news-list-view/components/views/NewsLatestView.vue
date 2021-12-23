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
  <div id="news-latest-view" class="px-2 pb-2">
    <div class="article-container">
      <div
        v-for="(item, index) of newsInfo"
        :key="item"
        class="article"
        :id="`articleItem-${index}`">
        <news-latest-view-item :item="item" :key="index" />
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    newsTarget: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: ()=> ({
    initialized: false,
    newsInfo: null,
    limit: 4,
    offset: 0,
    space: null,
    isHovered: false,
    commentsSize: 0,
    likeSize: 0,
    fullDateFormat: {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  computed: {
    spaceAvatarUrl() {
      return this.space && this.space.avatarUrl;
    },
    spaceDisplayName() {
      return this.space && this.space.displayName;
    }
  },
  created() {
    this.getNewsList();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.newsInfo = newsList.news;
            if (this.newsInfo && this.newsInfo[0] && this.newsInfo[0].spaceId) {
              this.getSpaceById(this.newsInfo[0].spaceId);
            }
            this.initialized = true;
          })
          .finally(() => this.initialized = false);
      }
    },
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },

  }
};
</script>