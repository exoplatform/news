<template>
  <div>
    <div class="flex-column">
      <exo-user-avatar
        :username="author"
        :fullname="authorFullName"
        :size="32"
        :title="author"
        :retrieve-extra-information="false"
        :labels="labels"
        bold-title
        class="align-center my-auto text-truncate white--text flex-grow-0 flex" />
    </div>
    <v-icon>
      mdi-chevron-right
    </v-icon>
    <div class="flex-column">
      <exo-space-avatar
        :space="space"
        :size="32"
        :labels="labels"
        class="align-center my-auto text-truncate white--text flex-grow-0 flex"
        bold-title
        link-style />
    </div>
    <div class="flex-column my-auto font-weight-bold body-2">
      <v-icon small class="ms-4 me-1">fas fa-clock</v-icon>
    </div>
    <div class="flex-column me-1 my-auto font-weight-bold body-2">
      <span>{{ postDate }}, 2020</span>
    </div>
    <div class="flex-column my-auto">
      <v-icon
          class="likeIconStyle baseline-vertical-align ms-6 me-2"
          size="14">
        fa-thumbs-up
      </v-icon>
    </div>
    <div class="flex-column subtitle-2 my-auto me-4">
      <span class="counterStyle">{{ likeSize }}</span>
    </div>
    <div class="flex-column my-auto">
      <v-icon
          class="commentIconStyle baseline-vertical-align mx-auto me-2"
          size="14">
        fa-comment
      </v-icon>
    </div>
    <div class="counterStyle flex-column subtitle-2 my-auto">
      <span>{{ commentsSize }}</span>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    author: {
      type: String,
      default: ''
    },
    authorFullName: {
      type: String,
      default: ''
    },
    spaceId: {
      type: String,
      default: ''
    },
    postDate: {
      type: String,
      default: ''
    },
  },
  data: () => ({
    space: null,
    commentsSize: 0,
    likeSize: 0,
  }),
  computed: {
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
  },
  created() {
    this.getSpaceById(this.spaceId);
    this.retrieveComments();
    this.getActivityById();
  },
  methods: {
    getSpaceById(spaceId) {
      this.$spaceService.getSpaceById(spaceId, 'identity')
        .then((space) => {
          if (space && space.identity && space.identity.id) {
            this.space = space;
          }
        });
    },
    getActivityById() {
      this.loading = true;
      this.likeSize = 5;
    },
    retrieveComments() {
      this.loading = true;
      this.commentsSize = 3;
    },
  }
};
</script>