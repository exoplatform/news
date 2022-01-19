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
    id="newsPublishTargetsManagementDrawer"
    ref="newsPublishTargetsManagementDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      {{ $t('news.publishTargets.management.addTarget') }}
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
          :disabled="true"
          class="btn btn-primary ms-2">
          {{ $t('news.publishTargets.managementDrawer.btn.confirm') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    targetDescriptionTextLength: 1000,
    targetDescription: '',
    targetName: '',
  }),
  computed: {
    checkAlphanumeric() {
      if (this.targetName && !this.targetName.trim().match(/^[\w\-\s]+$/) && this.targetName.length > 0) {
        return this.$t('news.list.settings.name.errorMessage');
      } else {
        return '';
      }
    },
  },
  methods: {
    open() {
      this.$refs.newsPublishTargetsManagementDrawer.open();
    },
    closeDrawer() {
      this.$refs.newsPublishTargetsManagementDrawer.close();
    }
  }
};
</script>