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
      @opened="stepper = 1"
      @closed="closeDrawer">
      <template v-if="editScheduledNews !== 'editScheduledNews'" slot="title">
        {{ $t('news.composer.postArticle') }}
      </template>
      <template v-else slot="title">
        {{ $t('news.composer.editArticle') }}
      </template>
      <template slot="content">
        <v-stepper
          v-model="stepper"
          vertical
          flat
          class="ma-0 py-0 me-4">
          <v-stepper-step
            :complete="stepper > 1"
            step="1"
            class="ma-0">
            {{ $t('news.composer.stepper.postStream.title') }}
          </v-stepper-step>
          <v-stepper-content step="1" class="ps-4 pe-6 my-0">
            <div class="d-flex flex-column pt-2">
              <div class="d-flex flex-row grey--text ms-2 postOnStreamOption">{{ $t('news.composer.stepper.postOnStream.description') }}</div>
              <div class="d-flex flex-row">
                <v-switch
                  inset
                  dense
                  value
                  input-value="true"
                  disabled
                  class="my-0 ms-3" />
                <label class="my-auto">
                  {{ $t('news.composer.stepper.postOnStream.option') }}
                </label>
              </div>
              <v-card-actions class="d-flex flex-row mt-4 ms-2 px-0">
                <v-spacer />
                <v-btn
                  class="btn btn-primary me-4"
                  outlined
                  @click="nextStep">
                  {{ $t('news.composer.stepper.continue') }}
                  <v-icon size="18" class="ms-2">
                    {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                  </v-icon>
                </v-btn>
              </v-card-actions>
            </div>
          </v-stepper-content>
          <v-stepper-step
            :complete="stepper > 2"
            step="2"
            class="ma-0">
            {{ $t('news.composer.stepper.publishSection.title') }}
          </v-stepper-step>
          <v-stepper-content step="2" class="ps-4 pe-6 my-0">
            <div class="d-flex flex-column pt-2">
              <div class="d-flex flex-row grey--text ms-2">{{ $t('news.composer.stepper.publishSection.description') }}</div>
              <div class="d-flex flex-row">
                <v-switch
                  v-model="publish"
                  inset
                  dense
                  class="my-0 ms-3" />
                <label class="my-auto">
                  {{ $t('news.composer.publishSection.option') }}
                </label>
              </div>
              <div v-if="allowPublishTargeting" class="d-flex flex-row grey--text ms-2">{{ $t('news.composer.stepper.selectedTarget.description') }}</div>
              <div v-if="allowPublishTargeting" class="d-flex flex-row selectTarget ms-2">
                <v-select
                  id="chooseTargets"
                  ref="chooseTargets"
                  :items="items"
                  :placeholder="$t('news.composer.stepper.chooseTarget.option')"
                  item-text="name"
                  item-value="id"
                  chips
                  hide-no-data
                  multiple
                  dense
                  outlined />
              </div>
              <v-card-actions class="d-flex flex-row mt-4 ms-2 px-0">
                <v-btn class="btn" @click="previousStep">
                  <v-icon size="18" class="me-2">
                    {{ $vuetify.rtl && 'fa-caret-right' || 'fa-caret-left' }}
                  </v-icon>
                  {{ $t('news.composer.stepper.back') }}
                </v-btn>
                <v-spacer />
                <v-btn
                  class="btn btn-primary me-4"
                  outlined
                  @click="nextStep">
                  {{ $t('news.composer.stepper.continue') }}
                  <v-icon size="18" class="ms-2">
                    {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                  </v-icon>
                </v-btn>
              </v-card-actions>
            </div>
          </v-stepper-content>
          <v-stepper-step
            :complete="stepper > 3"
            step="3"
            class="ma-0">
            {{ $t('news.composer.stepper.schedule.title') }}
          </v-stepper-step>
          <v-stepper-content step="3" class="ps-4 pe-6 my-0">
            <div class="scheduleNews">
              <v-radio-group v-model="postArticleMode" class="ms-2">
                <v-radio
                  value="immediate">
                  <span slot="label" class="postModeText">{{ $t('news.composer.postImmediately') }}</span>
                </v-radio>
                <v-radio
                  value="later"
                  class="mt-4">
                  <span slot="label" class="postModeText">{{ $t('news.composer.postLater') }}</span>
                </v-radio>
                <div v-if="showPostLaterMessage" class="mt-4">
                  <div class="grey--text my-4 scheduleInfoCursor">{{ $t('news.composer.choosePostDate') }}</div>
                  <div class="d-flex flex-column flex-grow-1">
                    <slot name="postDate"></slot>
                    <div class="d-flex flex-row flex-grow-1">
                      <date-picker
                        v-model="postDate"
                        :min-value="minimumPostDate"
                        :attach="false"
                        :top="true"
                        class="scheduleDatePicker flex-grow-0 my-auto" />
                      <div class="flex-grow-0">
                        <slot name="postDateDateTime"></slot>
                        <time-picker
                          v-model="postDateTime"
                          :min="minimumPostDateTime"
                          class="my-0" />
                      </div>
                    </div>
                  </div>
                </div>
                <v-radio
                  v-if="allowNotPost"
                  value="notPost"
                  class="postModeText mt-4">
                  <span slot="label" class="postModeText">{{ $t('news.composer.cancelPost') }}</span>
                </v-radio>
                <div v-if="showDontPostMessage" class="grey--text my-4 ms-4 scheduleInfoCursor">{{ $t('news.composer.chooseNotPost') }}</div>
              </v-radio-group>
              <v-btn
                :disabled="disabled"
                class="btn mt-4 px-0 mb-4"
                @click="previousStep">
                <v-icon size="18" class="me-2">
                  {{ $vuetify.rtl && 'fa-caret-right' || 'fa-caret-left' }}
                </v-icon>
                {{ $t('news.composer.stepper.back') }}
              </v-btn>
            </div>
          </v-stepper-content>
        </v-stepper>
      </template>
      <template slot="footer">
        <div v-if="editScheduledNews !== 'editScheduledNews'" class="d-flex justify-end">
          <v-btn
            :disabled="disabled"
            :loading="postingNews"
            class="btn btn-primary ms-2"
            @click="postArticle">
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
            @click="postArticle">
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
    stepper: 0,
    drawer: false,
    postArticleMode: 'later',
    postDateTime: '8:00',
    editScheduledNews: false,
    allowNotPost: false,
    schedulePostDate: null,
    postDate: null,
    canPublishNews: false,
    publish: false,
    news: null,
    items: [
      { id: 0, name: 'Latest news'},
      { id: 1, name: 'Snapshot Slider'},
      { id: 2, name: 'Homepage widget'}
    ],
    allowPublishTargeting: false,
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
    selected() {
      this.publish = this.selected;
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
    },
    selected() {
      return this.news && this.news.pinned;
    },
    disabled() {
      const postDate = new Date(this.postDate);
      const scheduleDate = new Date(this.schedulePostDate);
      return (this.postArticleMode === 'immediate' ? false : this.postArticleMode === 'later' && postDate.getTime() === scheduleDate.getTime()) && this.selected === this.publish;
    },
  },
  created() {
    this.$featureService.isFeatureEnabled('news.publishTargeting')
      .then(enabled => this.allowPublishTargeting = enabled);
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
    $(document).mousedown(() => {
      if (this.$refs.chooseTargets && this.$refs.chooseTargets.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.chooseTargets.isMenuActive = false;
        }, 200);
      }
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
          if (news) {
            this.news = news;
            this.schedulePostDate = news.schedulePostDate;
          }
        });
    },
    postArticle() {
      this.$emit('post-article', this.postArticleMode !=='later' ? null : this.$newsUtils.convertDate(this.postDate), this.postArticleMode, this.publish);
    },
    closeDrawer() {
      this.stepper = 0;
      this.publish = this.news.pinned;
      this.disabled = false;
      this.$refs.postNewsDrawer.close();
    },
    previousStep() {
      this.stepper--;
    },
    nextStep() {
      this.stepper++;
    },
  }
};
</script>