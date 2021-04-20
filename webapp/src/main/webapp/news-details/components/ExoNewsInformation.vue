<template>
  <v-card
    class="newsInfo"
    outlined>
    <v-list>
      <v-list-item three-line>
        <v-img :src="newsIllustrationURL"
               :height="spaceAvatarSize"
               :width="spaceAvatarSize"
               :max-height="spaceAvatarSize"
               :max-width="spaceAvatarSize" ></v-img>
        <v-list-item-content>
          <div class="font-weight-bold text-truncate ml-2">
            {{ news.spaceDisplayName }}
          </div>
          <v-list-item-title class="previewNewsTitle ml-2 mt-4">
            <a :href="news.authorProfileURL">
              <v-avatar size="24" class="mr-2">
                <img :src="news.profileAvatarURL">
              </v-avatar>
              <span class="text-truncate font-italic caption grey--text text--darken-2">{{ news.authorFullName }}</span>
            </a>
            <v-icon x-small >fas fa-chevron-right</v-icon>
            <a :href="news.spaceUrl">
              <v-avatar tile size="24" class="rounded mr-2">
                <img :src="news.spaceAvatarUrl">
              </v-avatar>
              <span class="text-truncate font-italic caption grey--text text--darken-2">{{ news.spaceDisplayName }}</span>
            </a>
          </v-list-item-title>
          <v-list-item-subtitle>

          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      required: true,
      default: null,
    }
  },
  data: () => ({
    spaceAvatarSize: 70,
  }),
  computed : {
    newsIllustrationURL() {
      return this.news && this.news.illustrationURL || '/news/images/newsImageDefault.png';
    }
  }
};
</script>