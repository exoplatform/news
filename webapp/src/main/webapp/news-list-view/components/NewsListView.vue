<template>
  <v-app class="news-list-view-app position-relative">
    <v-hover v-slot="{ hover }">
      <v-card flat class="list-view-card">
        <v-card-text class="pa-0">
          <news-settings v-if="$root.saveSettingsURL" :hover="hover" />
          <extension-registry-component
            v-if="selectedViewExtension"
            element-class="news-list-view"
            :component="selectedViewComponent"
            :params="viewComponentParams" />
        </v-card-text>
      </v-card>
    </v-hover>
  </v-app>
</template>

<script>
export default {
  props: {
    viewTemplate: {
      type: String,
      default: null,
    },
    newsTarget: {
      type: Array,
      default: null,
    },
    limit: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    extensionApp: 'NewsList',
    extensionType: 'views',
    newsList: [],
    viewExtensions: {},
    hover: false,
    loading: false,
    hasMore: false,
  }),
  computed: {
    selectedViewExtension() {
      if (this.viewTemplate) {
        return this.viewExtensions[this.viewTemplate];
      } else if (Object.keys(this.viewExtensions).length) {
        const sortedViewExtensions = Object.values(this.viewExtensions).sort((ext1, ext2) => ext1.rank - ext2.rank);
        return sortedViewExtensions[0];
      }
      return null;
    },
    selectedViewComponent() {
      return this.selectedViewExtension && {
        componentName: this.selectedViewExtension.id,
        componentOptions: this.selectedViewExtension,
      };
    },
    viewComponentParams() {
      return {
        viewExtension: this.selectedViewExtension,
        newsTarget: this.newsTarget,
        newsList: this.newsList,
        limit: this.limit,
        hasMore: this.hasMore,
      };
    },
  },
  watch: {
    viewExtensions() {
      this.$root.viewExtensions = this.viewExtensions;
    },
  },
  created() {
    this.retrieveNewsList().finally(() => this.$root.$applicationLoaded());

    document.addEventListener(`component-${this.extensionApp}-${this.extensionType}-updated`, this.refreshViewExtensions);
    this.refreshViewExtensions();
  },
  methods: {
    retrieveNewsList() {
      this.loading = true;
      return this.$newsListService.getNewsList(this.newsTarget, this.limit + 1)
        .then(newsList => {
          this.newsList = newsList || [];
          this.hasMore = this.newsList.length > this.limit;
        })
        .finally(() => this.loading = false);
    },
    refreshViewExtensions() {
      const extensions = extensionRegistry.loadComponents(this.extensionApp)
        .filter(component => component.componentName === this.extensionType)
        .map(component => component.componentOptions);
      extensions.forEach(extension => {
        if (extension.id && (!this.viewExtensions[extension.id] || this.viewExtensions[extension.id] !== extension)) {
          this.$set(this.viewExtensions, extension.id, extension);
        }
      });
    },
  },
};
</script>