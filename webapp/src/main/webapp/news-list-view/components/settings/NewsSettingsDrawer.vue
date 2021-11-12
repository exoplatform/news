<template>
  <exo-drawer
    ref="drawer"
    id="newsSettingsDrawer"
    right
    disable-pull-to-refresh
    @closed="reset">
    <template slot="title">
      {{ $t('news.list.settings.title') }}
    </template>
    <template slot="content">
      <form ref="form1" class="pa-4 mt-4">
        <label for="name" class="listViewLabel text-subtitle-1 mt-6">
          {{ $t('news.list.settings.name') }}
        </label>
        <v-text-field
          v-model="nameNewsList"
          type="string"
          name="nameNewsList"
          :placeholder="$t('news.list.settings.namePlaceholder')"
          class="input-block-level ignore-vuetify-classes"
          autofocus="autofocus"
          required
          outlined
          dense />
        <label for="newsTarget" class="listViewLabel text-subtitle-1 mt-6">
          {{ $t('news.list.settings.newsTarget') }}
        </label>
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
        <label for="viewTemplate" class="listViewLabel text-subtitle-1 mt-6">
          {{ $t('news.list.settings.viewTemplate') }}
        </label>
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
          :disabled="saving"
          :loading="saving"
          class="btn btn-primary"
          @click="save">
          {{ $t('news.button.ok') }}
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
    nameNewsList: '',
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
  },
  watch: {
    saving() {
      if (this.saving) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
    initializing() {
      if (this.initializing) {
        this.$refs.drawer.startLoading();
      } else {
        this.$refs.drawer.endLoading();
      }
    },
  },
  created() {
    $(document).click(() => {
      if (this.$refs.newsTargets && this.$refs.newsTargets.isMenuActive) {
        this.$refs.newsTargets.blur();
      }
      if (this.$refs.viewTemplates && this.$refs.viewTemplates.isMenuActive) {
        this.$refs.viewTemplates.blur();
      }
    });
  },
  methods: {
    open() {
      this.init();
      this.reset();
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    reset() {
      this.viewTemplate = this.$root.viewTemplate;
      this.viewExtensions = this.$root.viewExtensions;
      this.newsTarget = this.$root.newsTarget;
      this.limit = this.$root.limit;
    },
    init() {
      if (!this.initialized) {
        this.initializing = true;
        this.$newsListService.getNewsTarget()
          .then(newsTargets => {
            this.newsTargets = newsTargets.map(name => ({
              name,
              label: this.getLabel(`news.list.settings.newsTarget.${name}`, name),
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
        limit: this.limit,
      })
        .then(() => {
          this.$root.viewTemplate = this.viewTemplate;
          this.$root.newsTarget = this.newsTarget;
          this.$root.limit = this.limit;
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