<template>
  <v-flex class="space-suggester text-truncate">
    <exo-identity-suggester
      ref="newsSpaceSuggester"
      v-model="invitedSpaces"
      :labels="spaceSuggesterLabels"
      :include-users="false"
      :width="220"
      :search-options="searchOptions"
      name="newsSpaceAutocomplete"
      class="space-suggester newsSpaceAutocomplete"
      include-spaces
      only-redactor
      only-manager />
    <div v-if="displayedSpaces" class="identitySuggester no-border mt-0">
      <exo-news-space-item
        v-for="space in displayedSpaces"
        :key="space.identity.remoteId"
        :space="space"
        @remove-space="removeSpace" />
    </div>
  </v-flex>
</template>

<script>
export default {
  props: {
    spaces: {
      type: Array,
      default: () => null,
    },
  },
  data() {
    return {
      displayedSpaces: [],
      currentUser: null,
      invitedSpaces: []
    };
  },
  computed: {
    searchOptions() {
      return {
        spaceURL: '' ,
      };
    },
    spaceSuggesterLabels() {
      return {
        searchPlaceholder: this.$t('news.share.spaces.placeholder'),
        placeholder: this.$t('news.share.spaces.placeholder'),
        noDataLabel: this.$t('agenda.noDataLabel'),
      };
    },
  },
  watch: {
    invitedSpaces() {
      if (!this.invitedSpaces) {
        this.$nextTick(this.$refs.newsSpaceSuggester.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      if (!this.spaces) {
        this.spaces = [];
      }
      const found = false;
      if (!found) {
        this.displayedSpaces.push({
          identity: this.$suggesterService.convertSuggesterItemToIdentity(this.invitedSpaces),
        });
        this.spaces.push(this.invitedSpaces.remoteId);
      }
      this.invitedSpaces = null;
    }
  },
  methods: {
    removeSpace(space) {
      const index = this.displayedSpaces.findIndex(addedSpace => {
        return space.identity.remoteId === addedSpace.identity.remoteId
            && space.identity.providerId === addedSpace.identity.providerId;
      });
      if (index >= 0) {
        this.displayedSpaces.splice(index, 1);
      }
    },
  }
};
</script>