<template>
  <v-app
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
          px-3>
          <v-layout
            row
            mx-0
            align-center>
            <v-flex
              d-flex
              xs12
              sm6>
              <v-card v-if="isShowHeader"
                      flat
                      color="transparent">
                <v-card-text class="body-1 text-uppercase grey--text px-0">
                  {{ header }}
                </v-card-text>
              </v-card>
            </v-flex>
            <v-flex
              d-flex
              xs12
              sm6
              justify-end>
              <v-btn
                depressed
                small
                class="caption text-uppercase grey--text d-sm-flex" @click="openAllNewsPinned(url)" > {{ seeAll }}</v-btn>
            </v-flex>
          </v-layout>
        </v-flex>
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
                v-if="typeof newsInfo[0] !== 'undefined'"
                :src="newsInfo[0].illustrationURL"
                class="firstNewsImg"
                aspect-ratio="2.3"
                @click="openNews(newsInfo[0].url)">
                <v-row align="end" class="lightbox white--text pa-2 fill-height">
                  <v-list three-line class="flex transparent">
                    <v-list-item>
                      <v-list-item-content>
                        <v-list-item-title class="subtitle-1 font-weight-bold text-uppercase white--text contentTitle" @click="openNews(newsInfo[0].url)">{{ newsInfo[0].title }}</v-list-item-title>
                        <v-list-item-subtitle class="body-2 white--text contentBody" @click="openNews(newsInfo[0].url)" v-html="newsInfo[0].body"/>
                      </v-list-item-content>
                    </v-list-item>
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
                        <v-img :src="item.illustrationURL" @click="openNews(item.url)"/>
                      </v-list-item-avatar>

                      <v-list-item-content class="pt-0 pl-3">
                        <v-list-item-title class="subtitle-2 font-weight-bold text-uppercase  rightTitle" style="margin-bottom: 0px" @click="openNews(item.url)" v-html="item.title"/>
                        <v-list-item-subtitle class=" grey-color rightBody" @click="openNews(item.url)" v-html="item.body"/>
                      </v-list-item-content>
                    </v-list-item>
                  </template>
                </v-list>

              </v-layout>
            </v-flex>
            <div v-if="newsInfo.length === 0" class="noNews">
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
                      <v-list-item-title class="font-weight-bold text-uppercase white--text" @click="openNews(slide.url)" v-html="slide.title"></v-list-item-title>
                      <v-list-item-subtitle class="body-2 white--text" @click="openNews(slide.url)" v-html="slide.body"></v-list-item-subtitle>
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
</template>
<script>
export default {
  props: {
    newsInfo: {
      type: Object,
      required: false,
      default: function () {
        return new Object();
      }
    },
    seeAll: {
      type: String,
      required: false,
      default: null
    },
    header: {
      type: String,
      required: false,
      default: null
    },
    url: {
      type: String,
      required: false,
      default: null
    },
    isShowHeader: {
      type: Boolean,
      required: false,
      default: true
    },
  },
  created() {
    for (let i=0; i<this.newsInfo.length; i++){
      if(this.newsInfo[i].illustrationURL === null) {
        this.newsInfo[i].illustrationURL = '/news/images/newsImageDefault.png';
      }
    }
  },
  methods:{
    openNews(url){
      if (url !== null){
        window.location.href =url;
      }
    },openAllNewsPinned(url){
      if (url !== null){
        window.location.href =url;
      }
    }
  }
};
</script>

