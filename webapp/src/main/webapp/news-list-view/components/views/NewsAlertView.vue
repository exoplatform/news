<template>
  <div id="critical-alert-ticker" class="mt-n13">
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
      <span>Alerts</span>
    </div>

    <div class="ticker-container ps-5 flex-grow-1">
      <!-- <a
        class="articleLink"
        id="ticker-item-$uuid"
        target="_self"
        href="$itemLink">
        <div class="ticker-article" data-ticker="item">
          <span class="ticker-articleDate">March 18</span>
          <span class="ticker-seperator">|</span>
          <span class="ticker-articleTitle">Site data recovery after OVH incident</span>
          <span class="ticker-articleSummary">We switched site data to a temporary server. We plan to switch back to the normal server next week on April 8th. Meanwhile, please do not create any new content on the site. Thank you. - ITOPs Team</span>
        </div>
      </a> -->
      <v-carousel v-model="model" hide-delimiters :show-arrows="false" interval="1000" height="20"  >
      <v-carousel-item
        v-for="(color, i) in colors"
        :key="i"
        href="#"
      >
      <a class="articleLink flex-grow-1">Slide {{ i + 1 }}</a>
      </v-carousel-item>
    </v-carousel>
     </div>  

    <div class="slide-buttons d-flex pe-5">
      <v-btn
      @click="model--"
      icon
      style="color: white">
      <v-icon>chevron_left</v-icon>
    </v-btn>
    <v-btn
      @click="model++"
      icon
      style="color: white">
      <v-icon>chevron_right</v-icon>
    </v-btn>
    <v-btn
      v-if="canPublishNews"
      @click="openDrawer"
      icon
      style="color: white">
      <v-icon>mdi-cog</v-icon>
    </v-btn>
      <!-- <div class="previous-btn" style="width: 32px;">
        <svg
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          xmlns:xlink="http://www.w3.org/1999/xlink"
          x="0px"
          y="0px"
          width="65px"
          height="40px"
          viewBox="-11 -11.5 65 66">
          <path fill="#fff" d="M25.43,33.243L14.628,22.429c-0.433-0.432-0.433-1.132,0-1.564L25.43,10.051c0.432-0.432,1.132-0.432,1.563,0  c0.431,0.431,0.431,1.132,0,1.564L16.972,21.647l10.021,10.035c0.432,0.433,0.432,1.134,0,1.564  c-0.215,0.218-0.498,0.323-0.78,0.323C25.929,33.569,25.646,33.464,25.43,33.243z"></path>
        </svg>
      </div>
      <div class="next-btn" style="width: 32px;">
        <svg
          version="1.1"
          xmlns="http://www.w3.org/2000/svg"
          xmlns:xlink="http://www.w3.org/1999/xlink"
          x="0px"
          y="0px"
          width="65px"
          height="40px"
          viewBox="-11 -11.5 65 66">
          <path fill="#fff" d="M18.022,33.569c 0.282,0-0.566-0.105-0.781-0.323c-0.432-0.431-0.432-1.132,0-1.564l10.022-10.035       L17.241,11.615c 0.431-0.432-0.431-1.133,0-1.564c0.432-0.432,1.132-0.432,1.564,0l10.803,10.814c0.433,0.432,0.433,1.132,0,1.564 L18.805,33.243C18.59,33.464,18.306,33.569,18.022,33.569z"></path>
        </svg>
      </div> -->
    
    </div>
    <news-settings-drawer ref="settingsDrawer" />

  </div>
</template>

<script>
export default {
  data () {
    return {
      canPublishNews: false,
      colors: [
          'indigo',
          'warning',
          'pink darken-2',
          'red lighten-1',
          'deep-purple accent-4',
        ],
        slides: [
          'First',
          'Second',
          'Third',
          'Fourth',
          'Fifth',
        ],
      model: 0,
      news: [],
      initialized: false,
      canPublishNews: false,
      limit: 4,
      offset: 0,
      fullDateFormat: {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      },
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
    };
  },
  created() {
    this.reset();
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
          })
          .finally(() => this.initialized = false);
      }
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
