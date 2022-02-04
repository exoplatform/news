<template>
  <v-progress-circular
    v-if="loading"
    size="24"
    color="primary"
    indeterminate />
  <div v-else-if="content"><a :href="contentUrl">{{ contentTitle }}</a></div>
  <div v-else class="d-flex">
    <i :title="$t('analytics.errorRetrievingDataForValue', {0: value})" class="uiIconColorError my-auto"></i>
    <span class="text-no-wrap text-sub-title my-auto ml-1">
      {{ $t('analytics.deletedContent') }}
    </span>
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Object,
      default: function () {
        return null;
      },
    }
  },
  data: () => ({
    loading: true,
    content: null,
  }),
  computed: {
    contentTitle() {
      return this.content && this.content.title;
    },
    contentUrl() {
      return this.content && this.content.url;
    },
  },
  created() {
    if (this.value) {
      this.loading = true;
      this.getContent(this.value).then(content => {
        this.content = content;
        this.$forceUpdate();
      })
        .finally(() => this.loading = false);
    }
  },
  methods: {
    getContent(contentId) {
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${contentId}`, {
        credentials: 'include',
        method: 'GET',
      }).then((resp) => {
        if (resp && resp.ok) {
          return resp.json();
        }
        return null;
      });
    },
  },
};
</script>