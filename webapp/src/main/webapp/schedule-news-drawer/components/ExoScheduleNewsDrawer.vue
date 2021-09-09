<template>
  <div>
    <v-overlay
      z-index="1000"
      :value="drawer"
      @click.native="drawer = false" />
    <exo-drawer
      id="postNewsDrawer"
      ref="postNewsDrawer"
      v-model="drawer"
      show-overlay
      body-classes="hide-scroll decrease-z-index-more"
      right
      @closed="disabled = false">
      <template v-if="editScheduledNews !== 'editScheduledNews'" slot="title">
        {{ $t('news.composer.postArticle') }}
      </template>
      <template v-else slot="title">
        {{ $t('news.composer.editArticle') }}
      </template>
      <template slot="content">
        <div v-if="canPublishNews" class="d-flex flex-column mt-4 ms-3">
          <div class="d-flex flex-row">
            <span class="text-subtitle-1 grey--text postModeText">{{ $t('news.composer.modularity.publish') }}</span>
            <v-divider
              inset
              class="my-auto me-4 ms-3" />
          </div>
          <div class="d-flex flex-row">
            <v-checkbox
              v-model="publishToAll"
              value="true">
              <span slot="label" class="postModeText">{{ $t('news.composer.user.publish') }}</span>
            </v-checkbox>
          </div>
          <div class="d-flex flex-row grey--text ms-8">
            {{ $t('news.composer.user.publish.info') }}
          </div>
        </div>
        <div class="d-flex flex-column mt-4 ms-3">
          <div class="d-flex flex-row">
            <span class="text-subtitle-1 grey--text postModeText">{{ $t('news.composer.modularity.post') }}</span>
            <v-divider
              inset
              class="my-auto me-4 ms-3" />
          </div>
          <div class="d-flex flex-row">
            <v-radio-group v-model="postArticleMode">
              <v-radio
                value="immediate"
                @click="changeDisable">
                <span slot="label" class="postModeText">{{ $t('news.composer.postImmediately') }}</span>
              </v-radio>
              <v-radio
                value="later"
                class="mt-4"
                @click="changeDisable">
                <span slot="label" class="postModeText">{{ $t('news.composer.postLater') }}</span>
              </v-radio>
              <div v-if="showPostLaterMessage" class="mt-4">
                <div class="grey--text my-4 scheduleInfoCursor">{{ $t('news.composer.choosePostDate') }}</div>
                <div class="d-flex flex-row flex-grow-1">
                  <slot name="postDate"></slot>
                  <date-picker
                    v-model="postDate"
                    :min-value="minimumPostDate"
                    class="scheduleDatePicker flex-grow-1 my-auto" />
                  <div class="d-flex flex-row flex-grow-0">
                    <slot name="postDateDateTime"></slot>
                    <time-picker
                      v-model="postDateTime"
                      :min="minimumPostDateTime"
                      class="me-2" />
                  </div>
                </div>
              </div>
              <v-radio
                v-if="allowNotPost"
                value="notPost"
                class="postModeText mt-4"
                @click="changeDisable">
                <span slot="label" class="postModeText">{{ $t('news.composer.cancelPost') }}</span>
              </v-radio>
              <div v-if="showDontPostMessage" class="grey--text my-4 ms-4 scheduleInfoCursor">{{ $t('news.composer.chooseNotPost') }}</div>
            </v-radio-group>
          </div>
        </div>
      </template>
      <template slot="footer">
        <div v-if="editScheduledNews !== 'editScheduledNews'" class="d-flex justify-end">
          <v-btn
            :disabled="disabled"
            :loading="postingNews"
            class="btn btn-primary ms-2"
            @click="selectPostMode">
            {{ saveButtonLabel }}
          </v-btn>
        </div>
        <div v-else class="d-flex justify-end">
          <v-btn
            class="btn ms-2"
            @click="closeDrawer">
            {{ $t('news.composer.cancel') }}
          </v-btn>
          <v-btn
            :disabled="disabled"
            class="btn btn-primary ms-2"
            @click="selectPostMode">
            {{ $t('news.composer.confirm') }}
          </v-btn>
        </div>
      </template>
    </exo-drawer>
  </div>
