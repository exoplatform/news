<template>
  <div class="newsBodyHeader">
    <div class="d-flex flex-row pa-2 font-weight-bold subtitle-1">
      {{ newsTitle }}
    </div>
    <div class="d-flex flex-row pa-2">
      <div class="flex-column">
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
    <div class="d-flex flex-row caption grey--text text-darken-4">
      <v-icon x-small class="ms-4 me-1">far fa-clock</v-icon>
      <template v-if="publicationDate">
        <date-format
          :value="publicationDate"
          :format="dateFormat" />
      </template>
      <span v-else-if="postedDate">- {{ postedDate }}</span>
    </div>
    <div class="d-flex flex-row caption font-italic grey--text text-darken-1 pa-4">
      <span v-html="newsSummary"></span>
    </div>
    <v-divider class="mx-4 my-4" />
    <div class="d-flex flex-row pa-4">
      <span v-html="newsBody"></span>
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
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
    dateTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
    },
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
    postedDate() {
      return this.news && this.news.postedDate;
    },
    publicationState() {
      return this.news && this.news.publicationState;
    },
    scheduleDate() {
      return this.news && this.news.schedulePostDate;
    },
    showUpdateInfo() {
      return this.updatedDate || (this.news && this.news.updatedDate && this.news.updatedDate  !== 'null');
    },
    newsSummary() {
      return this.news && this.targetBlank(this.news.summary);
    },
    newsBody() {
      return this.news && this.targetBlank(this.news.body);
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
    },
    targetBlank: function (content) {
      const internal = location.host + eXo.env.portal.context;
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      const links = docElement.getElementsByTagName('a');
      for (let i=0; i < links.length ; i++) {
        let href = links[i].href.replace(/(^\w+:|^)\/\//, '');
        if (href.endsWith('/')) {
          href = href.slice(0, -1);
        }
        if (href !== location.host && !href.startsWith(internal)) {
          links[i].setAttribute('target', '_blank');
          links[i].setAttribute('rel', 'noopener noreferrer');
        }
      }
      return docElement.innerHTML;
    },
  }
};
</script>