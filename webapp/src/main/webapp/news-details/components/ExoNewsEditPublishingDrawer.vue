<template>
  <div>
    <v-overlay
      z-index="1000"
      :value="drawer"
      @click.native="drawer = false" />
    <exo-drawer
      id="postNewsDrawer"
      ref="postNewsDrawer"
      v-model="drawer"
      show-overlay
      body-classes="hide-scroll decrease-z-index-more"
      right
      @closed="closeDrawer">
      <template slot="title">
        {{ $t('news.composer.editArticle') }}
      </template>
      <template slot="content">
        <div>
          <div class="d-flex flex-column pa-2">
            <div class="d-flex flex-row font-weight-bold body-2 pa-3">
              {{ $t('news.details.editPublishing') }}
            </div>
            <div class="d-flex flex-row pa-4">
              <v-switch
                v-model="isActivityPosted"
                inset
                dense
                class="my-auto" />
              <label class="publishSectionOption my-auto">
                {{ $t('news.composer.stepper.postOnStream.option') }}
              </label>
            </div>
            <div class="d-flex flex-row font-weight-bold body-2 pa-3">
              {{ $t('news.details.editPublishing.description') }}
            </div>
            <div class="d-flex flex-row pa-4">
              <v-switch
                v-model="publish"
                inset
                dense
                class="my-auto" />
              <label class="publishSectionOption my-auto">
                {{ $t('news.details.editPublishing.option') }}
              </label>
            </div>
            <exo-news-targets-selector
              v-if="allowPublishTargeting && publish"
              id="chooseTargets"
              ref="chooseTargets"
              :news="news"
              :publish="publish"
              :allow-publish-targeting="allowPublishTargeting"
              @selected-targets="getSelectedTargets" />
          </div>
        </div>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="disabled"
            :loading="editingNews"
            class="btn btn-primary ms-2"
            @click="updateNews">
            {{ $t('news.edit.update') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </div>
</template>

<script>
export default {
  props: {
    news: {
      type: String,
      required: false,
      default: null
    },
  },
  data: () => ({
    selectedTargets: [],
    drawer: false,
    allowNotPost: false,
    publish: false,
    targets: [
      { id: 0, name: 'Latest news'},
      { id: 1, name: 'Snapshot Slider'},
      { id: 2, name: 'Homepage widget'}
    ],
    allowPublishTargeting: false,
    isActivityPosted: true,
    editingNews: false,
  }),
  watch: {
    selected() {
      this.publish = this.selected;
    },
  },
  computed: {
    selected() {
      return this.news && this.news.pinned;
    },
    disabled() {
      return (this.news && this.news.activityPosted ) === !this.isActivityPosted && this.disableTargetOption || this.showTargetInformation;
    },
    disableTargetOption() {
      if (this.publish) {
        return this.allowPublishTargeting && this.selectedTargets && this.selectedTargets.length === 0;
      }
      return this.selected === this.publish;
    },
    showTargetInformation() {
      return this.disableTargetOption && this.allowPublishTargeting && this.publish;
    },
    selectAllTargets() {
      return this.selectedTargets.length === this.targets.length;
    },
    selectSomeTarget() {
      return this.selectedTargets.length > 0 && !this.selectAllTargets;
    },
    selectionIcon() {
      if (this.selectAllTargets) {return 'mdi-close-box';}
      if (this.selectSomeTarget) {return 'mdi-minus-box';}
      return 'mdi-checkbox-blank-outline';
    },
    selectTargetLabel() {
      return this.selectAllTargets ? this.$t('news.composer.stepper.chooseTarget.deselectAllTargets') : this.$t('news.composer.stepper.chooseTarget.selectAllTargets');
    }
  },
  created() {
    this.$featureService.isFeatureEnabled('news.publishTargeting')
      .then(enabled => this.allowPublishTargeting = enabled);
    this.$root.$on('open-edit-publishing-drawer', () => {
      this.openDrawer();
    });
    $(document).click(() => {
      if (this.$refs.chooseTargets && this.$refs.chooseTargets.isMenuActive) {
        this.$refs.chooseTargets.blur();
      }
    });
  },
  methods: {
    openDrawer() {
      if (this.$refs.postNewsDrawer) {
        this.disabled = true;
        this.publish = this.news.pinned;
        this.isActivityPosted = !this.news.activityPosted;
        this.$refs.postNewsDrawer.open();
      }
    },
    getSelectedTargets(event) {
      this.selectedTargets = event;
    },
    updateNews() {
      this.editingNews = true;
      this.news.pinned = this.publish;
      this.news.activityPosted = !this.isActivityPosted;
      return this.$newsServices.updateNews(this.news, false).then(() => {
        this.editingNews = false;
        this.$emit('refresh-news', this.news.newsId);
        window.setTimeout(() => {
          this.drawer = false;
        }, 400);
      });
    },
    closeDrawer() {
      if (this.news) {
        this.publish = this.news.pinned;
      }
      this.disabled = false;
      this.$refs.postNewsDrawer.close();
    },
  }
};
</script>