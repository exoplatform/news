<template>
  <div class="newsContent">
    <div :title="newsTitle" class="newsDetailsTitle mt-2 pl-2 pr-2 ms-2 font-weight-bold subtitle-1">
      {{ newsTitle }}
    </div>
    <div class="d-flex flex-row pa-2 ms-2">
      <div v-if="news" class="flex-column">
        <exo-user-avatar
          :username="newsAuthor"
          :fullname="authorFullName"
          :size="32"
          :title="newsAuthor"
          :retrieve-extra-information="false"
          :labels="labels"
          class="align-center my-auto text-truncate flex-grow-0 flex" />
      </div>
      <v-icon>
        mdi-chevron-right
      </v-icon>
      <div class="flex-column">
      </div>
      <div v-if="space" class="flex-column">
        <exo-space-avatar
          :space="space"
          :size="32"
          :labels="labels"
          class="align-center my-auto text-truncate flex-grow-0 flex" />
      </div>
    </div>
    <div class="d-flex flex-row caption text-light-color">
      <exo-news-details-time :news="news" />
    </div>
    <div class="d-flex flex-row caption font-italic grey--text text-darken-1 pa-4">
      <span v-html="newsSummary"></span>
    </div>
    <v-divider class="mx-4" />
    <div class="d-flex flex-row pa-4 newsBody">
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
    newsId: {
      type: String,
      required: false,
      default: null
    },
    space: {
      type: Object,
      required: false,
      default: null
    },
  },
  data: () => ({
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
    publicationState() {
      return this.news && this.news.publicationState;
    },
    newsSummary() {
      return this.news && this.targetBlank(this.news.summary);
    },
    newsBody() {
      return this.news && this.targetBlank(this.news.body);
    },
    authorFullName() {
      return this.news && (this.news.authorFullName || this.news.authorDisplayName);
    },
    newsAuthor() {
      return this.news && this.news.author;
    },
  },
  methods: {
    targetBlank: function (content) {
      const internal = location.host + eXo.env.portal.context;
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      const links = docElement.getElementsByTagName('a');
      for (const link of links) {
        let href = link.href.replace(/(^\w+:|^)\/\//, '');
        if (href.endsWith('/')) {
          href = href.slice(0, -1);
        }
        if (href !== location.host && !href.startsWith(internal)) {
          link.setAttribute('target', '_blank');
          link.setAttribute('rel', 'noopener noreferrer');
        }
      }
      return docElement.innerHTML;
    },
  }
};
</script>