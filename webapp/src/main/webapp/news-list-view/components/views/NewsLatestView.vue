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
    <news-empty-latest-view v-if="!newsInfo" />
    <v-app
      v-else
      id="latestNewsDetails"
      class="VuetifyApp"
      flat>
      <v-container pa-0>
        <v-layout
          row
          mx-0
          class="white">
          <v-flex
            d-flex
            xs12
            px-3
            pb-3
            class="news-container">
            <v-layout
              row
              wrap
              mx-0
              class="d-none d-sm-flex">
              <v-flex
                d-flex
                xs12
                sm6>
                <v-img
                  v-if="newsInfo && typeof newsInfo[0] !== 'undefined'"
                  :src="newsInfo[0].illustrationURL"
                  aspect-ratio="2.3"
                  class="leftSideNewsImg"
                  @click="openNews(newsInfo[0].url)">
                  <v-row align="end" class="white--text pa-2 fill-height">
                    <v-list three-line class="flex leftSideText transparent">
                      <v-list-item>
                        <v-list-item-content>
                          <v-list-item-title>
                            <div class="flex d-flex flex-row">
                              <div class="flex-column newsSpaceInfos me-2 my-auto">
                                <a :href="spaceUrl">
                                  <img :src="spaceAvatarUrl">
                                </a>
                              </div>
                              <div class="flex-column text--white my-auto">
                                <a :href="spaceUrl">
                                  <span class="text-capitalize spaceName">{{ spaceDisplayName }}</span>
                                </a>
                              </div>
                            </div>
                          </v-list-item-title>
                          <v-list-item-subtitle
                            class="mainNewsBody text-truncate white--text"
                            @click="openNews(newsInfo[0].url)"
                            v-sanitized-html="newsInfo[0].body" />
                        </v-list-item-content>
                      </v-list-item>
                      <div class="flex d-flex flex-row white--text my-auto ms-4 mt-2">
                        <div class="flex-column my-auto me-2 leftPostDate">{{ newsInfo[0].postDate }}</div>
                        <div class="flex-column my-auto">
                          <v-icon
                            class="baseline-vertical-align white--text ms-6 me-2"
                            size="12">
                            fa-thumbs-up
                          </v-icon>
                        </div>
                        <div class="flex-column my-auto me-2 leftTextSize">
                          <span>{{ likeSize }}</span>
                        </div>
                        <div class="flex-column my-auto">
                          <v-icon
                            class="baseline-vertical-align white--text mx-auto me-2 leftTextSize"
                            size="12">
                            fa-comment
                          </v-icon>
                        </div>
                        <div class="flex-column my-auto">
                          <span>{{ commentsSize }}</span>
                        </div>
                      </div>
                    </v-list>
                  </v-row>
                </v-img>
              </v-flex>
              <v-flex
                d-flex
                xs12
                sm6
                align-start>
                <v-layout
                  row
                  wrap
                  mx-0
                  pl-2
                  class="news-right-list">
                  <v-list
                    three-line
                    class="d-xs-none py-0 list-news">
                    <template v-for="(item, index) of newsInfo.slice(1)">
                      <v-list-item
                        :key="item.title"
                        :id="`item-news${index}`"
                        @mouseenter="applyItemClass(index)"
                        @mouseleave="applyItemClass(index)"
                        class="px-0 news-item">
                        <v-list-item-avatar
                          tile
                          size="95"
                          class="mr-2 my-0">
                          <v-img :src="item.illustrationURL" @click="openNews(item.url)" />
                        </v-list-item-avatar>

                        <v-list-item-content class="pt-0 pl-3 my-auto">
                          <v-list-item-title
                            class="text-capitalize newsRightTitle mb-1"
                            style="margin-bottom: 0px"
                            @click="openNews(item.url)"
                            v-sanitized-html="item.title" />
                          <v-list-item-subtitle
                            class="newsRightBody my-2"
                            @click="openNews(item.url)"
                            v-sanitized-html="item.body" />
                          <div class="flex d-flex flex-row my-auto">
                            <div class="flex-column my-auto me-2 postDateNews"> {{ item.postDate }}</div>
                            <div class="flex-column my-auto">
                              <v-icon
                                class="likeIconStyle baseline-vertical-align ms-6 me-2"
                                size="12">
                                fa-thumbs-up
                              </v-icon>
                            </div>
                            <div class="flex-column my-auto me-2">
                              <span class="counterStyle">{{ likeSize }}</span>
                            </div>
                            <div class="flex-column my-auto">
                              <v-icon
                                class="commentIconStyle baseline-vertical-align mx-auto me-2"
                                size="12">
                                fa-comment
                              </v-icon>
                            </div>
                            <div class="counterStyle flex-column my-auto">
                              <span>{{ commentsSize }}</span>
                            </div>
                          </div>
                        </v-list-item-content>
                      </v-list-item>
                    </template>
                  </v-list>
                </v-layout>
              </v-flex>
            </v-layout>
          </v-flex>
        </v-layout>
      </v-container>
    </v-app>
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
    limit: 0,
    commentsSize: 0,
    likeSize: 0,
    space: null,
    isHovered: false,
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
    spaceUrl() {
      let groupId;
      if (this.space && this.space.groupId) {
        groupId = this.space.groupId.replace(/\//g, ':');
        return `${eXo.env.portal.context}/g/${groupId}/`;
      }
      return `${eXo.env.portal.context}/dw/home/`;
    },
    spaceAvatarUrl() {
      return this.space && this.space.avatarUrl;
    },
    spaceDisplayName() {
      return this.space && this.space.displayName;
    }
  },
  created() {
    this.getNewsList();
    this.retrieveComments();
    this.getActivityById();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.limit)
          .then(newsList => {
            this.newsInfo = newsList;
            if (this.newsInfo && this.newsInfo[0] && this.newsInfo[0].spaceId) {
              this.getSpaceById(this.newsInfo[0].spaceId);
            }
            this.initialized = true;
            this.newsInfo.forEach(function (item, i) {
              if (item.illustrationURL === null) {
                this.newsInfo[i].illustrationURL = '/news/images/news.png';
              }
            });
          })
          .finally(() => this.initialized = false);
      }
    },
    getActivityById() {
      this.loading = true;
      this.likeSize = 5;
    },
    retrieveComments() {
      this.loading = true;
      this.commentsSize = 3;
    },
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },
    openNews(url){
      if (url !== null){
        window.location.href = url;
      }
    },
    applyItemClass(index) {
      this.isHovered = !this.isHovered;
      const elementNewTop = document.getElementById(`item-news${index}`);
      return this.isHovered ? elementNewTop.classList.add('newsRightBodyHover') : elementNewTop.classList.remove('newsRightBodyHover');
    }
  }
};
</script>
