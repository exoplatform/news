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
  <v-dialog
    ref="dialog"
    v-model="dialog"
    class="pa-2"
    content-class="uiPopup">
    <v-card class="elevation-12">
      <div class="ClearFix">
        <span class="subtitle-1 pa-3 font-weight-bold PopupTitle popupTitle text-truncate"> {{ $t('news.composer.visibility.mobile.title') }}</span>
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