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
  <div class="targetsSelector">
    <div
      class="d-flex flex-row selectTarget ms-2"
      @click.stop>
      <v-select
        id="chooseTargets"
        ref="chooseTargets"
        v-model="selectedTargets"
        :items="referencedTargets"
        :menu-props="{ bottom: true, offsetY: true}"
        :placeholder="$t('news.composer.stepper.chooseTarget.option')"
        item-text="label"
        item-value="name"
        chips
        hide-no-data
        multiple
        dense
        outlined
        @change="addTarget()">
        <template v-slot:prepend-item>
          <v-list-item
            ripple
            @click.stop="toggleTargetsSelection">
            <v-list-item-action>
              <v-icon :color="selectedTargets.length > 0 ? 'primary' : ''">
                {{ selectionIcon }}
              </v-icon>
            </v-list-item-action>
            <v-list-item-content>
              <v-list-item-title>
                {{ selectTargetLabel }}
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>
        <template v-slot:selection="{ item, index }">
          <v-chip
            v-if="index === 0"
            close
            @click:close="removeTarget(item)">
            <span>{{ item.label }}</span>
          </v-chip>
          <span
            v-if="index === 1"
            class="grey--text text-caption">
            (+{{ selectedTargets.length - 1 }} {{ $t('news.composer.stepper.chooseTarget.others') }})
          </span>
        </template>
      </v-select>
    </div>
    <span v-if="showTargetInformation" class="d-flex flex-row error--text ms-2">
      {{ $t('news.composer.stepper.chooseTarget.mandatory') }}
    </span>
  </div>
</template>

<script>
export default {
  props: {
    allowPublishTargeting: {
      type: Boolean,
      default: false
    },
    publish: {
      type: Boolean,
      default: false
    },
    news: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: () =>({
    selectedTargets: [],
    referencedTargets: [],
  }),
  computed: {
    disableTargetOption() {
      return this.allowPublishTargeting && this.selectedTargets && this.selectedTargets.length === 0 && this.publish;
    },
    showTargetInformation() {
      return this.disableTargetOption && this.allowPublishTargeting && this.publish;
    },
    selectAllTargets() {
      return this.selectedTargets.length === this.referencedTargets.length;
    },
    selectSomeTarget() {
      return this.selectedTargets.length > 0 && !this.selectAllTargets;
    },
    selectionIcon() {
      if (this.selectAllTargets) {return 'mdi-close-box';}
      if (this.selectSomeTarget) {return 'mdi-minus-box';}
      return 'mdi-checkbox-blank-outline';
    },
    selectTargetLabel() {
      return this.selectAllTargets ? this.$t('news.composer.stepper.chooseTarget.deselectAllTargets') : this.$t('news.composer.stepper.chooseTarget.selectAllTargets');
    }
  },
  created() {
    this.getReferencedTargets();
    $(document).click(() => {
      if (this.$refs.chooseTargets && this.$refs.chooseTargets.isMenuActive) {
        this.$refs.chooseTargets.blur();
      }
    });
    this.selectedTargets = this.news.targets;
  },
  methods: {
    removeTarget(item) {
      this.selectedTargets.splice(this.selectedTargets.indexOf(item), 1);
      this.selectedTargets = [...this.selectedTargets];
      this.$emit('selected-targets', this.selectedTargets);
    },
    toggleTargetsSelection() {
      this.$nextTick(() => {
        if (this.selectAllTargets) {
          this.selectedTargets = [];
        } else {
          const selectedTargets = [];
          for (const item in this.referencedTargets) {
            selectedTargets.push(this.referencedTargets[item].name);
          }
          this.selectedTargets = selectedTargets;
        }
        this.$emit('selected-targets', this.selectedTargets);
      });
    },
    addTarget() {
      this.$emit('selected-targets', this.selectedTargets);
    },
    getReferencedTargets() {
      this.$newsTargetingService.getReferencedTargets()
        .then(referencedTargets => this.referencedTargets = referencedTargets);
    },
  }
};
</script>