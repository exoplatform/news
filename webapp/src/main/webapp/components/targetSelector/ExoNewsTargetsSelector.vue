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
      class="d-flex flex-row selectTarget ms-2 mr-3"
      @click.stop>
      <v-select
        id="chooseTargets"
        ref="chooseTargets"
        v-model="selectedTargets"
        :items="targets"
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
        <template #prepend-item>
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
        <template #selection="{ item, index }">
          <v-tooltip 
            :disabled="!item.description"
            bottom>
            <template #activator="{ on, attrs }">
              <v-chip
                v-bind="attrs"
                v-on="on"
                v-if="index === 0"
                close
                @click:close="removeTarget(item)">
                <span>{{ item.label }}</span>
              </v-chip>
            </template>
            <span>{{ item.description }}</span>
          </v-tooltip>
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
    <div class="ms-2">
      <span class="text-subtitle-2 font-weight-bold"> {{ $t('news.composer.stepper.audienceSection.title') }} </span>
      <p>{{ $t('news.composer.stepper.audienceSection.description') }}</p>
      <div 
        @click.stop
        class="mr-3">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-sheet
                height="40"
                :class="[disableAudienceChoiceSelect ? 'select-audience-block grey lighten-3' : 'select-audience-block']" v-on="on">
              <v-select
                v-on="on"
                id="chooseAudience"
                ref="chooseAudience"
                class="text-subtitle-2 py-0"
                v-model="selectedAudience"
                :items="audiences"
                dense
                outlined
                :disabled="disableAudienceChoiceSelect"
                @change="addAudience()" />
            </v-sheet>
          </template>
          <span>{{ selectAudienceTooltipText }}</span>
        </v-tooltip>


      </div>
      <div class="d-flex flex-row grey--text ms-2 mt-2">
        <i class="fas fa-exclamation-triangle mx-2 pt-1"></i>
        {{ selectedAudienceDescription }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    publish: {
      type: Boolean,
      default: false
    },
    news: {
      type: Object,
      required: false,
      default: null
    },
    targets: {
      type: Object,
      default: null
    },
    audience: {
      type: String,
      default: null
    },
  },
  data: () =>({
    selectedAudience: null,
    selectedTargets: [],
    disableAudienceChoice: false,
    isDataInitialized: false
  }),
  computed: {
    selectedAudienceDescription() {
      return this.selectedAudience === this.$t('news.composer.stepper.audienceSection.allUsers') ? this.$t('news.composer.stepper.audienceSection.allUsers.description') : this.$t('news.composer.stepper.audienceSection.onlySpaceMembers.description');
    },
    audiences() {
      return [this.$t('news.composer.stepper.audienceSection.allUsers'), this.$t('news.composer.stepper.audienceSection.onlySpaceMembers')];
    }, 
    disableTargetOption() {
      return this.selectedTargets && this.selectedTargets.length === 0 && this.publish;
    },
    showTargetInformation() {
      return this.disableTargetOption && this.publish;
    },
    selectAllTargets() {
      return this.selectedTargets.length === this.targets.length;
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
    },
    disableAudienceChoiceSelect() {
      return this.disableAudienceChoice;
    },
    selectAudienceTooltipText() {
      return this.disableAudienceChoiceSelect ? this.$t('news.composer.stepper.audienceSection.Restricted.audience.tooltip') : this.selectedAudienceDescription;
    }
  },
  created() {
    if (!this.audience && this.news?.audience) {
      this.audience = this.news.audience;
    }
    this.selectedAudience = !this.audience || this.audience === 'all' ? this.$t('news.composer.stepper.audienceSection.allUsers') : this.$t('news.composer.stepper.audienceSection.onlySpaceMembers');
    $(document).click(() => {
      if (this.$refs.chooseTargets && this.$refs.chooseTargets.isMenuActive) {
        this.$refs.chooseTargets.blur();
      }
      if (this.$refs.chooseAudience && this.$refs.chooseAudience.isMenuActive) {
        this.$refs.chooseAudience.blur();
      }
    });
    this.selectedTargets = this.news.targets;
    if (this.selectedTargets.length > 0) {
      this.selectAudience(this.selectedTargets);
    }
    this.$nextTick(() => this.isDataInitialized = true);
  },
  watch: {
    selectedTargets() {
      if (this.isDataInitialized) {
        const selectedTargetForCurrentUser = this.selectedTargets.filter(item => this.targets.some(e => {
          return e.name === item;
        }));
        if (!selectedTargetForCurrentUser.length > 0) {
          this.disableAudienceChoice = false;
          this.selectedAudience = this.audiences[0];
          this.addAudience();
        } else {
          this.selectAudience(selectedTargetForCurrentUser);
        }
      }
    },
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
          for (const item in this.targets) {
            selectedTargets.push(this.targets[item].name);
          }
          this.selectedTargets = selectedTargets;
        }
        this.$emit('selected-targets', this.selectedTargets);
      });
    },
    addTarget() {
      this.$emit('selected-targets', this.selectedTargets);
    },
    addAudience(){
      this.$emit('selected-audience', this.selectedAudience);
    },
    selectAudience(selectedTargetForCurrentUser) {
      const targets = this.targets.filter(item => selectedTargetForCurrentUser.includes(item.name));
      const restrictedAudience = targets.some(target => target.restrictedAudience);
      this.selectedAudience = restrictedAudience ? this.audiences[1] : this.audiences[0];
      if (restrictedAudience) {
        this.disableAudienceChoice = true;
      } else {
        this.disableAudienceChoice = false;
      }
      this.addAudience();
    }
  }
};
</script>