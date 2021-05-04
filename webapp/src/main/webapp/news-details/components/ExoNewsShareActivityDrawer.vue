<template>
  <v-app class="shareNewsDrawer">
    <exo-drawer
      ref="shareNewsDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        {{ $t('news.share.drawer.popupTitle') }}
      </template>
      <template slot="content">
        <v-form
          v-if="news"
          ref="newsShareFrom"
          class="flex news-form share-news"
          flat>
          <div class="d-flex flex-column flex-grow-1">
            <div class="d-flex flex-row">
              <span class="mt-4 ml-2 mb-2">{{ $t('news.share.shareInSpaces') }} </span>
            </div>
            <div class="d-flex flex-row flex-grow-1 newsSpaceSuggester">
              <exo-news-space-suggester :spaces="spaces" class="ml-2" />
            </div>
            <div class="d-flex flex-row">
              <textarea
                v-model="description"
                :placeholder="$t('news.share.sharedActivityPlaceholder')"
                class="ml-2 ignore-vuetify-classes newsShareDescription">
              </textarea>
            </div>
          </div>
        </v-form>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="shareDisabled"
            class="btn btn-primary mr-2"
            @click="shareNews">
            {{ $t('news.share.share') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>

export default {
  data: () => ({
    news: null,
    spaces: [],
    description: '',
  }),
  computed: {
    shareDisabled: function() {
      return !this.spaces || this.spaces.filter(part => part !== '').length === 0;
    },
  },
  created() {
    this.$root.$on('news-share-drawer-open', news => {
      this.news = news;
      this.open();
    });
  },
  methods: {
    open() {
      if (this.$refs.shareNewsDrawer) {
        this.$refs.shareNewsDrawer.open();
      }
    },
    shareNews() {
      this.$newsServices.shareNews(this.news.newsId, this.news.activityId, this.description, this.spaces)
        .then(() => {
          this.$root.$emit('news-shared', this.news, this.spaces);
          this.closeShareNewsDrawer();
        });
    },
    closeShareNewsDrawer() {
      this.$root.$emit('news-share-drawer-closed');
      this.$refs.shareNewsDrawer.close();
      this.spaces = [];
      this.description = '';
    },
  }
};
</script>