<template>
  <div class="targetsSelector">
    <div
      class="d-flex flex-row selectTarget ms-2"
      @click.stop>
      <v-select
        id="chooseTargets"
        ref="chooseTargets"
        v-model="selectedTargets"
        :items="targets"
        :menu-props="{ bottom: true, offsetY: true}"
        :placeholder="$t('news.composer.stepper.chooseTarget.option')"
        item-text="name"
        item-value="id"
        chips
        hide-no-data
        multiple
        dense
        outlined
        @change="addTarget(selectedTargets)">
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
            <span>{{ item.name }}</span>
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
    targets: [
      { id: 0, name: 'Latest news'},
      { id: 1, name: 'Snapshot Slider'},
      { id: 2, name: 'Homepage widget'}
    ],
  }),
  computed: {
    disableTargetOption() {
      return this.allowPublishTargeting && this.selectedTargets && this.selectedTargets.length === 0 && this.publish;
    },
    showTargetInformation() {
      return this.disableTargetOption && this.allowPublishTargeting && this.publish;
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
    }
  },
  created() {
    $(document).click(() => {
      if (this.$refs.chooseTargets && this.$refs.chooseTargets.isMenuActive) {
        this.$refs.chooseTargets.blur();
      }
    });
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
          this.selectedTargets = this.targets.slice();
        }
        this.$emit('selected-targets', this.selectedTargets);
      });
    },
    addTarget(items) {
      const selectedTargets = [];
      for (const item in items) {
        selectedTargets.push(this.targets[item]);
      }
      this.$emit('selected-targets', selectedTargets);
    }
  }
};
</script>