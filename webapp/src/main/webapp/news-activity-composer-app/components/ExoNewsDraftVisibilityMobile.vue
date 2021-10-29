<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    content-class="uiPopup">
    <v-card class="elevation-12">
      <div class="ClearFix">
        <span class="subtitle-1 pl-2 pt-2 font-weight-bold PopupTitle popupTitle text-truncate"> {{ $t('news.composer.visibility.mobile.title') }}</span>
      </div>
      <v-radio-group class="mt-2" v-model="visibility">
        <v-radio
          v-for="item in items"
          :key="item.key"
          :label="item.text"
          class="pa-1"
          :value="item.value" />
      </v-radio-group>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    items: {
      type: Array,
      default: function() {
        return null;
      },
    },
    width: {
      type: String,
      default: function() {
        return '200px!important';
      },
    },
  },
  data: () => ({
    dialog: false,
    visibility: false,
  }),
  watch: {
    dialog() {
      if (this.dialog) {
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    },
    visibility() {
      this.$root.$emit('update-visibility', this.visibility);
      this.dialog = false;
      this.$nextTick(() => this.dialog = false);
    }
  },
  created() {
    this.$root.$on('open-draft-visibility', this.openVisibilityDialog);
  },
  methods: {
    openVisibilityDialog(visibility) {
      this.dialog = true;
      this.visibility = visibility;
      this.$emit('opened');
      this.$nextTick(() => this.dialog = true);
    },
  }
};
</script>