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
              <time-picker
                v-model="datePublishedTime"
                class="me-4" />
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
            {{ saveButtonLabel }}
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
    datePublishedTime: '8:00',
    showDatePublishing: false,
    allowPostingLater: false,
  }),
  watch: {
    datePublished(newVal, oldVal) {
      if (!this.datePublished || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      if ( this.datePublished < new Date().getTime()) {
        this.datePublished = new Date();
      }
      const newDate = new Date(this.datePublished);
      newDate.setHours(this.datePublishedTime.getHours());
      newDate.setMinutes(this.datePublishedTime.getMinutes());
      newDate.setSeconds(0);
      this.datePublished = newDate;
    },
    datePublishedTime(newVal, oldVal) {
      if (!this.datePublishedTime || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      const newDate = new Date(this.datePublished);
      newDate.setHours(this.datePublishedTime.getHours());
      newDate.setMinutes(this.datePublishedTime.getMinutes());
      newDate.setSeconds(0);
      this.datePublished = newDate;
    },
  },
  computed: {
    saveButtonLabel() {
      return this.postArticleMode==='later' ? this.$t('news.composer.schedule'): this.$t('news.composer.post');
    }
  },
  created() {
    this.initializeDate();
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
    initializeDate() {
      const nextDate = new Date();
      nextDate.setDate(nextDate.getDate()+1);
      this.datePublished = nextDate;
      this.datePublishedTime = nextDate;
      this.datePublishedTime.setHours(8);
      this.datePublishedTime.setMinutes(0);
      this.datePublishedTime.setMilliseconds(0);
    },
    selectPostMode() {
      let datePublish = null;
      if (this.postArticleMode !=='later') {
        datePublish = null;
      } else {
        datePublish = this.$newsUtils.convertDate(this.datePublished);
      }
      this.$emit('post-article', datePublish);
    },
  }
};
</script>