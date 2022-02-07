<template>
  <v-list
    class="newsAdvancedSettings"
    subheader
    two-line>
    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
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
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.orderCriteria') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <select
          v-model="selectedType"
          @change="selectedOption('selectedType', selectedType)"
          class="width-auto my-auto subtitle-1 ignore-vuetify-classes">
          <option value="lastPublished">{{ $t('news.list.settings.drawer.advancedSettings.lastPublished') }}</option>
          <option value="lastModified">{{ $t('news.list.settings.drawer.advancedSettings.lastModified') }}</option>
        </select>
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showListHeader') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('news.list.settings.drawer.advancedSettings.displayTextHeader') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showHeader"
          inset
          dense
          @change="selectedOption('showHeader', showHeader)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showSeeAll') }}
        </v-list-item-title>
        <v-list-item-subtitle>
          {{ $t('news.list.settings.drawer.advancedSettings.displaySeeAll') }}
        </v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showSeeAll"
          inset
          dense
          @change="selectedOption('showSeeAll', showSeeAll)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.seeAllButton') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <input
          v-model="seeAllUrl"
          type="url"
          id="seeLink"
          name="seeLink"
          required
          @change="$emit('see-all-url', seeAllUrl)"
          class="seeLink input-block-level ignore-vuetify-classes">
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleTitle') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleTitle"
          inset
          dense
          @change="selectedOption('showArticleTitle', showArticleTitle)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleSummary') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showSummary"
          inset
          dense
          @change="selectedOption('showSummary', showSummary)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleImage') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleImage"
          inset
          dense
          @change="selectedOption('showArticleImage', showArticleImage)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleAuthor') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleAuthor"
          inset
          dense
          @change="selectedOption('showArticleAuthor', showArticleAuthor)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleDate') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleDate"
          inset
          dense
          @change="selectedOption('showArticleDate', showArticleDate)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleSpace') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleSpace"
          inset
          dense
          @change="selectedOption('showArticleSpace',showArticleSpace)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>

    <v-list-item>
      <v-list-item-content>
        <v-list-item-title>
          {{ $t('news.list.settings.drawer.advancedSettings.showArticleReactionsCounter') }}
        </v-list-item-title>
      </v-list-item-content>
      <v-list-item-action>
        <v-switch
          v-model="showArticleReactions"
          inset
          dense
          @change="selectedOption('showArticleReactions', showArticleReactions)"
          class="my-auto" />
      </v-list-item-action>
    </v-list-item>
  </v-list>
</template>
<script>
export default {
  data: () => ({
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
    limit: null
  }),
  created() {
    this.reset();
  },
  methods: {
    selectedOption(selectedOption, optionValue) {
      this.$emit('selected-option', selectedOption, optionValue);
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
      this.showArticleDate = this.$root.showArticleDate;
      this.showArticleReactions = this.$root.showArticleReactions;
      this.seeAllUrl = this.$root.seeAllUrl || `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news?filter=pinned`;
      this.selectedType = this.$root.selectedType || 'lastPublished';
    },
  }
};
</script>