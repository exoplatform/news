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
            @click="openAddTargetDrawer">
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
                <div :title="props.item.label" class="align-center text-truncate">
                  {{ $t(props.item.label) }}
                </div>
              </td>
              <td>
                <div :title="props.item.description" class="align-center text-truncate">
                  {{ $t(props.item.description) }}
                </div>
              </td>
              <td>
                <div
                  v-if="props.item.permissions.length > 0"
                  class="align-center text-truncate d-flex flex-column">
                  <div class="d-flex flex-row">
                    <div class="identitySuggester no-border mx-4">
                      <news-publish-targets-management-permissions
                        :permission="props.item.permissions[0]"
                        :close="false" />
                    </div>
                    <span
                      v-if="props.item.permissions.length > 1" 
                      class="permissionsIcon font-weight-bold mt-5">
                      +{{ $t(props.item.permissions.length-1) }}</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="align-center">
                  <v-btn
                    icon
                    text
                    @click="openDrawer(props.item.name, props.item.description, props.item.permissions)">
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
        @ok="deleteNewsTarget(selectedTargetName)" />
      <news-publish-targets-management-drawer
        ref="newsPublishTargetsManagementDrawer"
        @news-target-saved="init" />
    </v-main>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    targetPermissions: [],
    headers: [],
    newsTargets: [],
    permissions: [],
    itemsPerPage: 10,
    initialized: false,
    loading: true,
    selectedTargetName: '',
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
      { text: this.$t('news.publishTargets.managementDrawer.permissions'), align: 'center' },
      { text: this.$t('newsTargets.settings.actions'), align: 'center' },
    ];
    this.init();
  },
  methods: {
    init() {
      if (!this.initialized) {
        this.loading = true;
        this.$newsTargetingService.getAllTargets()
          .then(newsTargets => {
            this.newsTargets = newsTargets.map(newsTarget => ({
              name: newsTarget.name,
              label: newsTarget.properties.label,
              description: newsTarget.properties.description,
              permissions: newsTarget.permissions || [],
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
          const clickMessage = this.$t('news.details.undoDelete');
          const message = this.$t('news.newsTarget.deleteSuccess');
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessage: message ,
            alertLinkText: clickMessage ,
            alertLinkCallback: () => this.undoDeleteNewsTarget(targetName),
          }}));
        });
      setTimeout(() => {
        const deletedNewsTarget = localStorage.getItem('deletedNewsTarget');
        if (deletedNewsTarget != null) {
          this.init();
        }
      }, redirectionTime);
    },
    deleteConfirmDialog(target) {
      this.selectedTargetName = target;
      this.$refs.deleteConfirmDialog.open();
    },
    openDrawer(targetName, targetDescription, targetPermissions) {
      let selectedTarget = null;
      selectedTarget = {
        targetName: targetName && targetName.trim(),
        targetDescription: targetDescription && targetDescription.trim(),
        targetPermissions: targetPermissions,
      };
      this.$root.$emit('selected-target', selectedTarget);
      this.$refs.newsPublishTargetsManagementDrawer.open();
    },
    openAddTargetDrawer() {
      this.$refs.newsPublishTargetsManagementDrawer.open();
    },
    undoDeleteNewsTarget(targetName) {
      return this.$newsTargetingService.undoDeleteTarget(targetName)
        .then(() => {
          const message =  this.$t('news.newsTarget.deleteCanceled');
          document.dispatchEvent(new CustomEvent('alert-message', {detail: {
            alertType: 'success',
            alertMessage: message
          }}));
        });
    },
  }
};
</script>