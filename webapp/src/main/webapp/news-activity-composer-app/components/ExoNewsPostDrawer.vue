<template>
  <div>
    <v-overlay
      z-index="1000"
      :value="drawer"
      @click.native="drawer = false" />
    <exo-drawer
      ref="postNewsDrawer"
      v-model="drawer"
      show-overlay
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
              class="flex-grow-1 my-auto" />
            <div class="d-flex flex-row flex-grow-0">
              <slot name="postDateDateTime"></slot>
              <time-picker
                v-model="postDateTime"
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
  </div>
</template>

<script>
export default {
  data: () => ({
    disabled: true,
    drawer: false,
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
      if ( this.postDate < new Date().getTime()) {
        this.postDate = new Date();
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