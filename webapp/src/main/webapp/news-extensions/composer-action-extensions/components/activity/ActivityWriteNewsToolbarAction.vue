<template>
  <v-btn
    id="writeNewsBtnToolbar"
    :ripple="false"
    class="d-flex flex-row align-center py-2"
    text
    @click="switchToNews">
    <v-icon
      color="grey darken-1"
      size="27">
      fa-newspaper
    </v-icon>
    <v-span class="body-2 font-weight-bold ms-5 dark-grey-color">
      {{ $t('news.title') }}
    </v-span>
  </v-btn>
</template>
<script>
export default {
  props: {
    activityId: {
      type: String,
      default: null,
    },
    message: {
      type: String,
      default: null,
    },
    templateParams: {
      type: Object,
      default: null,
    },
    files: {
      type: Array,
      default: null,
    },
    activityType: {
      type: Array,
      default: null,
    },
  },
  methods: {
    switchToNews() {
      let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
      if (eXo.env.portal.spaceId) {
        url += `?spaceId=${eXo.env.portal.spaceId}`;
      }
      window.open(url, '_blank');
    },
  },
};
</script>
