<template>
  <v-alert
    v-model="displayAlert"
    :type="alertType"
    border="left"
    class="white"
    elevation="2"
    dismissible
    colored-border
    outlined>
    <span class="text-color">
      {{ alertMessage }}
    </span>
    <v-btn
      v-if="alert.click"
      class="primary--text"
      text
      @click="alert.click">
      {{ alert.clickMessage }}
    </v-btn>
    <v-btn
      slot="close"
      slot-scope="{toggle}"
      icon
      small
      light
      @click="toggle">
      <v-icon>close</v-icon>
    </v-btn>
  </v-alert>
</template>

<script>
export default {
  props: {
    alert: {
      type: Object,
      default: null
    },
  },
  data: () => ({
    displayAlert: true,
  }),
  computed: {
    alertMessage() {
      return this.alert.message;
    },
    alertType() {
      return this.alert.type;
    },
  },
  watch: {
    displayAlert() {
      if (!this.displayAlert) {
        this.$emit('dismissed');
      }
    },
  },
  created() {
    const time = 5000;
    window.setTimeout(() => this.displayAlert = false, time);
  },
};
</script>