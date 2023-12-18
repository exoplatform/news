<template>
  <div v-if="news" class="d-flex flex-row">
    <div class="flex-column my-auto">
      <v-icon x-small class="ms-4 me-1">far fa-clock</v-icon>
    </div>
    <div class="flex-column me-1 my-auto">
      <span>{{ postModeLabel }} </span>
    </div>
    <date-format
      v-if="publicationDate"
      :value="publicationDate"
      :format="dateFormat" />
    <span v-else-if="postedDate" class="flex-column my-auto">- {{ postedDate }}</span>
    <template v-else-if="publicationState === 'staged'">
      <date-format
        :value="scheduleDate"
        :format="dateFormat" />
      <span>-</span>
      <date-format
        :value="scheduleDate"
        :format="dateTimeFormat" />
    </template>
    <date-format
      v-else-if="updatedDate"
      :value="updatedDate"
      :format="dateFormat" />
    <div v-if="notSameUpdater" class="ms-1 flex-column me-1 my-auto"> {{ $t('news.activity.by') }}</div>
    <div v-if="notSameUpdater" class="flex-column my-auto">
      <a :href="updaterProfileURL">{{ updaterFullName }}</a>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    dateTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  computed: {
    publicationDate() {
      return this.news && this.news.publicationDate && this.news.publicationDate.time && new Date(this.news.publicationDate.time);
    },
    updatedDate() {
      return this.news && this.news.updateDate && this.news.updateDate.time && new Date(this.news.updateDate.time);
    },
    postedDate() {
      return this.news && this.news.postedDate;
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    scheduleDate() {
      return this.news && this.news.schedulePostDate;
    },
    notSameUpdater() {
      return this.news && this.news.updater !=='__system' && this.news.updater !== this.news.author;
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.news.updater}`;
    },
    postModeLabel() {
      return this.publicationState === 'staged' && this.updatedDate ? this.$t('news.details.scheduled') :this.$t('news.activity.lastUpdated');
    },
    updaterFullName() {
      return this.news && this.news.updater !=='__system' ? this.news.updaterFullName : this.news.authorFullName;
    },
  }
};
</script>