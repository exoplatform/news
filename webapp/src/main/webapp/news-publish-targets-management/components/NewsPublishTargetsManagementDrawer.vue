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
  <exo-drawer
    id="newsPublishTargetsManagementDrawer"
    ref="newsPublishTargetsManagementDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right
    @closed="reset">
    <template v-if="saveMode === 'creationMode'" slot="title">
      {{ $t('news.publishTargets.management.addTarget') }}
    </template>
    <template v-else slot="title">
      {{ $t('news.publishTargets.management.editTarget') }}
    </template>
    <template slot="content">
      <v-form
        ref="activityShareFrom"
        class="flex mx-4"
        flat>
        <div class="d-flex flex-column flex-grow-1">
          <div class="d-flex flex-row mt-6 mb-1">
            <label class="text-subtitle-1 font-weight-bold">
              {{ $t('news.publishTargets.managementDrawer.name') }}*
            </label>
          </div>
          <div class="d-flex flex-row">
            <v-text-field
              id="targetName"
              ref="targetName"
              v-model="targetName"
              type="string"
              name="newsHeader"
              :placeholder="$t('news.publishTargets.managementDrawer.placeholder.name')"
              :error-messages="checkAlphanumeric"
              maxlength="100"
              class="targetName input-block-level ignore-vuetify-classes"
              counter
              required
              outlined
              dense />
          </div>
          <div class="d-flex flex-row mt-6">
            <label class="text-subtitle-1 font-weight-bold">
              {{ $t('news.publishTargets.managementDrawer.description') }}
            </label>
          </div>
          <div class="d-flex flex-row">
            <extended-textarea
              id="targetDescription"
              ref="targetDescription"
              v-model="targetDescription"
              :placeholder="$t('news.publishTargets.managementDrawer.placeholder.description')"
              :max-length="targetDescriptionTextLength"
              class="targetDescription pt-0 " />
          </div>
          <div v-if="sameTargetError" class="d-flex flex-row mt-4">
            <v-icon class="warning--text">warning</v-icon>
            <span class="ms-2 grey--text">
              {{ $t('news.publishTargets.managementDrawer.sameNewsTargetWarning') }}
            </span>
          </div>
          <div class="d-flex flex-row mt-6">
            <label class="text-subtitle-1 font-weight-bold">
              {{ $t('news.publishTargets.managementDrawer.permissions') }}*
              <v-tooltip bottom v-if="!isMobile">
                <template #activator="{ on, attrs }">
                  <v-icon
                    color="grey darken-2"
                    dark
                    v-bind="attrs"
                    v-on="on"
                    size="16"
                    class="px-2 iconStyle">
                    fa-info-circle
                  </v-icon>
                </template>
                <span>
                  {{ $t('news.publishTargets.managementDrawer.permissions.tooltip') }}
                </span>
              </v-tooltip>
            </label>
          </div>
          <div class="d-flex flex-row">
            <span class="text-subtitle-6 font-weight-regular">
              {{ $t('news.publishTargets.managementDrawer.permissions.description') }}
            </span>
          </div>
          <div class="d-flex flex-row">
            <span class="text-subtitle-6 grey--text font-weight-regular pt-2">
              {{ $t('news.publishTargets.managementDrawer.permissions.restrict.audience.choice.description') }}
            </span>
          </div>
          <div class="d-flex flex-row">
            <exo-identity-suggester
              ref="targetPermissions"
              :labels="suggesterLabels"
              v-model="targetPermissions"
              name="permissions"
              height="40"
              :ignore-items="ignoredItems"
              :group-member="userGroup"
              :group-type="groupType"
              :all-groups-for-admin="allGroupsForAdmin"
              :search-options="{filterType: 'all'}"
              include-spaces
              include-groups
              required />
          </div>
        </div>
      </v-form>
      <div v-if="permissions.length > 0" class="identitySuggester no-border mx-4">
        <news-publish-targets-management-permissions
          v-for="permission in permissions"
          :key="permission"
          :permission="permission"
          :close="true"
          @remove-permission="removePermission" />
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex justify-end">
        <v-btn
          class="btn ms-2"
          @click="closeDrawer">
          {{ $t('news.publishTargets.managementDrawer.btn.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving || disabled"
          :loading="saving"
          class="btn btn-primary ms-2"
          @click="saveTarget">
          {{ saveButtonLabel }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    targetPermissions: [],
    allGroupsForAdmin: true,
    userGroup: '/platform/users',
    groupType: 'GROUP',
    drawer: false,
    saving: false,
    targetDescriptionTextLength: 1000,
    targetDescription: '',
    targetName: '',
    sameTargetError: false,
    selectedTarget: '',
    originalTargetName: '',
    saveMode: 'creationMode',
    permissions: [],
    permissionsUpdated: false,
  }),
  computed: {
    ignoredItems() {
      return this.permissions.map(permission => `${permission.providerId}:${permission.remoteId}`);
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('news.publishTargets.managementDrawer.placeholder.permissions'),
        noDataLabel: this.$t('news.publishTargets.managementDrawer.permissions.noData')
      };
    },
    checkAlphanumeric() {
      return this.targetName && !this.targetName.trim().match(/^[a-zA-Z\u00C0-\u00FF ]*$/) && this.targetName.length > 0 ? this.$t('news.list.settings.name.errorMessage') : '';
    },
    disabled() {
      return (this.selectedTarget.targetName === this.targetName && this.selectedTarget.targetDescription === this.targetDescription && !this.permissionsUpdated) || this.checkAlphanumeric !== '' || this.targetName.length === 0 || this.permissions.length === 0 || this.sameTargetError || (typeof this.targetDescription !== 'undefined' && this.targetDescription.length > this.targetDescriptionTextLength);
    },
    saveButtonLabel() {
      return this.saveMode === 'edit' ? this.$t('news.publishTargets.managementDrawer.btn.update') : this.$t('news.publishTargets.managementDrawer.btn.confirm');
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
  },
  watch: {
    targetPermissions() {
      if (!this.targetPermissions) {
        this.$nextTick(this.$refs.targetPermissions.$refs.selectAutoComplete.deleteCurrentItem);
        return;
      }
      const found = this.permissions.find(permission => {
        return permission.remoteId === this.targetPermissions.remoteId
            && permission.providerId === this.targetPermissions.providerId;
      });
      if (!found) {
        this.permissions.push(this.mapPermission(this.targetPermissions));
        this.permissionsUpdated = true;
      }
      this.targetPermissions=null;
    },
  
    saving() {
      if (this.saving) {
        this.$refs.newsPublishTargetsManagementDrawer.startLoading();
      } else {
        this.$refs.newsPublishTargetsManagementDrawer.endLoading();
      }
    },
    targetName(newVal, oldVal) {
      this.sameTargetError = newVal && newVal.length > 0 && oldVal.length > 0 && newVal === oldVal;
    },
  },
  created() {
    this.$root.$on('selected-target', (selectedTarget) => {
      this.selectedTarget = selectedTarget;
      this.originalTargetName = selectedTarget.targetName;
      this.targetName = selectedTarget.targetName;
      this.targetDescription = selectedTarget.targetDescription;
      this.permissions = JSON.parse(JSON.stringify(selectedTarget.targetPermissions));
      if ( this.targetName === selectedTarget.targetName && this.targetDescription === selectedTarget.targetDescription) {
        this.sameTargetError = true;
      }
      this.saveMode = 'edit';
    });
    this.$root.$on('open-news-publish-targets-management-drawer', () => { this.open(); });
  },
  methods: {
    removePermission(permission) {
      const index = this.permissions.findIndex(addedPermission => {
        return permission.remoteId === addedPermission.remoteId
        && permission.providerId === addedPermission.providerId;
      });
      if (index >= 0) {
        this.permissions.splice(index, 1);
        this.permissionsUpdated = true;
      }
    },
    mapPermission(permission) {
      const fullName = permission.profile
          && permission.profile.fullName
          && permission.profile.fullName.substring(0, permission.profile.fullName.lastIndexOf(' ('));
      return {
        'id': permission.providerId === 'space' ? `space:${permission.spaceId}` : permission.spaceId,
        'profile': {
          'fullName': fullName,
        },
        'name': permission.displayName || fullName,
        'remoteId': permission.remoteId,
        'providerId': permission.providerId,
        'avatar': permission.profile.avatarUrl

      };
    },
    open() {
      this.$refs.newsPublishTargetsManagementDrawer.open();
    },
    closeDrawer() {
      this.$refs.newsPublishTargetsManagementDrawer.close();
      this.reset();
    },
    saveTarget() {
      if (this.saveMode === 'edit') {
        this.updateNewsTarget();
      } else {
        this.createTarget();
      }
    },
    createTarget() {
      this.saving = true;
      const target = {
        name: '',
        properties: ''
      };
      let permissions = '';
      if (this.permissions.length > 0) {
        this.permissions.forEach(permission => {
          permissions = `${permissions + permission.id},`;
        }); 
      } 
      target.name = this.targetName;
      target.properties = {
        description: this.targetDescription,
        label: this.targetName,
        permissions: permissions,
      };
      this.sameTargetError = false;
      this.$newsTargetingService.createTarget(target)
        .then((createdTarget) => {
          this.$emit('news-target-saved');
          this.$root.$emit('new-news-target-created', createdTarget);
          this.reset();
          this.closeDrawer();
        })
        .catch((resp) => {
          if (resp.message && resp.message === 409) {
            this.sameTargetError = true;
            this.disabled = true;
            this.saving = false;
          }
        })
        .finally(() => this.saving = false);
    },
    updateNewsTarget() {
      this.saving = true;
      const target = {
        name: '',
        type: '',
        properties: ''
      };
      let permissions = '';
      if (this.permissions.length > 0) {
        this.permissions.forEach(permission => {
          permissions = `${permissions + permission.id},`;
        }); 
      } 
      target.name = this.targetName;
      target.properties = {
        description: this.targetDescription,
        label: this.targetName,
        permissions: permissions,
      };
      this.$newsTargetingService.updateTarget(target, this.originalTargetName)
        .then((resp) => {
          if (resp && resp === 200) {
            this.$emit('news-target-saved');
            this.reset();
            this.closeDrawer();
          }
        })
        .finally(() => this.saving = false);
    },
    reset() {
      this.targetDescription = '';
      this.targetName = '';
      this.saveMode = 'creationMode';
      this.permissions=[];
      this.permissionsUpdated= false;
    },
  },
};
</script>