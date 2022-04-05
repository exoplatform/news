<template>
  <v-list-item class="clickable" :href="url">
    <v-list-item-icon class="me-3 my-auto">
      <v-icon size="22" class="icon-default-color"> fas fa-newspaper </v-icon>
    </v-list-item-icon>

    <v-list-item-content>
      <v-list-item-title class="text-color body-2">{{ activityTitle }}</v-list-item-title>
    </v-list-item-content>

    <v-list-item-action>
      <v-btn icon>
        <v-icon class="yellow--text text--darken-2" size="18">fa-star</v-icon>
      </v-btn>
    </v-list-item-action>
  </v-list-item>
</template>
<script>
export default {
  props: {
    id: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    activityTitle: '',
    url: ''
  }),
  created() {
    this.$newsServices.getNewsById(this.id, false)
      .then(news => {
        this.activityTitle = news.title;
        this.url = news.url;
      });
  },
};
</script>
