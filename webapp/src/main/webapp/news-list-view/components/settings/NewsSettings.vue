<template>
  <div>
    <v-btn
      v-show="hover"
      icon
      absolute
      right
      @click="openDrawer">
      <v-icon>mdi-cog</v-icon>
    </v-btn>
    <news-settings-drawer ref="settingsDrawer" />
  </div>
</template>
<script>
export default {
  props: {
    hover: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
  }),
  methods: {
    openDrawer() {
      this.$refs.settingsDrawer.open();
    },
  },
};
</script>