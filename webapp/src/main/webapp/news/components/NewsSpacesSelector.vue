<template>
  <div class="filterSpacesDrawer" @keydown.esc="closeFilterNewsDrawer()">
    <div class="btn" @click="openFilterNewsDrawer()">
      <i :class="{filterApplied}" class="uiIconFilter"></i>
      <span v-if="selectedSpaces.length === 0 || !filterApplied">{{ $t('news.app.filter.label') }}</span>
      <span v-if="filterApplied && selectedSpaces.length !== 0">{{ $t('news.app.filter.filterApplied') }}</span>
    </div>
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ $t('news.app.filter.bySpaces') }}</span>
        <a class="closebtn" href="javascript:void(0)" @click="closeFilterNewsDrawer()">×</a>
      </div>
      <div class="content">
        <div class="searchSpacesInput ">
          <i class="uiIconSearchSpaces"></i>
          <input
            v-model="searchSpaceText"
            :placeholder="$t('news.app.filter.drawer.searchPlaceholder')"
            type="text">
        </div>
        <div v-if="!noSpacesFound" class="spacesList">
          <div v-for="(space) in userSpaces" :key="space">
            <span :for="space.displayName" class="uiCheckbox">
              <input v-model="selectedSpaces" :id="space.displayName" :value="space.id" type="checkbox" class="checkbox">
              <span class="spaceCheckbox">
                <img :src="space.avatarUrl" :alt="space.avatarUrl" class="avatarMini">
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
      </div>
      <div class="footer">
        <button :disabled="selectedSpaces.length === 0" type="button" class="btn reset" @click="resetSelectedSpaces()">{{ $t('news.app.filter.drawer.reset') }}</button>
        <button :disabled="disableApplyButton" type="button" class="btn btn-primary" @click="applySpaceFilter()">{{ $t('news.app.filter.drawer.apply') }}</button>
        <button type="button" class="btn" @click="closeFilterNewsDrawer()">{{ $t('news.app.filter.drawer.cancel') }}</button>
      </div>
    </div>
    <div v-show="showFilterNews" class="drawer-backdrop" @click="closeFilterNewsDrawer()"></div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';
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
      open: false,
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
  watch:{
    searchSpaceText() {
      if(this.searchSpaceText.trim()) {
        clearTimeout(this.searchSpaces);
        this.searchSpaces = setTimeout(() => {
          newsServices.searchSpaces(this.searchSpaceText).then((spaces) => {
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
    this.loadUserSpaces();
  },
  methods:{
    openFilterNewsDrawer() {
      this.open = true;
      this.showFilterNews = true;
      document.body.style.overflow = 'hidden';
    },
    closeFilterNewsDrawer() {
      this.open = false;
      this.selectedSpaces = this.value;
      this.showFilterNews = false;
      this.searchSpaceText = '';
      document.body.style.overflow = 'auto';
    },
    resetSelectedSpaces() {
      this.selectedSpaces = [];
      this.filterChanged = this.filterApplied;
    },
    loadUserSpaces() {
      this.showLoadMore = true;
      newsServices.getUserSpaces(0, this.limit).then(data => {
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
      newsServices.getUserSpaces(this.limit, 0).then((resp) => resp.spaces).then(spaces => {
        spaces.forEach(space => {
          space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
          this.userSpaces.push(space);
        });
      });
      this.showLoadMore = false;
    },
    applySpaceFilter() {
      this.value = this.selectedSpaces;
      this.$emit('input', this.value);

      this.closeFilterNewsDrawer();

      this.filterApplied = this.selectedSpaces.length !== 0;
      this.filterChanged = false;
    }
  }
};
</script>