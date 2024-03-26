<template>
  <div class="action">
    <v-btn
      v-if="displayButton"
      :href="link"
      color="primary"
      target="_blank"
      class="ps-0"
      link
      text
      @click="switchToNews">
      <v-icon size="12">fas fa-pencil-alt fa-sm</v-icon>
      <span class="ms-1 font-weight-bold text-decoration-underline text-capitalize-first-letter">
        {{ $t('news.composer.switch.article') }}
      </span>
    </v-btn>
  </div>
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
    maxMessageLength: {
      type: Number,
      default: 0,
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
  computed: {
    link() {
      const baseUrl = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/news/editor`;
      if (eXo.env.portal.spaceId) {
        return `${baseUrl}?spaceId=${eXo.env.portal.spaceId}&type=draft`;
      } else {
        return baseUrl;
      }
    },
    displayButton() {
      return this.message && this.message.length > this.maxMessageLength && this.activityType && this.activityType.length === 0;
    },
  },
  methods: {
    switchToNews() {
      localStorage.setItem('exo-activity-composer-message', this.message || '');
    },
  },
};
</script>