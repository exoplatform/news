<template>
  <v-list>
    <v-list-item class="px-0">
      <v-list-item-action class="me-2 ms-4">
        <v-checkbox
          v-model="selectAll"
          :indeterminate="partiallySelected"
          color="primary"
          class="ma-auto newsCheckbox"
          @click="changeAllSelection" />
      </v-list-item-action>
      <v-list-item-content>
        <news-filter-space-search
          ref="queryInput"
          v-model="query"
          class="mb-0 mt-1" />
      </v-list-item-content>
      <v-list-item-action class="ms-2">
        <select
          v-model="selectionType"
          class="width-auto my-auto subtitle-1 ignore-vuetify-classes">
          <option value="all">{{ $t('news.app.filter.drawer.all') }}</option>
          <option value="selected">{{ $t('news.app.filter.drawer.selected') }}</option>
          <option value="nonSelected">{{ $t('news.app.filter.drawer.nonSelected') }}</option>
        </select>
      </v-list-item-action>
    </v-list-item>
    <news-filter-space-item
      v-for="space in filteredSpaces"
      :key="space.id"
      :space="space"
      :space-ids="spaceIds"
      :selected-ids="value"
      @changeSelection="changeSelection" />

    <v-flex v-if="hasMore" class="d-flex my-4 border-box-sizing">
      <v-btn
        :loading="loading"
        :disabled="loading"
        class="btn mx-auto"
        @click="loadMore">
        {{ $t('news.app.filter.drawer.showMoreSpaces') }}
      </v-btn>
    </v-flex>
  </v-list>
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
    spaces: [],
    selectionType: 'all',
    selectAll: true,
    loading: false,
    query: null,
    limit: 20,
    pageSize: 20,
    totalSize: 0,
  }),
  computed: {
    filteredSpaces() {
      if (this.selectionType === 'selected') {
        return this.selectedSpaces;
      } else if (this.selectionType === 'nonSelected') {
        return this.unselectedSpaces;
      }
      return this.spaces;
    },
    partiallySelected() {
      return this.value && this.value.length && (this.hasMore || this.spaceIds.length !== this.value.length);
    },
    hasMore() {
      return this.limit < this.totalSize;
    },
    spaceIds() {
      return this.spaces && this.spaces.filter(space => space.id).map(space => Number(space.id)) || [];
    },
    selectedSpaces() {
      if (this.value === false) {
        return [];
      }
      if (this.selectAll || !this.value || !this.value.length) {
        return this.spaces;
      }
      return this.spaces && this.spaces.filter(space => this.value.indexOf(Number(space.id)) >= 0) || [];
    },
    unselectedSpaces() {
      return this.spaces && this.spaces.filter(space => !this.selectedSpaces || !this.selectedSpaces.find(s => s.id === space.id)) || [];
    },
  },
  watch: {
    limit() {
      this.retrieveSpaces();
    },
    query() {
      this.retrieveSpaces();
    },
    loading() {
      if (this.loading) {
        this.$emit('start-loading');
      } else {
        this.$emit('end-loading');
      }
    },
  },
  created() {
    this.retrieveSpaces();
  },
  methods: {
    reset() {
      if (!this.spaces || !this.spaces.length) {
        this.retrieveSpaces();
      }
      this.selectAll = !this.value || !this.value.length;
      window.setTimeout(() => this.$refs.queryInput.$el.querySelector('input').focus(), 200);
    },
    loadMore() {
      if (this.hasMore) {
        this.limit += this.pageSize;
      }
    },
    changeAllSelection() {
      if (this.selectAll) {
        this.checkAll();
      } else {
        this.uncheckAll();
      }
    },
    changeSelection(selectedSpaceIds) {
      if (this.value && this.value.length === 1 && !selectedSpaceIds.length) {
        this.uncheckAll();
      } else if (!this.query && selectedSpaceIds.length === this.spaceIds.length) {
        this.checkAll();
      } else {
        this.value = selectedSpaceIds;
        this.selectAll = false;
        this.$emit('input', this.value);
      }
    },
    checkAll() {
      if (this.query) {
        this.value = this.spaceIds.slice();
        this.selectAll = false;
      } else {
        this.value = [];
        this.selectAll = true;
      }
      this.$emit('input', this.value);
    },
    uncheckAll() {
      this.value = false;
      this.selectAll = false;
      this.$emit('input', this.value);
    },
    retrieveSpaces() {
      this.loading = true;
      this.$spaceService.getSpaces(this.query, 0, this.limit, 'member', 'identity').then(data => {
        this.spaces = data.spaces;
        this.totalSize = data.size || this.totalSize;
        return this.$nextTick();
      }).then(data => {
        if (data && data.spaces) {
          this.spaces = data.spaces;
        } else {
          this.spaces = [];
        }
        if (this.value === false) {
          this.uncheckAll();
        } else if (!this.value || !this.value.length) {
          this.selectedSpaces = this.spaces.slice();
        }
      }).then(data => {
        if (data && data.spaces) {
          if (this.query) {
            this.selectedSpaces = data.spaces.filter(space => space.displayName.indexOf(this.query) >= 0);
          } else {
            this.selectedSpaces = data.spaces;
          }
        }
      }).finally(() => this.loading = false);
    },
  },
};
</script>