<template>
  <v-app>
    <exo-drawer
      ref="postNewsDrawer"
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
          <div class="grey--text my-4">{{ $t('news.composer.choosePostDate') }}</div>
          <div class="d-flex flex-row flex-grow-1">
            <date-picker
              v-model="postDate"
              :min-value="minimumPostDate"
              class="flex-grow-1 my-auto" />
            <div class="d-flex flex-row flex-grow-0">
              <slot name="postDateDateTime"></slot>
              <time-picker
                v-model="postDateTime"
                :min="minimumDateTime"
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
    postDate: null,
    postDateTime: '8:00',
    allowPostingLater: false,
  }),
  watch: {
    postDate(newVal, oldVal) {
      if (!this.postDate || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      const newDate = new Date(this.postDate);
      newDate.setHours(this.postDateTime.getHours());
      newDate.setMinutes(this.postDateTime.getMinutes());
      newDate.setSeconds(0);
      this.postDate = newDate;
    },
    postDateTime(newVal, oldVal) {
      if (!this.postDateTime || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      const newDate = new Date(this.postDate);
      newDate.setHours(this.postDateTime.getHours());
      newDate.setMinutes(this.postDateTime.getMinutes());
      newDate.setSeconds(0);
      this.postDate = newDate;
    },
  },
  computed: {
    saveButtonLabel() {
      return this.postArticleMode==='later' ? this.$t('news.composer.schedule'): this.$t('news.composer.post');
    },
    minimumPostDate() {
      return new Date();
    },
    minimumDateTime() {
      if ( new Date(this.postDate).getDate() === new Date().getDate()) {
        const currentTime = new Date();
        currentTime.setHours(currentTime.getHours());
        currentTime.setSeconds(0);
        currentTime.setMinutes ( currentTime.getMinutes() + 30 );
        return currentTime;
      }
      return null;
    }
  },
  created() {
    this.initializeDate();
    this.$featureService.isFeatureEnabled('news.postLater')
      .then(enabled => this.allowPostingLater = enabled);
  },
  methods: {
    open() {
      if (this.$refs.postNewsDrawer) {
        this.disabled = false;
        this.$refs.postNewsDrawer.open();
      }
    },
    initializeDate() {
      const nextDate = new Date();
      nextDate.setDate(nextDate.getDate()+1);
      this.postDate = nextDate;
      this.postDateTime = nextDate;
      this.postDateTime.setHours(8);
      this.postDateTime.setMinutes(0);
      this.postDateTime.setMilliseconds(0);
    },
    selectPostMode() {
      this.$emit('post-article', this.postArticleMode !=='later' ? null : this.$newsUtils.convertDate(this.postDate));
    },
  }
};
</script>