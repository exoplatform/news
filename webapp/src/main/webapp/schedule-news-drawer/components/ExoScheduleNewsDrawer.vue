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
          v-if="editScheduledNews !== 'editScheduledNews'"
          v-model="stepper"
          vertical
          flat
          class="ma-0 py-0 me-4">
          <v-stepper-step
            :complete="stepper > 1"
            step="1"
            class="ma-0">
            {{ $t('news.composer.stepper.postStream') }}
          </v-stepper-step>
          <v-stepper-content step="1" class="ps-4 pe-6 my-0">
            <div class="d-flex flex-column pt-2">
              <div class="d-flex flex-row grey--text ms-2 chooseSpaceStream">{{ $t('news.composer.stepper.chooseStream') }}</div>
              <div class="d-flex flex-row">
                <v-switch
                  v-model="hiddenActivity"
                  inset
                  dense
                  class="my-0 ms-3" />
                <label class="my-auto">
                  {{ $t('news.composer.stepper.postActivityStream') }}
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
            {{ $t('news.composer.stepper.publishSection') }}
          </v-stepper-step>
          <v-stepper-content step="2" class="ps-4 pe-6 my-0">
            <div class="d-flex flex-column pt-2">
              <div class="d-flex flex-row grey--text ms-2">{{ $t('news.composer.stepper.publishDedicatedSection') }}</div>
              <div class="d-flex flex-row">
                <v-switch
                  v-model="publish"
                  inset
                  dense
                  class="my-0 ms-3" />
                <label class="my-auto">
                  {{ $t('news.details.header.menu.publish') }}
                </label>
              </div>
              <div v-if="allowTargetingPublish" class="d-flex flex-row grey--text ms-2">{{ $t('news.composer.stepper.selectedTarget') }}</div>
              <v-select
                v-if="allowTargetingPublish"
                id="selectTarget"
                ref="selectTarget"
                :items="items"
                :placeholder="$t('news.composer.stepper.chooseTarget')"
                item-text="name"
                item-value="id"
                chips
                hide-no-data
                multiple
                dense
                outlined
                class="d-flex flex-row selectTarget ms-2" />
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
            {{ $t('news.composer.schedule') }}
          </v-stepper-step>
          <v-stepper-content step="3" class="ps-4 pe-6 my-0">
            <div class="scheduleNews d-flex flex-column">
              <v-radio-group v-model="postArticleMode" class="d-flex flex-row ms-2">
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
              <v-card-actions class="d-flex flex-row mt-4 ms-2 px-0">
                <v-btn
                  :disabled="disabled"
                  class="btn"
                  @click="previousStep">
                  <v-icon size="18" class="me-2">
                    {{ $vuetify.rtl && 'fa-caret-right' || 'fa-caret-left' }}
                  </v-icon>
                  {{ $t('news.composer.stepper.back') }}
                </v-btn>
              </v-card-actions>
            </div>
          </v-stepper-content>
        </v-stepper>
        <div v-else class="d-flex flex-column flex-grow-1 schedulingMode ma-auto pa-auto">
          <v-radio-group v-model="schedulingMode">
            <v-radio
              class="d-flex flex-row scheduleRadio"
              value="editScheduling">
              <span slot="label" class="postModeText">Edit Scheduling</span>
            </v-radio>
            <div v-if="schedulingMode === 'editScheduling'" class="d-flex flex-row">
              <v-divider
                class="d-flex flex-column flex-grow-1 ms-3"
                inset
                vertical />
              <div class="d-flex flex-column flex-grow-1">
                <div class="d-flex flex-row ms-5 my-2 subtitle-2 font-weight-bold">Post on the stream</div>
                <div class="d-flex flex-row ms-3 mb-2">
                  <v-switch
                    v-model="hiddenActivity"
                    inset
                    dense
                    class="my-0 ms-3" />
                  <label class="my-auto">
                    {{ $t('news.composer.stepper.postActivityStream') }}
                  </label>
                </div>
                <div class="d-flex flex-row ms-5 my-2 subtitle-2 font-weight-bold">Publish in a dedicated section</div>
                <div class="d-flex flex-row ms-3 mb-2">
                  <v-switch
                    v-model="publish"
                    inset
                    dense
                    class="my-0 ms-3" />
                  <label class="my-auto">
                    {{ $t('news.details.header.menu.publish') }}
                  </label>
                </div>
                <div v-if="allowTargetingPublish" class="d-flex flex-row ms-2">
                  <v-select
                    id="selectTarget"
                    ref="selectTarget"
                    :items="items"
                    :placeholder="$t('news.composer.stepper.chooseTarget')"
                    item-text="name"
                    item-value="id"
                    chips
                    hide-no-data
                    multiple
                    dense
                    outlined
                    class="selectTarget" />
                </div>
                <v-radio-group v-model="postArticleMode" class="d-flex flex-row ms-2">
                  <v-radio
                    value="immediate">
                    <span slot="label" class="postModeText">{{ $t('news.composer.postImmediately') }}</span>
                  </v-radio>
                  <v-radio
                    value="later"
                    class="mt-4">
                    <span slot="label" class="postModeText">{{ $t('news.composer.postLater') }}</span>
                  </v-radio>
                  <div v-if="showPostLaterMessage">
                    <div class="d-flex flex-row grey--text mt-4 my-4 scheduleInfoCursor">{{ $t('news.composer.choosePostDate') }}</div>
                    <div class="d-flex flex-row">
                      <slot name="postDate"></slot>
                      <date-picker
                        v-model="postDate"
                        :min-value="minimumPostDate"
                        :attach="false"
                        :top="true"
                        class="scheduleDatePicker d-flex flex-column flex-grow-0 my-auto" />
                      <div class="d-flex flex-column flex-grow-0">
                        <slot name="postDateDateTime"></slot>
                        <time-picker
                          v-model="postDateTime"
                          :min="minimumPostDateTime"
                          class="my-0" />
                      </div>
                    </div>
                  </div>
                </v-radio-group>
              </div>
            </div>
            <v-radio
              value="cancelScheduling"
              class="d-flex flex-row my-2 scheduleRadio">
              <span slot="label" class="postModeText">Cancel Scheduling</span>
            </v-radio>
          </v-radio-group>
        </div>
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
    allowTargetingPublish: false,
    hiddenActivity: true,
    schedulingMode: 'editScheduling',
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
      return (this.postArticleMode === 'immediate' ? false : this.postArticleMode === 'later' && postDate.getTime() === scheduleDate.getTime()) && this.selected === this.publish || this.stepper<3;
    },
  },
  created() {
    this.$featureService.isFeatureEnabled('news.targetingPublish')
      .then(enabled => this.allowTargetingPublish = enabled);
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
      if (this.$refs.selectTarget && this.$refs.selectTarget.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.selectTarget.isMenuActive = false;
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
      if (this.stepper === 3) {
        this.$emit('post-article', this.postArticleMode !== 'later' ? null : this.$newsUtils.convertDate(this.postDate), this.postArticleMode, this.publish, !this.hiddenActivity);
      }
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