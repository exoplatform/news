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
                  class="firstNewsImg"
                  aspect-ratio="2.3"
                  @click="openNews(newsInfo[0].url)">
                  <v-row align="end" class="lightbox white--text pa-2 fill-height">
                    <v-list three-line class="flex transparent">
                      <v-list-item>
                        <v-list-item-content>
                          <v-list-item-title class="subtitle-1 font-weight-bold text-uppercase white--text contentTitle" @click="openNews(newsInfo[0].url)">{{ newsInfo[0].title }}</v-list-item-title>
                          <v-list-item-subtitle
                            class="body-2 white--text contentBody"
                            @click="openNews(newsInfo[0].url)"
                            v-sanitized-html="newsInfo[0].body" />
                        </v-list-item-content>
                      </v-list-item>
                      <div class="flex d-flex flex-row body-2 white--text my-auto ms-4 mt-2">
                        <div class="flex-column my-auto me-2">{{ newsInfo[0].schedulePostDate }}</div>
                        <div class="flex-column my-auto">
                          <v-icon
                            class="baseline-vertical-align white--text ms-6 me-2"
                            size="14">
                            fa-thumbs-up
                          </v-icon>
                        </div>
                        <div class="flex-column my-auto me-4">
                          <span>{{ likeSize }}</span>
                        </div>
                        <div class="flex-column my-auto">
                          <v-icon
                            class="baseline-vertical-align white--text mx-auto me-2"
                            size="14">
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
                  pl-3
                  class="news-right-list">
                  <v-list
                    three-line
                    class="d-xs-none py-1 list-news">
                    <template v-for="item of newsInfo.slice(1)">
                      <v-list-item
                        :key="item.title"
                        class="px-0 news-item">
                        <v-list-item-avatar
                          tile
                          size="95"
                          class="mr-2 my-0">
                          <v-img :src="item.illustrationURL" @click="openNews(item.url)" />
                        </v-list-item-avatar>

                        <v-list-item-content class="pt-0 pl-3">
                          <v-list-item-title
                            class="subtitle-2 font-weight-bold text-capitalize newsRightTitle mb-1"
                            style="margin-bottom: 0px"
                            @click="openNews(item.url)"
                            v-sanitized-html="item.title" />
                          <v-list-item-subtitle
                            class="font-weight-bold newsRightBody"
                            @click="openNews(item.url)"
                            v-sanitized-html="item.body" />
                          <div class="flex d-flex flex-row my-auto mt-1">
                            <div class="flex-column subtitle-2 my-auto me-2 scheduleDateLatestNews"> {{ item.schedulePostDate }}</div>
                            <div class="flex-column my-auto">
                              <v-icon
                                class="likeIconStyle baseline-vertical-align ms-6 me-2"
                                size="14">
                                fa-thumbs-up
                              </v-icon>
                            </div>
                            <div class="flex-column subtitle-2 my-auto me-4">
                              <span class="counterStyle">{{ likeSize }}</span>
                            </div>
                            <div class="flex-column my-auto">
                              <v-icon
                                class="commentIconStyle baseline-vertical-align mx-auto me-2"
                                size="14">
                                fa-comment
                              </v-icon>
                            </div>
                            <div class="counterStyle flex-column subtitle-2 my-auto">
                              <span>{{ commentsSize }}</span>
                            </div>
                          </div>
                        </v-list-item-content>
                      </v-list-item>
                    </template>
                  </v-list>
                </v-layout>
              </v-flex>
              <div v-if="newsInfo.length === 0" class="noNews mx-auto">
                <div class="noNewsContent">
                  <i class="uiNoNewsIcon"></i>
                  <div class="noNewsTitle">{{ $t('news.latest.noNews') }}</div>
                </div>
              </div>
            </v-layout>

            <v-carousel
              v-if="newsInfo.length !== 0"
              :height="250"
              class="d-sm-none carousel-news"
              touch
              hide-delimiters>
              <v-carousel-item
                v-for="(slide, index) of newsInfo"
                :key="index"
                :src="slide.illustrationURL"
                aspect-ratio="2.3"
                @click="openNews(slide.url)">
                <v-sheet
                  color="transparent news-text"
                  height="50%"
                  width="100%"
                  class="mx-auto"
                  tile>
                  <v-list three-line class="flex item-lightbox">
                    <v-list-item class="px-2">
                      <v-list-item-content class="py-0">
                        <v-list-item-title
                          class="font-weight-bold text-uppercase white--text"
                          @click="openNews(slide.url)"
                          v-sanitized-html="slide.title" />
                        <v-list-item-subtitle
                          class="body-2 white--text"
                          @click="openNews(slide.url)"
                          v-sanitized-html="slide.body" />
                      </v-list-item-content>
                    </v-list-item>
                  </v-list>
                </v-sheet>
              </v-carousel-item>
            </v-carousel>
            <div v-else class="d-sm-none noNews">
              <div class="noNewsContent">
                <i class="uiNoNewsIcon"></i>
                <div class="noNewsTitle">{{ $t('news.latest.noNews') }}</div>
              </div>
            </div>
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
  }),
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
            this.initialized = true;
            for (let i=0; i<this.newsInfo.length; i++) {
              if (this.newsInfo[i].illustrationURL === null) {
                this.newsInfo[i].illustrationURL = '/news/images/news.png';
              }
            }
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
    openNews(url){
      if (url !== null){
        window.location.href =url;
      }
    },
  }
};
</script>
