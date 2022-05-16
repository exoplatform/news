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
        </div>
      </v-form>
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
    drawer: false,
    saving: false,
    targetDescriptionTextLength: 1000,
    targetDescription: '',
    targetName: '',
    sameTargetError: false,
    selectedTarget: '',
    originalTargetName: '',
    saveMode: 'creationMode',
  }),
  computed: {
    checkAlphanumeric() {
      return this.targetName && !this.targetName.trim().match(/^[a-zA-Z\u00C0-\u00FF ]*$/) && this.targetName.length > 0 ? this.$t('news.list.settings.name.errorMessage') : '';
    },
    disabled() {
      return (this.selectedTarget.targetName === this.targetName && this.selectedTarget.targetDescription === this.targetDescription) || this.checkAlphanumeric !== '' || this.targetName.length === 0 || this.sameTargetError || this.targetDescription.length > this.targetDescriptionTextLength;
    },
    saveButtonLabel() {
      return this.saveMode === 'edit' ? this.$t('news.publishTargets.managementDrawer.btn.update') : this.$t('news.publishTargets.managementDrawer.btn.confirm');
    }
  },
  watch: {
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
      if ( this.targetName === selectedTarget.targetName && this.targetDescription === selectedTarget.targetDescription) {
        this.sameTargetError = true;
      }
      this.saveMode = 'edit';
    });
  },
  methods: {
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
      target.name = this.targetName;
      target.properties = {
        description: this.targetDescription,
        label: this.targetName
      };
      this.sameTargetError = false;
      this.$newsTargetingService.createTarget(target)
        .then((resp) => {
          if (resp && resp === 200) {
            this.$emit('news-target-saved');
            this.reset();
            this.closeDrawer();
          } else if (resp && resp === 409) {
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
      target.name = this.targetName;
      target.properties = {
        description: this.targetDescription,
        label: this.targetName
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
    },
  },
};
</script>