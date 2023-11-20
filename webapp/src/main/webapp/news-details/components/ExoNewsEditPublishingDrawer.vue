<!--
Copyright (C) 2021 eXo Platform SAS.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
-->
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
        {{ $t('news.details.editPublishing.title') }}
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
                :disabled="!allowedTargets.length"
                inset
                dense
                class="my-auto"
                @change="removeTargets" />
              <label class="publishSectionOption my-auto">
                {{ $t('news.details.editPublishing.option') }}
              </label>
            </div>
            <div v-if="allowedTargets.length === 0" class="d-flex flex-row grey--text ms-2">
              <i class="fas fa-exclamation-triangle mx-2 mt-3"></i>
              {{ $t('news.composer.stepper.selectedTarget.noTargetAllowed') }}
            </div>
            <exo-news-targets-selector
              v-if="publish && allowedTargets.length"
              id="chooseTargets"
              ref="chooseTargets"
              :news="news"
              :publish="publish"
              :targets="allowedTargets"
              :audience="audience"
              @selected-targets="getSelectedTargets"
              @selected-audience="getSelectedAudience" />
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
    selectedAudience: null,
    audience: null,
    drawer: false,
    allowNotPost: false,
    publish: false,
    targets: [
      { id: 0, name: 'Latest news'},
      { id: 1, name: 'Snapshot Slider'},
      { id: 2, name: 'Homepage widget'}
    ],
    isActivityPosted: true,
    editingNews: false,
    disabled: false,
    allowedTargets: [],
  }),
  watch: {
    selected() {
      this.publish = this.selected;
    },
    isActivityPosted() {
      if (this.isHiddenActivity === !this.isActivityPosted && (this.publish === this.selected || (this.publish && this.selectedTargets && this.selectedTargets.length === 0))) {
        this.disabled = true;
      } else {
        this.disabled = false;
      }
    },
    publish() {
      if ((this.publish === this.selected && this.isHiddenActivity === !this.isActivityPosted) || (this.isHiddenActivity === !this.isActivityPosted && (this.publish === this.selected || (this.publish && this.selectedTargets && this.selectedTargets.length === 0)))) {
        this.disabled = true;
      } else {
        this.disabled = false;
      }     
      if (this.publish && this.news.targets && this.selectedTargets !== this.news.targets) {
        this.selectedTargets = this.news.targets;
        this.disabled = false;
        if (this.news.published) {
          this.disabled = true;
        }
      }
    },
    selectedTargets(newVal, oldVal) {
      if (this.publish && newVal.length === 0) {
        this.disabled = true;
      } else if (this.publish && this.selectedTargets !== this.news.targets && newVal !== oldVal) {
        this.disabled = false;
      }
    },
  },
  computed: {
    selected() {
      return this.news && this.news.published;
    },
    isHiddenActivity() {
      return this.news && this.news.activityPosted;
    },
    disableTargetOption() {
      return this.selectedTargets && this.selectedTargets.length === 0;
    },
    showTargetInformation() {
      return this.disableTargetOption && this.publish;
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
    this.selectedAudience = this.news.audience === 'all' ? this.$t('news.composer.stepper.audienceSection.allUsers') : this.$t('news.composer.stepper.audienceSection.onlySpaceMembers');
    this.audience = this.news.audience;
    this.getAllowedTargets();
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
      if (this.news) {
        this.publish = this.news.published;
        this.isActivityPosted = !this.news.activityPosted;
      }
      if (this.$refs.postNewsDrawer) {
        this.disabled = true;
        this.$refs.postNewsDrawer.open();
      }
    },
    getSelectedTargets(selectedTargets) {
      this.selectedTargets = selectedTargets;
    },
    getSelectedAudience(selectedAudience) {
      this.selectedAudience = selectedAudience;
      this.disabled = false;
    },
    updateNews() {
      this.editingNews = true;
      this.news.published = this.publish;
      this.news.activityPosted = !this.isActivityPosted;
      this.news.targets = this.selectedTargets;
      if (this.publish) {
        this.news.audience = this.selectedAudience === this.$t('news.composer.stepper.audienceSection.allUsers') ? 'all' : 'space';
      }
      else {
        this.news.audience = null;
      }
      return this.$newsServices.updateNews(this.news, false).then(() => {
        this.editingNews = false;
        const message = this.$t('news.composer.alert.success.UpdateTargets');
        this.$root.$emit('news-notification-alert', {
          message,
          type: 'success',
        });
        this.$emit('refresh-news', this.news.newsId);
        window.setTimeout(() => {
          this.drawer = false;
        }, 400);
      })
        .catch(() => {
          this.editingNews = false;
          const message = this.$t('news.composer.alert.error.UpdateTargets');
          this.$root.$emit('news-notification-alert', {
            message,
            type: 'error',
          });
        });
    },
    removeTargets() {
      this.selectedTargets = [];
    },
    closeDrawer() {
      this.disabled = false;
      this.$refs.postNewsDrawer.close();
    },
    getAllowedTargets() {
      this.$newsTargetingService.getAllowedTargets()
        .then(targets => {
          this.allowedTargets = targets.map(target => ({
            name: target.name,
            label: target.properties && target.properties.label && target.properties.label.length > 35 ? target.properties.label.substring(0, 35).concat('...'): target.properties.label,
            tooltipInfo: target.properties && target.properties.label,
            description: target.properties && target.properties.description,
            restrictAudience: target.restrictAudience,
            groupAndSpacePublisher: target.groupAndSpacePublisher
          }));
        });
    },
  }
};
</script>