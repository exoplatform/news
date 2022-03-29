<template>
  <div id="critical-alert-ticker" :class="canPublishNews ? 'mt-n13' : 'mt-n5'">
    <div class="ticker-header">
      <div class="ticker-icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          xmlns:xlink="http://www.w3.org/1999/xlink"
          version="1.1"
          id="Layer_1"
          x="0px"
          y="0px"
          viewBox="0 0 512 512"
          style="enable-background:new 0 0 512 512;"
          xml:space="preserve">
          <path d="M507.494,426.066L282.864,53.537c-5.677-9.415-15.87-15.172-26.865-15.172c-10.995,0-21.188,5.756-26.865,15.172    L4.506,426.066c-5.842,9.689-6.015,21.774-0.451,31.625c5.564,9.852,16.001,15.944,27.315,15.944h449.259    c11.314,0,21.751-6.093,27.315-15.944C513.508,447.839,513.336,435.755,507.494,426.066z M256.167,167.227    c12.901,0,23.817,7.278,23.817,20.178c0,39.363-4.631,95.929-4.631,135.292c0,10.255-11.247,14.554-19.186,14.554    c-10.584,0-19.516-4.3-19.516-14.554c0-39.363-4.63-95.929-4.63-135.292C232.021,174.505,242.605,167.227,256.167,167.227z     M256.498,411.018c-14.554,0-25.471-11.908-25.471-25.47c0-13.893,10.916-25.47,25.471-25.47c13.562,0,25.14,11.577,25.14,25.47    C281.638,399.11,270.06,411.018,256.498,411.018z"></path>
        </svg>
      </div>
      <span>{{ alertHeader }}</span>
    </div>

    <div class="ticker-container ps-5 flex-grow-1">
      <v-carousel
        v-model="slider"
        hide-delimiters
        cycle
        :show-arrows="false"
        interval="5000"
        height="20"
        v-if="!emptyTemplate">
        <v-carousel-item
          v-for="(item,i) in news"
          :key="i"
          :href="item.url">
          <a class="articleLink flex-grow-2">
            <div class="ticker-article">
              <span class="ticker-articleDate">
                <date-format
                  :value="new Date(item.publishDate.time)"
                  :format="dateFormat" />
              </span>
              <span class="ticker-seperator">|</span>
              <span class="ticker-articleTitle">{{item.title}} - {{item.summary}}</span>
            </div>
          </a>
        </v-carousel-item>
      </v-carousel>
    </div>  

    <div class="slide-buttons d-flex pe-2">
      <v-btn
        @click="slider--"
        icon
        style="color: white"
        :disabled="emptyTemplate">
        <v-icon>chevron_left</v-icon>
      </v-btn>
      <v-btn
        @click="slider++"
        icon
        style="color: white"
        :disabled="emptyTemplate">
        <v-icon>chevron_right</v-icon>
      </v-btn>
      <v-btn
        v-if="canPublishNews"
        @click="openDrawer"
        icon
        style="color: white">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>

<script>
export default {
  props: {
    newsTarget: {
      type: String,
      required: false,
      default: null
    },
  },
  data () {
    return {
      canPublishNews: false,
      slider: 0,
      news: [],
      initialized: false,
      limit: 4,
      offset: 0,

      showHeader: false,
      showSeeAll: false,
      showArticleTitle: true,
      showArticleSummary: true,
      showArticleImage: true,
      showArticleAuthor: true,
      showArticleSpace: true,
      showArticleDate: true,
      showArticleReactions: true,
      seeAllUrl: '',
      selectedOption: null,
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
    };
  },
  computed: {
    emptyTemplate() {
      return !(this.news && this.news.length);
    },
    alertHeader() {
      return !this.emptyTemplate ? this.$t('news.alerts.header') : '';
    },
    
  },
  created() {
    this.reset();
    this.$root.$on('saved-news-settings', this.refreshNewsViews);
    this.getNewsList();
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
  },
  methods: {
    openDrawer() {
      this.$refs.settingsDrawer.open();
    },
    getNewsList() {
      if (!this.initialized) {
        this.$newsListService.getNewsList(this.newsTarget, this.offset, this.limit, true)
          .then(newsList => {
            this.news = newsList.news;
            this.initialized = true;
            console.log(newsList.news);

          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget, selectedOption){
      this.showArticleSummary = selectedOption.showArticleSummary;
      this.showArticleTitle = selectedOption.showArticleTitle;
      this.showArticleImage = selectedOption.showArticleImage;
      this.seeAllUrl = selectedOption.seeAllUrl;
      this.limit = selectedOption.limit;
      this.selectedOption = selectedOption;
      this.newsTarget = selectedTarget;
      this.getNewsList();
    },
    reset() {
      this.limit = this.$root.limit;
      this.showHeader = this.$root.showHeader;
      this.showSeeAll = this.$root.showSeeAll;
      this.showArticleTitle = this.$root.showArticleTitle;
      this.showArticleImage = this.$root.showArticleImage;
      this.showArticleSummary = this.$root.showArticleSummary;
      this.showArticleAuthor = this.$root.showArticleAuthor;
      this.showArticleSpace = this.$root.showArticleSpace;
      this.showArticleDate = this.$root.showArticleDate;
      this.showArticleReactions = this.$root.showArticleReactions;
      this.seeAllUrl = this.$root.seeAllUrl;
      this.selectedOption = {
        limit: this.limit,
        showHeader: this.showHeader,
        showSeeAll: this.showSeeAll,
        showArticleTitle: this.showArticleTitle,
        showArticleSummary: this.showArticleSummary,
        showArticleAuthor: this.showArticleAuthor,
        showArticleSpace: this.showArticleSpace,
        showArticleDate: this.showArticleDate,
        showArticleReactions: this.showArticleReactions,
        showArticleImage: this.showArticleImage,
        seeAllUrl: this.seeAllUrl,
      };
    },
  }
};
</script>
