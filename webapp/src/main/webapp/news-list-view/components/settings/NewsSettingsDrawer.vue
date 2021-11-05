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
      <form ref="form1" class="pa-4">
        <v-label for="limit">
          {{ $t('news.list.settings.limit') }}
        </v-label>
        <input
          v-model="limit"
          type="number"
          name="limit"
          class="input-block-level ignore-vuetify-classes my-3"
          autofocus="autofocus"
          required>
        <v-label for="viewTemplate">
          {{ $t('news.list.settings.viewTemplate') }}
        </v-label>
        <select
          v-model="viewTemplate"
          name="viewTemplate"
          class="input-block-level ignore-vuetify-classes my-3"
          required>
          <option
            v-for="item in viewTemplates"
            :key="item.name"
            :value="item.name">
            {{ item.label }}
          </option>
        </select>
        <v-label for="newsTarget">
          {{ $t('news.list.settings.newsTarget') }}
        </v-label>
        <select
          v-model="newsTarget"
          name="newsTarget"
          class="input-block-level ignore-vuetify-classes my-3"
          required>
          <option
            v-for="item in newsTargets"
            :key="item.name"
            :value="item.name">
            {{ item.label }}
          </option>
        </select>
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