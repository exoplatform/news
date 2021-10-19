<template>
  <div v-if="displayLockMessage && !isMobile" class="lockReaction d-flex flex-row mt-lg-1 ms-4 disabled--text">
    <v-icon
      small
      class="baseline-vertical-align mx-auto disabled--text">
      mdi-lock
    </v-icon>
    <span class="ms-2 mx-auto">{{ $t('news.details.cannot.react') }}</span>
  </div>
</template>
<script>
export default {
  data: () => ({
    displayLockMessage: null,
  }),
  computed: {
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
    },
  },
  created() {
    const activityId = this.getActivityId('id');
    if (activityId) {
      this.$newsServices.getNewsByActivityId(activityId)
        .then(news => this.displayLockMessage = news.hiddenActivity);
    }
  },
  methods: {
    getActivityId(paramName) {
      const urlParams = new URLSearchParams(window.location.search);
      if (urlParams.has(paramName)) {
        return urlParams.get(paramName);
      }
    },
  },
};
</script>