</template>

<script>
export default {
  props: {
    newsId: {
      type: String,
      required: false,
      default: null
    },
    postingNews: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    drawer: false,
    disabled: false,
    postArticleMode: 'later',
    postDateTime: '8:00',
    editScheduledNews: false,
    allowNotPost: false,
    schedulePostDate: null,
    postDate: null,
    canPublishNews: false,
    publishToAll: false,
  }),
  watch: {
    postDate(newVal, oldVal) {
      if (!this.postDate || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      const postDate = new Date(this.postDate);
      const scheduleDate = new Date(this.schedulePostDate);
      this.disabled = postDate.getTime() === scheduleDate.getTime();
      postDate.setHours(this.postDateTime.getHours());
      postDate.setMinutes(this.postDateTime.getMinutes());
      postDate.setSeconds(0);
      this.postDate = postDate;
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
      return this.postArticleMode === 'later' ? this.$t('news.composer.schedule'): this.$t('news.composer.post');
    },
    minimumPostDate() {
      return new Date();
    },
    minimumPostDateTime() {
      const currentDate = new Date();
      return this.postDate && Number(new Date(this.postDate.getTime())) < currentDate.getTime() ? new Date(currentDate.getTime() + 1800000) : null;
    },
    showDontPostMessage() {
      return this.allowNotPost && this.postArticleMode !== 'later' && this.postArticleMode !== 'immediate';
    },
    showPostLaterMessage() {
      return this.postArticleMode === 'later' || !this.allowNotPost && this.postArticleMode !== 'immediate';
    }
  },
  created() {
    this.$newsServices.canPublishNews().then(canPublishNews => {
      this.canPublishNews = canPublishNews;
    });
    if (this.newsId) {
      this.initializeDate();
    }
    this.$root.$on('open-schedule-drawer', (scheduleMode) => {
      this.editScheduledNews = scheduleMode;
      if (scheduleMode === 'editScheduledNews') {
        this.allowNotPost = true;
        this.postArticleMode = 'later';
      } else {
        this.allowNotPost = false;
        this.postArticleMode = 'immediate';
      }
      this.openDrawer();
    });
  },
  methods: {
    openDrawer() {
      if (this.$refs.postNewsDrawer) {
        if (this.editScheduledNews ==='editScheduledNews') {
          this.disabled = true;
          this.postDateTime = new Date(this.schedulePostDate);
          this.postDate = this.postDateTime;
          this.postDateTime.setHours(new Date(this.schedulePostDate).getHours());
          this.postDateTime.setMinutes(new Date(this.schedulePostDate).getMinutes());
          this.postDateTime.setSeconds(new Date(this.schedulePostDate).getSeconds());
          this.postDateTime.setMilliseconds(new Date(this.schedulePostDate).getMilliseconds());
        } else {
          const nextDate = new Date();
          nextDate.setDate(nextDate.getDate() + 1);
          this.postDate = nextDate;
          this.postDateTime = nextDate;
          this.postDateTime.setHours(8);
          this.postDateTime.setMinutes(0);
          this.postDateTime.setSeconds(0);
          this.postDateTime.setMilliseconds(0);
        }
        this.$refs.postNewsDrawer.open();
      }
    },
    initializeDate() {
      this.$newsServices.getNewsById(this.newsId, false)
        .then(news => {
          if (news !== null && news.schedulePostDate) {
            this.schedulePostDate = news.schedulePostDate;
          }
        });
    },
    selectPostMode() {
      this.$emit('post-article', this.postArticleMode !=='later' ? null : this.$newsUtils.convertDate(this.postDate), this.postArticleMode, this.publishToAll);
    },
    changeDisable() {
      const postDate = new Date(this.postDate);
      const scheduleDate = new Date(this.schedulePostDate);
      this.disabled = this.postArticleMode === 'immediate' ? false : this.postArticleMode === 'later' && postDate.getTime() === scheduleDate.getTime();
    },
    closeDrawer() {
      this.$refs.postNewsDrawer.close();
    }
  }
};
</script>