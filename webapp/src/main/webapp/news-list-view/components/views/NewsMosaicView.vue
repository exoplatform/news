<template>
  <div id="top-news-mosaic">
    <div class="mosaic-container ma-3">
      <div 
        v-for="(item, index) of news"
        :key="index"
        class="article"
        :id="`articleItem-${index}`">
        <a
          class="articleLink"
          target="_self"
          :href="item.url">
          <img :src="showArticleImage && item.illustrationURL !== null ? item.illustrationURL : '/news/images/news.png'" :alt="$t('news.latest.alt.articleImage')">
          <div class="titleArea">
            <div v-if="showArticleDate" class="newsSpace">
              <date-format
                :value="new Date(item.publishDate.time)"
                :format="dateFormat" />
            </div>
            <div v-if="showArticleTitle" class="articleTitle">
              {{ item.title }}
            </div>
          </div>
        </a>
      </div>
    </div>
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
      showSeeAll: true,
      showArticleTitle: true,
      showArticleSummary: false,
      showArticleImage: true,
      showArticleAuthor: true,
      showArticleSpace: true,
      showArticleDate: true,
      showArticleReactions: true,
      seeAllUrl: '',
      newsHeader: '',
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
          })
          .finally(() => this.initialized = false);
      }
    },
    refreshNewsViews(selectedTarget, selectedOption) {
      this.showArticleTitle = selectedOption.showArticleTitle;
      this.showArticleDate = selectedOption.showArticleDate;
      this.showHeader = selectedOption.showHeader;
      this.selectedOption = selectedOption;
      this.newsHeader = selectedOption.header;
      this.seeAllUrl = selectedOption.seeAllUrl;
      this.limit = selectedOption.limit;
      this.newsTarget = selectedTarget;
      this.getNewsList();
    },
    reset() {
      this.limit = this.$root.limit;
      this.showHeader = this.$root.showHeader;
      this.newsHeader = this.$root.header;
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
