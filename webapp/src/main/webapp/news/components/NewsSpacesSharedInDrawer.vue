<template>
  <v-app class="newsSpacesSharedInDrawer">
    <exo-drawer
      ref="newsSpacesSharedInDrawer"
      body-classes="hide-scroll decrease-z-index-more"
      right>
      <template slot="title">
        {{ $t('news.app.sharedInSpaces') }}
      </template>
      <template slot="content">
        <v-list>
          <v-list-item v-for="(act) in sharedActivities" :key="act" class="spaceSharedIn">
            <v-list-item-avatar>
              <v-avatar size="28" class="rounded">
                <v-img :src="act.spaceAvatar" :alt="act.spaceAvatar" />
              </v-avatar>
            </v-list-item-avatar>
            <v-list-item-content class="text-truncate">
              {{ act.spaceDisplayName }}
            </v-list-item-content>
            <v-list-item-action>
              <v-btn
                :href="act.activityUrl"
                class="btn text-capitalize">
                {{ $t('news.app.viewArticle') }}
              </v-btn>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </template>
    </exo-drawer>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    sharedActivities: []
  }),
  created() {
    this.$root.$on('news-spaces-drawer-open', sharedActivities => {
      this.sharedActivities = sharedActivities;
      this.open();
    });
  },
  methods: {
    open() {
      if (this.$refs.newsSpacesSharedInDrawer) {
        this.$refs.newsSpacesSharedInDrawer.open();
      }
    },
  }
};
</script>

<style scoped>

</style>