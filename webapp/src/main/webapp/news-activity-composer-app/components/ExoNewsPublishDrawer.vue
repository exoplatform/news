<template>
  <v-app>
    <exo-drawer
      ref="publishNewsDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        {{ $t('news.composer.postArticle') }}
      </template>
      <template slot="content">
        <v-radio-group v-model="postArticleMode" class="ml-2">
          <v-radio
            :label="$t('news.composer.postImmediately')"
            value="immediate" />
          <v-radio
            v-if="allowPostingLater"
            :label="$t('news.composer.postLater')"
            value="later" />
        </v-radio-group>
        <div v-if="postArticleMode==='later' && allowPostingLater" class="ml-4">
          <div class="grey--text my-4">{{ $t('news.composer.chooseDatePublish') }}</div>
          <div class="d-flex flex-row flex-grow-1">
            <date-picker
              v-model="datePublished"
              class="flex-grow-1 my-auto" />
            <div class="d-flex flex-row flex-grow-0">
              <slot name="datePublishedDateTime"></slot>
              <time-picker v-model="datePublishedTime" class="me-4" />
            </div>
          </div>
        </div>
      </template>
      <template slot="footer">
        <div class="d-flex justify-end">
          <v-btn
            :disabled="disabled"
            class="btn btn-primary ms-2"
            @click="selectPostMode">
            {{ $t('news.composer.post') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    disabled: true,
    postArticleMode: 'immediate',
    datePublished: null,
    datePublishedTime: null,
    showDatePublishing: false,
    allowPostingLater: false,
  }),
  created() {
    this.$featureService.isFeatureEnabled('news.postLater')
      .then(enabled => this.allowPostingLater = enabled);
  },
  methods: {
    open() {
      if (this.$refs.publishNewsDrawer) {
        this.disabled = false;
        this.$refs.publishNewsDrawer.open();
      }
    },
    selectPostMode() {
      const newDate = new Date(this.datePublished);
      newDate.setFullYear(newDate.getFullYear());
      newDate.setMonth(newDate.getMonth());
      newDate.setDate(newDate.getDate());
      newDate.setHours(this.datePublishedTime.getHours());
      newDate.setMinutes(this.datePublishedTime.getMinutes());
      newDate.setSeconds(0);
      this.$emit('post-article', newDate);
    },
  }
};
</script>