<template>
  <exo-drawer
    ref="newsFiltersDrawer"
    right
    @opened="drawer = true"
    @closed="drawer = false">
    <template slot="title">
      {{ $t('news.app.filter.bySpaces') }}
    </template>
    <template slot="content">
      <div class="content">
        <v-list>
          <v-list-item class="px-0">
            <v-scale-transition>
              <v-text-field
                v-model="searchSpaceText"
                :placeholder="$t('news.app.filterBySpacePlaceholder')"
                prepend-inner-icon="fa-filter"
                class="pa-0 my-auto" />
            </v-scale-transition>
          </v-list-item>
          <v-list-item class="px-0">
            <div v-if="!noSpacesFound" class="spacesList">
              <div v-for="(space) in userSpaces" :key="space">
                <span :for="space.displayName" class="uiCheckbox">
                  <input
                    v-model="selectedSpaces"
                    :id="space.displayName"
                    :value="space.id"
                    type="checkbox"
                    class="checkbox">
                  <span class="spaceCheckbox">
                    <img
                      :src="space.avatarUrl"
                      :alt="space.avatarUrl"
                      class="avatarMini">
                    {{ space.displayName }}</span>
                </span>
                <br>
              </div>
              <div v-if="showLoadMore" class="moreSpace">
                <div class="loadMoreSpaces" @click="loadMoreSpaces()">
                  <a>{{ $t('news.app.filter.drawer.showMoreSpaces') }}</a>
                </div>
              </div>
            </div>
            <div v-if="noSpacesFound" class="noSpacesFound">
              <span> {{ $t('news.app.filter.drawer.noSpacesFound') }} </span>
            </div>
          </v-list-item>
        </v-list>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-btn
          :disabled="selectedSpaces.length === 0"
          class="btn ms-2"
          @click="resetSelectedSpaces">
          {{ $t('news.app.filter.drawer.reset') }}
        </v-btn>
        <v-spacer />
        <v-btn
          class="btn ms-2"
          @click="close">
          {{ $t('news.app.filter.drawer.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disableApplyButton"
          class="btn btn-primary ms-2"
          @click="applySpaceFilter">
          {{ $t('news.app.filter.drawer.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      showFilterNews: false,
      drawer: false,
      selectionType: 'all',
      selectedSpaces: [],
      searchSpaceText: '',
      userSpaces: [],
      noSpacesFound: false,
      showLoadMore: true,
      filterApplied: false,
      filterChanged: false,
      limit: 10,
      searchSpaces: '',
      searchDelay: 300
    };
  },
  computed: {
    disableApplyButton() {
      return !this.filterApplied && !this.filterChanged || this.filterApplied && !this.filterChanged || !this.filterApplied && this.filterChanged && this.selectedSpaces.length === 0;
    }
  },
  watch: {
    searchSpaceText() {
      if (this.searchSpaceText.trim()) {
        clearTimeout(this.searchSpaces);
        this.searchSpaces = setTimeout(() => {
          this.$newsServices.searchSpaces(this.searchSpaceText).then((spaces) => {
            if (spaces.length) {
              this.noSpacesFound = false;
              spaces.forEach(space => {
                space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
              });
              this.userSpaces = spaces;
            } else {
              this.noSpacesFound = true;
            }
          });
        }, this.searchDelay);
        this.showLoadMore = false;
      } else {
        this.noSpacesFound = false;
        this.loadUserSpaces();
      }
    },
    selectedSpaces() {
      const filteredSpacesSorted = this.value.map(Number).sort((a, b) => a - b);
      const selectedSpacesSorted = this.selectedSpaces.map(Number).sort((a, b) => a - b);
      if (filteredSpacesSorted.length !== selectedSpacesSorted.length) {
        this.filterChanged = true;
      } else {
        let result = false;
        for (let i = 0; i < filteredSpacesSorted.length; i++) {
          if (filteredSpacesSorted[i] !== selectedSpacesSorted[i]) {
            result = true;
          }
        }
        this.filterChanged = result;
      }
    }
  },
  created() {
    this.$root.$on('news-space-selector-drawer-open', this.open);
    if (this.value.length > 0) {
      this.filterApplied = true;
      this.selectedSpaces = this.value;
    }
    this.loadUserSpaces();
  },
  methods: {
    close() {
      this.$refs.newsFiltersDrawer.close();
    },
    open() {
      this.$refs.newsFiltersDrawer.open();
    },
    resetSelectedSpaces() {
      this.selectedSpaces = [];
      this.filterChanged = this.filterApplied;
    },
    loadUserSpaces() {
      this.showLoadMore = true;
      this.$newsServices.getUserSpaces(0, this.limit,'member').then(data => {
        if (data.size < this.limit) {
          this.showLoadMore = false;
        }
        return data.spaces;
      }).then(spaces =>{
        spaces.forEach(space => {
          space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
        });
        this.userSpaces = spaces;
      });
    },
    loadMoreSpaces() {
      this.$newsServices.getUserSpaces(this.limit, 0 ,'member').then((resp) => resp.spaces).then(spaces => {
        spaces.forEach(space => {
          space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
          this.userSpaces.push(space);
        });
      });
      this.showLoadMore = false;
    },
    applySpaceFilter() {
      // eslint-disable-next-line vue/no-mutating-props
      this.value = this.selectedSpaces;
      this.$emit('input', this.value);
      this.close();
      this.filterApplied = this.selectedSpaces.length !== 0;
      this.filterChanged = false;
    }
  }
};
</script>
