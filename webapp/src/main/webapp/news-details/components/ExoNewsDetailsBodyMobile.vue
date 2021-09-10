<template>
  <div class="newsBodyHeader">
    <div class="d-flex flex-row py-2 ms-2 font-weight-bold subtitle-1">
      {{ newsTitle }}
    </div>
    <div class="d-flex flex-row py-2">
      <div class="flex-column ms-2">
        <exo-user-avatar
          :username="news.author"
          :fullname="news.author"
          :size="32"
          :title="news.author"
          :retrieve-extra-information="false"
          :labels="labels"
          class="align-center my-auto text-truncate flex-grow-0 flex" />
      </div>
      <v-icon>
        mdi-chevron-right
      </v-icon>
      <div class="flex-column">
      </div>
      <div class="flex-column">
        <exo-space-avatar
          :space="currentSpace"
          :size="32"
          :labels="labels"
          class="align-center my-auto text-truncate flex-grow-0 flex" />
      </div>
    </div>
    <div id="informationNews" class="newsInformation d-flex flex-row py-2">
      <div class="newsAuthor">
        <template v-if="publicationDate">
          <date-format
            :value="publicationDate"
            :format="dateFormat"
            class="newsInformationValue newsPostedDate news-details-information" />
        </template>
        <span v-else-if="postedDate" class="newsInformationValue newsPostedDate news-details-information">- {{ postedDate }}</span>
      </div>
      <div v-if="showUpdateInfo" class="newsUpdater">
        <div v-if="publicationState !== 'staged'">
          <span class="newsInformationLabel">{{ $t('news.activity.lastUpdated') }} </span>
        </div>
        <div v-else>
          <span class="newsInformationLabel">{{ $t('news.details.scheduled') }} </span>
        </div>
        <div>
          <template v-if="publicationState !== 'staged' && updatedDate">
            <date-format
              :value="updatedDate"
              :format="dateFormat"
              class="newsInformationValue newsUpdatedDate" />
          </template>
          <template v-else-if="publicationState === 'staged'">
            <date-format
              :value="scheduleDate"
              :format="dateFormat"
              class="newsInformationValue newsUpdatedDate" />
            <span class="newsInformationValue">-</span>
            <date-format
              :value="scheduleDate"
              :format="dateTimeFormat"
              class="newsInformationValue newsUpdatedDate ml-1 me-1" />
          </template>
          <div v-else-if="news.updatedDate" class="newsInformationValue newsUpdatedDate">{{ news.updatedDate }}</div>
          <div v-if="notSameUpdater">
            <span class="newsInformationLabel"> {{ $t('news.activity.by') }} </span>
            <a :href="updaterProfileURL" class="newsInformationValue newsUpdaterName">{{ updaterFullName }}</a>
          </div>
        </div>
      </div>
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
    currentSpace: null,
  }),
  computed: {
    newsTitle() {
      return this.news && this.news.title;
    },
    spaceUrl() {
      return this.news && this.news.spaceAvatarUrl;
    },
    labels() {
      return {
        CancelRequest: this.$t('profile.CancelRequest'),
        Confirm: this.$t('profile.Confirm'),
        Connect: this.$t('profile.Connect'),
        Ignore: this.$t('profile.Ignore'),
        RemoveConnection: this.$t('profile.RemoveConnection'),
        StatusTitle: this.$t('profile.StatusTitle'),
        join: this.$t('space.join'),
        leave: this.$t('space.leave'),
        members: this.$t('space.members'),
      };
    },
    updaterFullName() {
      return (this.news && this.news.updaterFullName) || (this.updaterIdentity && this.updaterIdentity.profile && this.updaterIdentity.profile.fullname);
    },
    updaterProfileURL() {
      return this.news && `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.news.updater}`;
    },
    notSameUpdater() {
      return this.news && this.news.updater !=='__system' && (this.news.updater !== this.news.author || this.news.authorFullName !== this.news.updaterFullName);
    },
    publicationDate() {
      return this.news && this.news.publicationDate && this.news.publicationDate.time && new Date(this.news.publicationDate.time);
    },
    updatedDate() {
      return this.news && this.news.updateDate && this.news.updateDate.time && new Date(this.news.updateDate.time);
    },
  },
  created() {
    if (this.news && this.news.spaceId) {
      this.getSpaceById();
    }
  },
  methods: {
    getSpaceById() {
      this.$spaceService.getSpaceById(this.news.spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.currentSpace = space;
          }
        });
    }
  }
};
</script>