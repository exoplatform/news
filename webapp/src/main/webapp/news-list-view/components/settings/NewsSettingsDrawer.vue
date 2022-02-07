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
  <exo-drawer
    ref="newsSettingsDrawer"
    id="newsSettingsDrawer"
    right
    fixed
    @closed="close">
    <template slot="title">
      <div class="flex flex-row">
        <v-btn
          v-if="showAdvancedSettings"
          class="flex flex-column me-1"
          icon
          text>
          <v-icon
            @click="showAdvancedSettings = !showAdvancedSettings">
            mdi-keyboard-backspace
          </v-icon>
        </v-btn>
        <div v-else class="flex flex-column">{{ $t('news.list.settings.title') }}</div>
      </div>
    </template>
    <template slot="content">
      <form ref="form1" class="pa-2 ms-2">
        <div v-if="!showAdvancedSettings" class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row">
            <label for="name" class="listViewLabel text-subtitle-1 mt-6">
              {{ $t('news.list.settings.header') }}
            </label>
          </div>
          <div class="d-flex flex-row">
            <v-text-field
              v-model="newsHeader"
              type="string"
              name="newsHeader"
              :placeholder="$t('news.list.settings.namePlaceholder')"
              :error-messages="checkAlphanumeric"
              maxlength="100"
              class="input-block-level ignore-vuetify-classes"
              counter
              required
              outlined
              dense />
          </div>
          <div class="d-flex flex-row">
            <label for="newsTarget" class="listViewLabel text-subtitle-1 mt-6">
              {{ $t('news.list.settings.newsTarget') }}
            </label>
          </div>
          <div class="d-flex flex-row">
            <v-select
              id="newsTargets"
              ref="newsTargets"
              v-model="newsTarget"
              :items="newsTargets"
              :menu-props="{ bottom: true, offsetY: true}"
              item-text="label"
              item-value="name"
              dense
              outlined
              @click.stop>
              <template v-slot:selection="{ item, index }">
                <span :title="item.toolTipInfo">
                  {{ item.label }}
                </span>
              </template>
            </v-select>
          </div>
          <div class="d-flex flex-row">
            <label for="viewTemplate" class="listViewLabel text-subtitle-1 mt-6">
              {{ $t('news.list.settings.viewTemplate') }}
            </label>
          </div>
          <div class="d-flex flex-row">
            <v-select
              id="viewTemplates"
              ref="viewTemplates"
              v-model="viewTemplate"
              :items="displayedViewTemplates"
              :menu-props="{ bottom: true, offsetY: true}"
              item-text="label"
              item-value="name"
              dense
              outlined
              @click.stop />
          </div>
          <div class="d-flex flex-row mt-4 mx-8">
            <v-img
              :src="previewTemplate"
              class="previewTemplate" />
          </div>
        </div>
        <news-advanced-settings
          v-else
          @limit-value="limit = $event"
          @see-all-url="seeAllUrl = $event"
          @selected-option="selectedOption" />
      </form>
      <div class="d-flex flex-row mt-4 mx-8 justify-end advancedSettings">
        <span v-if="!showAdvancedSettings" @click="showAdvancedSettings = !showAdvancedSettings">{{ $t('news.list.settings.drawer.advancedSettings') }}</span>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          <template>
            {{ $t('news.button.cancel') }}
          </template>
        </v-btn>
        <v-btn
          :disabled="saving || disabled"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('news.list.settings.drawer.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    initialized: false,
    initializing: false,
    saving: false,
    viewTemplate: null,
    newsTargets: [],
    viewExtensions: {},
    newsTarget: null,
    limit: 5,
    newsHeader: '',
    showAdvancedSettings: false,
    showHeader: false,
    showSeeAll: false,
    showArticleTitle: false,
    showSummary: false,
    showArticleImage: false,
    showArticleAuthor: false,
    showArticleSpace: false,
    showArticleDate: false,
    showArticleReactions: false,
    seeAllUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news?filter=pinned`,
    selectedType: 'lastPublished',
  }),
  computed: {
    viewTemplates() {
      if (this.viewExtensions) {
        return Object.keys(this.viewExtensions).map(name => ({
          name,
          label: this.getLabel(`news.list.settings.viewTemplate.${name}`, name),
        }));
      }
      return [];
    },
    displayedViewTemplates() {
      return this.viewTemplates.filter(e=> !e.name.includes('EmptyTemplate'));
    },
    checkAlphanumeric() {
      return this.targetName && !this.targetName.trim().match(/^[\w\-\s]+$/) && this.targetName.length > 0 ? this.$t('news.list.settings.name.errorMessage') : '';
    },
    disabled() {
      return this.checkAlphanumeric !== '' || (this.newsHeader && this.newsHeader.length === 0);
    },
    previewTemplate() {
      if ( this.viewTemplate === 'NewsLatest') {
        return '/news/images/latestNews.png';
      } else if ( this.viewTemplate === 'NewsSlider') {
        return '/news/images/sliderNews.png';
      } else {
        return '';
      }
    }
  },
  watch: {
    saving() {
      if (this.saving) {
        this.$refs.newsSettingsDrawer.startLoading();
      } else {
        this.$refs.newsSettingsDrawer.endLoading();
      }
    },
    initializing() {
      if (this.initializing) {
        this.$refs.newsSettingsDrawer.startLoading();
      } else {
        this.$refs.newsSettingsDrawer.endLoading();
      }
    },
  },
  created() {
    this.disabled = true;
    this.init();
    $(document).click(() => {
      if (this.$refs.newsTargets) {
        this.$refs.newsTargets.blur();
      }
      if (this.$refs.viewTemplates) {
        this.$refs.viewTemplates.blur();
      }
    });
  },
  methods: {
    open() {
      this.reset();
      const overlayElement = document.getElementById('drawers-overlay');
      if (overlayElement) {
        overlayElement.style.display = 'block';
      }
      this.$refs.newsSettingsDrawer.open();
    },
    close() {
      this.$refs.newsSettingsDrawer.close();
      const overlayElement = document.getElementById('drawers-overlay');
      if (overlayElement) {
        overlayElement.style.display = 'none';
      }
      window.setTimeout(() =>this.showAdvancedSettings = false, 200);
    },
    reset() {
      this.viewTemplate = this.$root.viewTemplate;
      this.viewExtensions = this.$root.viewExtensions;
      this.newsTarget = this.$root.newsTarget;
      this.newsHeader = this.$root.header;
      this.limit = this.$root.limit;
      this.showHeader = this.$root.showHeader;
      this.showSeeAll = this.$root.showSeeAll;
      this.showArticleTitle = this.$root.showArticleTitle;
      this.showArticleImage = this.$root.showArticleImage;
      this.showSummary = this.$root.showSummary;
      this.showArticleAuthor = this.$root.showArticleAuthor;
      this.showArticleSpace = this.$root.showArticleSpace;
      this.showArticleReactions = this.$root.showArticleReactions;
      this.seeAllUrl = this.$root.seeAllUrl || `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news?filter=pinned`;
      this.selectedType = this.$root.selectedType || 'lastPublished';
    },
    init() {
      if (!this.initialized) {
        this.initializing = true;
        this.$newsTargetingService.getAllTargets()
          .then(newsTargets => {
            this.newsTargets = newsTargets.map(newsTarget => ({
              name: newsTarget.name,
              label: newsTarget.properties && newsTarget.properties.label && newsTarget.properties.label.length > 35 ? newsTarget.properties.label.substring(0, 35).concat('...'): newsTarget.properties.label,
              toolTipInfo: newsTarget.properties && newsTarget.properties.label
            }));
            this.initialized = true;
          })
          .finally(() => this.initializing = false);
      }
    },
    save() {
      this.saving = true;
      let selectedOption = null;
      this.$newsListService.saveSettings(this.$root.saveSettingsURL ,{
        viewTemplate: this.viewTemplate,
        newsTarget: this.newsTarget,
        header: this.newsHeader,
        showHeader: this.showHeader,
        showSeeAll: this.showSeeAll,
        showArticleTitle: this.showArticleTitle,
        showSummary: this.showSummary,
        showArticleImage: this.showArticleImage,
        showArticleAuthor: this.showArticleAuthor,
        showArticleSpace: this.showArticleSpace,
        showArticleReactions: this.showArticleReactions,
        showArticleDate: this.showArticleDate,
        seeAllUrl: this.seeAllUrl,
        selectedType: this.selectedType,
        limit: this.limit,
      })
        .then(() => {
          this.$root.viewTemplate = this.viewTemplate;
          this.$root.newsTarget = this.newsTarget;
          this.$root.header = this.newsHeader;
          this.$root.limit = this.limit;
          this.$root.showHeader = this.showHeader;
          this.$root.showSeeAll = this.showSeeAll;
          this.$root.showArticleTitle = this.showArticleTitle;
          this.$root.showSummary = this.showSummary;
          this.$root.showArticleAuthor = this.showArticleAuthor;
          this.$root.showArticleSpace = this.showArticleSpace;
          this.$root.showArticleImage = this.showArticleImage;
          this.$root.showArticleDate = this.showArticleDate;
          this.$root.showArticleReactions = this.showArticleReactions;
          this.$root.seeAllUrl = this.seeAllUrl;
          this.$root.selectedType = this.selectedType;
          selectedOption = {
            limit: this.limit,
            showHeader: this.showHeader,
            showSeeAll: this.showSeeAll,
            showArticleTitle: this.showArticleTitle,
            showSummary: this.showSummary,
            showArticleAuthor: this.showArticleAuthor,
            showArticleSpace: this.showArticleSpace,
            showArticleDate: this.showArticleDate,
            showArticleReactions: this.showArticleReactions,
            showArticleImage: this.showArticleImage,
            seeAllUrl: this.seeAllUrl,
            selectedType: this.selectedType,
          };
          this.$root.$emit('saved-news-settings', this.newsTarget, selectedOption);
          this.close();
        })
        .finally(() => {
          this.saving = false;
        });
    },
    getLabel(label, defaultLabel) {
      if (label === this.$t(label)) {
        return defaultLabel || label;
      } else {
        return this.$t(label);
      }
    },
    showAdvancedSettings() {
      this.showAdvancedSettings = !this.showAdvancedSettings;
    },
    selectedOption(selectedOption, optionValue) {
      switch (selectedOption) {
      case 'showArticleSpace':
        this.showArticleSpace = optionValue;
        break;
      case 'showArticleDate':
        this.showArticleDate = optionValue;
        break;
      case 'showArticleReactions':
        this.showArticleReactions = optionValue;
        break;
      case 'showArticleAuthor':
        this.showArticleAuthor = optionValue;
        break;
      case 'showArticleImage':
        this.showArticleImage = optionValue;
        break;
      case 'showSummary':
        this.showSummary = optionValue;
        break;
      case 'showArticleTitle':
        this.showArticleTitle = optionValue;
        break;
      case 'showSeeAll':
        this.showSeeAll = optionValue;
        break;
      case 'showHeader':
        this.showHeader = optionValue;
        break;
      case 'seeAllUrl':
        this.seeAllUrl = optionValue;
        break;
      case 'selectedType':
        this.selectedType = optionValue;
        break;
      }
    }
  },
};
</script>