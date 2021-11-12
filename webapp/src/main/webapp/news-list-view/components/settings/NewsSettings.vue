<template>
  <div class="d-flex flex-row pa-4">
    <div class="d-flex flex-column flex-grow-1 body-1 text-truncate text-uppercase grey--text my-auto">{{ headerLabel }}</div>
    <div class="d-flex flex-column">
      <v-btn
        depressed
        small
        class="caption text-uppercase grey--text my-auto me-2">
        {{ $t('news.published.seeAll') }}
      </v-btn>
    </div>
    <div class="d-flex flex-column justify-end my-auto">
      <v-btn
        icon
        @click="openDrawer">
        <v-icon>mdi-cog</v-icon>
      </v-btn>
    </div>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  props: {
    hover: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    news: null,
  }),
  methods: {
    openDrawer() {
      this.$refs.settingsDrawer.open();
    },
  },
  computed: {
    headerLabel() {
      return this.news ? this.$t('news.latest.header'): this.$t('news.latest.header.post');
    }
  }
};
</script>