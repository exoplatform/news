<!--
Copyright (C) 2022 eXo Platform SAS.

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
  <v-list
    class="newsAdvancedSettings"
    subheader
    two-line>
    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.maxArticle') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <input
          v-model="limit"
          type="number"
          id="maxArticle"
          name="maxArticle"
          @change="$emit('limit-value', limit)"
          class="maxArticle input-block-level ignore-vuetify-classes">
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showListHeader') }}
        </v-list-item-title>
        <v-list-item-subtitle class="advancedSettingsLabel">
          {{ $t('news.list.settings.drawer.advancedSettings.displayTextHeader') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showHeader"
          dense
          :disabled="displaySliderButton || displayMosaicButtons || displayStoriesButtons"
          @change="selectedOption('showHeader', showHeader)"
          class="displaySeeAllButton my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showSeeAll') }}
        </v-list-item-title>
        <v-list-item-subtitle class="advancedSettingsLabel">
          {{ $t('news.list.settings.drawer.advancedSettings.displaySeeAll') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showSeeAll"
          dense
          @change="selectedOption('showSeeAll', showSeeAll)"
          class="displayHeaderTitle my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item v-if="showSeeAll">
      <v-list-item-content class="py-0">
        <v-list-item-action>
          <v-text-field
            v-model="seeAllUrl"
            :placeholder="$t('news.list.settings.drawer.advancedSettings.enterUrl')"
            :rules="[urlRules.required]"
            autofocus
            type="url"
            id="seeLink"
            name="seeLink"
            required
            outlined
            dense
            @keyup="$emit('see-all-url', seeAllUrl)"
            @change="$emit('see-all-url', seeAllUrl)"
            class="seeLink input-block-level ignore-vuetify-classes my-0" />
        </v-list-item-action>
      </v-list-item-content>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleTitle') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleTitle"
          dense
          @change="selectedOption('showArticleTitle', showArticleTitle)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleSummary') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleSummary"
          dense
          :disabled="displayLatestButton || displayAlertsButtons || displayMosaicButtons || displayStoriesButtons"
          @change="selectedOption('showArticleSummary', showArticleSummary)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleImage') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleImage"
          :disabled="displayAlertsButtons"
          dense
          @change="selectedOption('showArticleImage', showArticleImage)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleAuthor') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleAuthor"
          dense
          :disabled="displayLatestButton || displayAlertsButtons"
          @change="selectedOption('showArticleAuthor', showArticleAuthor)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleDate') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleDate"
          dense
          @change="selectedOption('showArticleDate', showArticleDate)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleSpace') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleSpace"
          dense
          :disabled="displayStoriesButtons"
          @change="selectedOption('showArticleSpace',showArticleSpace)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title class="advancedSettingsLabel font-weight-regular">
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleReactionsCounter') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleReactions"
          :disabled="displayAlertsButtons"
          dense
          @change="selectedOption('showArticleReactions', showArticleReactions)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  props: {
    showArticleSummary: {
      type: Boolean,
      default: false,
    },
    showArticleAuthor: {
      type: Boolean,
      default: false,
    },
    showHeader: {
      type: Boolean,
      default: false,
    },
    showSeeAll: {
      type: Boolean,
      default: false,
    },
    viewTemplate: {
      type: String,
      default: '',
    },
    seeAllUrl: {
      type: String,
      default: '',
    }
  },
  data: () => ({
    showArticleTitle: false,
    showArticleImage: false,
    showArticleSpace: false,
    showArticleDate: false,
    showArticleReactions: false,
    limit: null,
    urlRules: {
      required: value => value == null || !!(value?.length),
    },
  }),
  computed: {
    displaySliderButton() {
      return this.viewTemplate === 'NewsSlider';
    },
    displayLatestButton() {
      return this.viewTemplate === 'NewsLatest';
    },
    displayAlertsButtons() {
      return this.viewTemplate === 'NewsAlert';
    },
    displayMosaicButtons() {
      return this.viewTemplate === 'NewsMosaic';
    },
    displayStoriesButtons() {
      return this.viewTemplate === 'NewsStories';
    },
  },
  created() {
    this.reset();
  },
  methods: {
    selectedOption(selectedOption, optionValue) {
      this.$emit('selected-option', selectedOption, optionValue);
    },
    reset() {
      this.viewExtensions = this.$root.viewExtensions;
      this.newsTarget = this.$root.newsTarget;
      this.newsHeader = this.$root.header;
      this.limit = this.$root.limit;
      this.showHeader = this.viewTemplate === 'NewsSlider' || this.viewTemplate === 'NewsMosaic' || this.viewTemplate === 'NewsStories' ? false : this.$root.showHeader;
      this.showSeeAll = this.$root.showSeeAll;
      this.showArticleTitle = this.$root.showArticleTitle;
      this.showArticleImage = this.viewTemplate === 'NewsAlert' ? false : this.$root.showArticleImage;
      this.showArticleSummary = this.viewTemplate === 'NewsLatest' || this.viewTemplate === 'NewsAlert' || this.viewTemplate === 'NewsMosaic' || this.viewTemplate === 'NewsStories' ? false : this.$root.showArticleSummary;
      this.showArticleAuthor = this.viewTemplate === 'NewsLatest' || this.viewTemplate === 'NewsAlert' ? false : this.$root.showArticleAuthor;
      this.showArticleSpace = this.viewTemplate === 'NewsStories' ? false : this.$root.showArticleSpace;
      this.showArticleDate = this.$root.showArticleDate;
      this.showArticleReactions = this.viewTemplate === 'NewsAlert' ? false : this.$root.showArticleReactions;
      this.seeAllUrl = this.$root.seeAllUrl || '';
    },
  }
};
</script>