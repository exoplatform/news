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
      {{ $t('news.list.settings.title') }}
    </template>
    <template slot="content">
      <form ref="form1" class="pa-2 ms-2">
        <div class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row">
            <label for="name" class="listViewLabel text-subtitle-1 mt-6">
              {{ $t('news.list.settings.name') }}
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
              @click.stop />
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
              :items="viewTemplates"
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
      </form>
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
  }),
  computed: {
    viewTemplates() {
      if (this.viewExtensions) {
        delete this.viewExtensions.NewsEmptyTemplate;
        return Object.keys(this.viewExtensions).map(name => ({
          name,
          label: this.getLabel(`news.list.settings.viewTemplate.${name}`, name),
        }));
      }
      return [];
    },
    checkAlphanumeric() {
      if (this.newsHeader && !this.newsHeader.trim().match(/^[\w\-\s]+$/) && this.newsHeader.length > 0) {
        return this.$t('news.list.settings.name.errorMessage');
      } else {
        return '';
      }
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
      this.init();
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
    },
    reset() {
      this.viewTemplate = this.$root.viewTemplate;
      this.viewExtensions = this.$root.viewExtensions;
      this.newsTarget = this.$root.newsTarget;
      this.newsHeader = this.$root.header;
    },
    init() {
      if (!this.initialized) {
        this.initializing = true;
        this.$newsTargetingService.getAllTargets()
          .then(newsTargets => {
            this.newsTargets = newsTargets.map(newsTarget => ({
              name: newsTarget.name,
              label: newsTarget.label,
            }));
            this.initialized = true;
          })
          .finally(() => this.initializing = false);
      }
    },
    save() {
      this.saving = true;
      this.$newsListService.saveSettings(this.$root.saveSettingsURL ,{
        viewTemplate: this.viewTemplate,
        newsTarget: this.newsTarget,
        header: this.newsHeader,
      })
        .then(() => {
          this.$root.viewTemplate = this.viewTemplate;
          this.$root.newsTarget = this.newsTarget;
          this.$root.header = this.newsHeader;
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
  },
};
</script>