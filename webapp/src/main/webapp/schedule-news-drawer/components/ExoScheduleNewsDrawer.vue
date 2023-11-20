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
          v-if="canPublishNews"
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
                  v-model="isActivityPosted"
                  inset
                  dense
                  class="my-0 ms-3" />
                <label class="publishSectionOption my-auto">
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
                  :disabled="!allowedTargets.length"
                  inset
                  dense
                  class="my-0 ms-3" />
                <label class="publishSectionOption my-auto">
                  {{ $t('news.composer.publishSection.option') }}
                </label>
              </div>
              <div class="d-flex flex-row grey--text ms-2">
                <i v-if="allowedTargets.length === 0" class="fas fa-exclamation-triangle mx-2 mt-3"></i>
                {{ selectedTargetDescription }}
              </div>
              <exo-news-targets-selector
                v-if="publish && allowedTargets.length"
                id="chooseTargets"
                ref="chooseTargets"
                :news="news"
                :targets="allowedTargets"
                :publish="publish"
                :audience="audience"
                @selected-targets="getSelectedTargets"
                @selected-audience="getSelectedAudience" />
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
                  :disabled="disableTargetOption"
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
            <div class="ms-3 grey--text">{{ $t('news.composer.stepper.postedOrPublish.description') }}</div>
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
              <v-card-actions class="d-flex flex-row mt-4 ms-2 px-0">
                <v-btn
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
        <div v-else>
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
                  class="postModeText mt-4">
                  <span slot="label" class="postModeText">{{ $t('news.composer.cancelPost') }}</span>
                </v-radio>
                <div v-if="showDontPostMessage" class="grey--text my-4 ms-4 scheduleInfoCursor">{{ $t('news.composer.chooseNotPost') }}</div>
              </v-radio-group>
            </div>
          </div>
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
            {{ $t('news.composer.btn.cancel') }}
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
    isActivityPosted: true,
    selectedTargets: [],
    allowedTargets: [],
    audience: null,
    selectedAudience: null,
    disabled: true,
  }),
  watch: {
    postDate(newVal, oldVal) {
      if (!this.postDate || !newVal || !oldVal || new Date(newVal).getTime() === new Date(oldVal).getTime()) {
        return;
      }
      const postDate = new Date(this.postDate);
      const scheduleDate = new Date(this.schedulePostDate);
      const postDateString = postDate.getFullYear().toString() + postDate.getMonth().toString()+ postDate.getDate().toString() ;
      const scheduleDateString = scheduleDate.getFullYear().toString() + scheduleDate.getMonth().toString() + scheduleDate.getDate().toString();
      const postTimeString = postDate.getHours().toString() + postDate.getMinutes().toString();
      const scheduleTimeString = scheduleDate.getHours().toString() + scheduleDate.getMinutes().toString();
      this.disabled = ((postDateString === scheduleDateString) && (postTimeString === scheduleTimeString));
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
    publish() {
      if (this.editScheduledNews ==='editScheduledNews') {
        if ((this.publish === this.selected && !this.publish) || (this.publish && this.selectedTargets && this.selectedTargets.length === 0) ) {
          this.disabled = true;
        } else {
          this.disabled = false;
        }
      } else {
        this.disabled = true;
      }
    },
    selectedTargets(newVal, oldVal) {
      if (!this.canPublishNews) {
        return ;
      }
      if (this.editScheduledNews ==='editScheduledNews' && this.publish) {
        if (this.selectedTargets.length > 0 && newVal.length !== oldVal.length) {
          this.disabled = false;
        } else {
          this.disabled = true;
        }
      } else {
        this.disabled = true;
      }
    },
    isActivityPosted() {
      if (this.editScheduledNews ==='editScheduledNews') {
        if (this.visibilityActivity === !this.isActivityPosted) {
          this.disabled = true;
        } else {
          this.disabled = false;
        }
      } else {
        this.disabled = true;
      }
    },
    stepper() {
      if (this.canPublishNews && (this.stepper === 1 || this.stepper === 2) && this.editScheduledNews !=='editScheduledNews') {
        this.disabled = true;
      } else if (this.canPublishNews && (this.stepper === 2) && this.editScheduledNews !=='editScheduledNews'){
        this.disabled = (this.visibilityActivity === !this.isActivityPosted) || this.selected === this.publish;
      } else if (this.canPublishNews && this.stepper === 3 && this.editScheduledNews !=='editScheduledNews') {
        this.disabled = false;
      }
    },
    postArticleMode() {
      const postDate = new Date(this.postDate);
      const scheduleDate = new Date(this.schedulePostDate);
      if (this.postArticleMode === 'later') {
        this.disabled = postDate.getTime() === scheduleDate.getTime();
      } else {
        this.disabled = false;
      }
    }
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
      return this.news && this.news.published;
    },
    visibilityActivity() {
      return this.news && this.news.activityPosted;
    },
    disableTargetOption() {
      return this.publish && this.selectedTargets && this.selectedTargets.length === 0;
    },
    selectedTargetDescription(){
      return this.allowedTargets.length === 0 ? this.$t('news.composer.stepper.selectedTarget.noTargetAllowed') : this.$t('news.composer.stepper.selectedTarget.description');
    }
  },
  created() {
    this.selectedAudience= this.$t('news.composer.stepper.audienceSection.allUsers');
    this.disabled = true;
    this.getAllowedTargets();
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
      if (this.newsId) {
        this.initializeDate();
      }
      if (this.$refs.postNewsDrawer) {
        if (this.editScheduledNews ==='editScheduledNews') {
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
            this.canPublishNews = news.canPublish;
            this.isActivityPosted = !news.activityPosted;
            this.schedulePostDate = news.schedulePostDate;
            this.selectedTargets = news.targets;
            this.audience = news.audience ? news.audience : 'all';
          }
        });
    },
    postArticle() {
      this.$emit('post-article', this.postArticleMode !== 'later' ? null : this.$newsUtils.convertDate(this.postDate), this.postArticleMode, this.publish, !this.isActivityPosted, this.selectedTargets, this.publish ? this.selectedAudience : null);
    },
    closeDrawer() {
      if (this.news) {
        this.publish = this.news.published;
      }
      this.stepper = 0;
      this.disabled = false;
      this.$refs.postNewsDrawer.close();
    },
    previousStep() {
      this.stepper--;
    },
    nextStep() {
      this.stepper++;
    },
    getSelectedTargets(selectedTargets) {
      this.selectedTargets = selectedTargets;
    },
    getSelectedAudience(selectedAudience) {
      this.selectedAudience = selectedAudience;
      if (this.editScheduledNews ==='editScheduledNews') {
        this.disabled = false;
      }
    },
    getAllowedTargets() {
      this.$newsTargetingService.getAllowedTargets()
        .then(targets => {
          this.allowedTargets = targets.map(target => ({
            name: target.name,
            label: target.properties && target.properties.label && target.properties.label.length > 35 ? target.properties.label.substring(0, 35).concat('...'): target.properties.label,
            tooltipInfo: target.properties && target.properties.label,
            description: target.properties && target.properties.description,
            restrictAudience: target.restrictAudience,
            groupAndSpacePublisher: target.groupAndSpacePublisher
          }));
        });
    },
  }
};
</script>