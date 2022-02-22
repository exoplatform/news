<template>
  <div class="actionItem" @click="switchToNews">
    <div class="actionItemIcon">
      <div class="newsComposerIcon"></div>
    </div>
    <div class="actionItemDescription">
      <div class="actionLabel">{{ $t('news.composer.write') }}</div>
      <div class="actionDescription">
        <p>{{ $t('news.composer.write.description') }}</p>
      </div>
    </div>
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
  },
  methods: {
    switchToNews() {
      let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
      if (eXo.env.portal.spaceId) {
        url += `?spaceId=${eXo.env.portal.spaceId}`;
      }
      localStorage.setItem('exo-activity-composer-message', this.message || '');
      localStorage.setItem('exo-activity-composer-attachments', JSON.stringify(this.files || []));
      window.open(url, '_blank');
    },
  },
};
</script>