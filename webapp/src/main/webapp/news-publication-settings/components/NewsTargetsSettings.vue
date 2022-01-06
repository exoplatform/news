<template>
  <v-app class="newsTargetsAdminSettings">
    <v-main class="white rounded-lg ma-5 px-7 pb-2">
      <div class="white">
        <div class="d-flex flex-row">
          <h4 class="py-5 font-weight-bold">
            {{ $t('newsTargets.settings.title') }}
          </h4>
        </div>
        <div class="d-flex flex-row pb-5">
          <v-btn
            class="btn btn-primary">
            <v-icon dark>
              mdi-plus
            </v-icon>
            <span class="ms-2 d-none d-lg-inline">
              {{ $t('newsTargets.settings.button.addTarget') }}
            </span>
          </v-btn>
        </div>
        <v-divider class="mt-5" />
        <v-data-table
          :loading="loading"
          :headers="headers"
          :items="newsTargets"
          :items-per-page="itemsPerPage"
          :hide-default-footer="hideFooter"
          :footer-props="{
            itemsPerPageText: `${$t('newsTargets.settings.itemsPerPage')}:`,
          }"
          :loading-text="$t('newsTargets.settings.loadingResults')"
          :no-results-text="$t('newsTargets.settings.noResultsFound')"
          :no-data-text="$t('newsTargets.settings.noTargets')"
          disable-sort>
          <template slot="item" slot-scope="props">
            <tr>
              <td>
                <div class="align-center">
                  {{ $t(props.item.label) }}
                </div>
              </td>
              <td>
                <div class="align-center">
                  {{ $t(props.item.name) }}
                </div>
              </td>
              <td>
                <div class="align-center">
                  <v-btn icon text>
                    <v-icon
                      dark
                      color="primary"
                      size="16">
                      fas fa-pen
                    </v-icon>
                  </v-btn>
                  <v-btn icon text>
                    <v-icon
                      dark
                      color="primary"
                      size="16">
                      fas fa-trash
                    </v-icon>
                  </v-btn>
                </div>
              </td>
            </tr>
          </template>
        </v-data-table>
      </div>
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    headers: [],
    newsTargets: [],
    itemsPerPage: 10,
    initialized: false,
    loading: true,
  }),
  computed: {
    hideFooter() {
      return this.newsTargets && this.newsTargets.length <= this.itemsPerPage;
    },
  },
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  },
  created() {
    this.headers = [
      { text: this.$t('newsTargets.settings.name'), align: 'center' },
      { text: this.$t('newsTargets.settings.description'), align: 'center' },
      { text: this.$t('newsTargets.settings.actions'), align: 'center' },
    ];
    this.init();
  },
  methods: {
    init() {
      if (!this.initialized) {
        this.initializing = true;
        this.loading = true;
        this.$newsTargetingService.getAllTargets()
          .then(newsTargets => {
            this.newsTargets = newsTargets.map(newsTarget => ({
              name: newsTarget.name,
              label: newsTarget.label,
            }));
            this.initialized = true;
          })
          .finally(() => {
            if (!this.initialized) {
              this.$root.$applicationLoaded();
            }
            this.loading = false;
            this.initialized = false;
          });
      }
    },
  }
};
</script>