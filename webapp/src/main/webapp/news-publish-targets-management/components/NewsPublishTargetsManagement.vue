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
            class="btn btn-primary"
            @click="openDrawer">
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
                <div :title="props.item.description" class="align-center text-truncate">
                  {{ $t(props.item.description) }}
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
                  <v-btn
                    icon
                    text
                    @click="deleteConfirmDialog(props.item.name)">
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
      <exo-confirm-dialog
        ref="deleteConfirmDialog"
        :message="$t('news.newsTarget.message.confirmDeleteNews')"
        :title="$t('news.newsTarget.title.confirmDeleteNews')"
        :ok-label="$t('news.button.ok')"
        :cancel-label="$t('news.button.cancel')"
        @ok="deleteNewsTarget(selectedTarget)" />
      <news-publish-targets-management-drawer ref="newsPublishTargetsManagementDrawer" @news-target-saved="init" />
      <exo-news-notification-alerts />
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
    selectedTarget: '',
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
              label: newsTarget.properties.label,
              description: newsTarget.properties.description,
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
    deleteNewsTarget(targetName) {
      const deleteDelay = 6;
      const redirectionTime = 8100;
      this.$newsTargetingService.deleteTargetByName(targetName, deleteDelay)
        .then(() => {
          this.$root.$emit('confirm-newsTarget-deletion', targetName);
        });
      setTimeout(() => {
        const deletedNewsTarget = localStorage.getItem('deletedNewsTarget');
        if (deletedNewsTarget != null) {
          this.init();
        }
      }, redirectionTime);
    },
    deleteConfirmDialog(target) {
      this.selectedTarget = target;
      this.$refs.deleteConfirmDialog.open();
    },
    openDrawer() {
      this.$refs.newsPublishTargetsManagementDrawer.open();
    }
  }
};
</script>