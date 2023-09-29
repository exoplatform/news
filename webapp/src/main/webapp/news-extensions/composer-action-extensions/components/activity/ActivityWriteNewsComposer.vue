<template>
  <v-card
    id="writeNewsComposerButton"
    class="mx-4 px-6 py-3"
    outlined
    flat
    hover
    :title="displayTitle">
    <div
      class="d-flex flex-row align-center"
      @click="switchToNews">
      <v-icon
        color="grey darken-1"
        size="42">
        fa-newspaper
      </v-icon>
      <v-span class="caption font-weight-bold ms-5">
        {{ $t('news.composer.write') }}
      </v-span>
    </div>
  </v-card>
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
    disableComposerButton() {
      return this.activityType && this.activityType.length !== 0;
    },
    displayTitle() {
      return this.disableComposerButton ? this.$t('news.composer.write.disable') : this.$t('news.composer.write.description');
    }
  },
  watch: {
    disableComposerButton(value) {
      if (value) {
        document.getElementById('writeNewsComposerButton').setAttribute('style', 'opacity: 0.5');
      }
      else {
        document.getElementById('writeNewsComposerButton').removeAttribute('style');
      }
    }
  },
  methods: {
    switchToNews() {
      if (!this.disableComposerButton) {
        let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
        if (eXo.env.portal.spaceId) {
          url += `?spaceId=${eXo.env.portal.spaceId}`;
        }
        localStorage.setItem('exo-activity-composer-message', this.message || '');
        localStorage.setItem('exo-activity-composer-attachments', JSON.stringify(this.files || []));
        window.open(url, '_blank');
      }
    },
  },
};
</script>
