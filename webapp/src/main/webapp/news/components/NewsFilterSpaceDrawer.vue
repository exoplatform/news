<template>
  <exo-drawer
    ref="newsSpacesFilters"
    id="newsSpacesFilters"
    right
    @opened="drawer = true"
    @closed="drawer = false">
    <template slot="title">
      {{ $t('news.app.filter.label') }}
    </template>
    <template slot="content">
      <news-filter-space-list
        ref="filterSpaceList"
        v-model="selectedOwnerIds"
        class="me-4" />
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    drawer: false,
    selectedOwnerIds: [],
  }),
  watch: {
    selectedOwnerIds() {
      this.applyFilters();
    },
  },
  created() {
    this.$root.$on('news-space-selector-drawer-open', this.open);
    this.selectedOwnerIds = this.value;
  },
  methods: {
    applyFilters() {
      if (this.value !== this.selectedOwnerIds){
        this.value = this.selectedOwnerIds;
      }
      this.$emit('input', this.value);
    },
    close() {
      this.$refs.newsSpacesFilters.close();
    },
    open() {
      this.$refs.newsSpacesFilters.open();
      this.$nextTick().then(() => {
        this.$refs.filterSpaceList.reset();
      });
    },
  },
};
</script>