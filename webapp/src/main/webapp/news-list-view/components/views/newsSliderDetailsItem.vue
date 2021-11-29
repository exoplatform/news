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
  <div>
    <div class="flex-column newsSliderOwner my-auto">
      <a :href="authorProfileUrl">
        <img :src="authorAvatarUrl">
      </a>
    </div>
    <div class="flex-column my-auto">
      <span class="text-capitalize text--white font-weight-bold body-2">{{ authorFullName }}</span>
    </div>
    <v-icon>
      mdi-chevron-right
    </v-icon>
    <div class="flex-column">
      <exo-space-avatar
        v-if="space"
        :space="space"
        :size="32"
        :labels="labels"
        class="align-center my-auto text-truncate text-capitalize white--text flex-grow-0 flex"
        bold-title
        link-style />
    </div>
    <div class="flex-column my-auto font-weight-bold body-2">
      <v-icon small class="ms-4 me-1">fas fa-clock</v-icon>
    </div>
    <div class="flex-column me-1 my-auto font-weight-bold body-2">
      <span>{{ postDate }}, 2020</span>
    </div>
    <div class="flex-column my-auto">
      <v-icon
        class="likeIconStyle baseline-vertical-align ms-6 me-2"
        size="14">
        fa-thumbs-up
      </v-icon>
    </div>
    <div class="flex-column subtitle-2 my-auto me-4">
      <span class="counterStyle font-weight-bold">{{ likeSize }}</span>
    </div>
    <div class="flex-column my-auto">
      <v-icon
        class="commentIconStyle baseline-vertical-align mx-auto me-2"
        size="14">
        fa-comment
      </v-icon>
    </div>
    <div class="counterStyle flex-column subtitle-2 my-auto me-4">
      <span class="counterStyle font-weight-bold">{{ commentsSize }}</span>
    </div>
    <div class="flex-column my-auto">
      <i class="uiIconWatch watchIconStyle baseline-vertical-align mx-auto me-2 mb-1"></i>
    </div>
    <div class="counterStyle flex-column subtitle-2 my-auto me-2">
      <span class="counterStyle font-weight-bold">{{ viewsSize }}</span>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    author: {
      type: String,
      default: ''
    },
    authorFullName: {
      type: String,
      default: ''
    },
    spaceId: {
      type: String,
      default: ''
    },
    authorAvatarUrl: {
      type: String,
      default: ''
    },
    authorProfileUrl: {
      type: String,
      default: ''
    },
    postDate: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    space: null,
    commentsSize: 0,
    likeSize: 0,
    viewsSize: 0,
  }),
  computed: {
    labels() {
      return {
        CancelRequest: this.$t('profile.CancelRequest'),
        Confirm: this.$t('profile.Confirm'),
        Connect: this.$t('profile.Connect'),
        Ignore: this.$t('profile.Ignore'),
        RemoveConnection: this.$t('profile.RemoveConnection'),
        StatusTitle: this.$t('profile.StatusTitle'),
        join: this.$t('space.join'),
        leave: this.$t('space.leave'),
        members: this.$t('space.members'),
      };
    },
  },
  created() {
    if (this.spaceId) {
      this.getSpaceById(this.spaceId);
    }
    this.retrieveComments();
    this.getActivityById();
  },
  methods: {
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },
    getActivityById() {
      this.loading = true;
      this.likeSize = 5;
      this.viewsSize = 27;
    },
    retrieveComments() {
      this.loading = true;
      this.commentsSize = 3;
    },
  }
};
</script>