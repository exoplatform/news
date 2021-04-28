<template>
  <v-app>
    <v-snackbar
      :value="displayAlerts"
      color="transparent"
      elevation="0"
      app
      left>
      <exo-news-notification-alert
        v-for="(alert, index) in alerts"
        :key="index"
        :alert="alert"
        @dismissed="deleteAlert(index)" />
    </v-snackbar>
  </v-app>
</template>

<script>
import * as newsServices from '../../services/newsServices';

export default {
  props: {
    name: {
      type: String,
      default: null
    },
  },
  data: () => ({
    alerts: [],
  }),
  computed: {
    displayAlerts() {
      return this.alerts && this.alerts.length;
    },
  },
  created() {
    this.$root.$on('news-notification-alert', alert => this.alerts.push(alert));
    this.$root.$on('news-deleted', news => {
      if (news && news.newsId) {
        const clickMessage = this.$t('news.undoRemoveActivity');
        const message = this.$t('news.activityDeleteSuccess');
        this.$root.$emit('news-notification-alert', {
          message,
          type: 'success',
          click: () => this.undoDeleteNews(news.newsId),
          clickMessage,
        });
      }
    });
  },
  methods: {
    deleteAlert(index) {
      this.alerts.splice(index, 1);
      this.$forceUpdate();
    },
    undoDeleteNews(newsId) {
      return newsServices.undoDeleteNews(newsId)
        .then(() => {
          this.$root.$emit('undoDelete');
          this.$forceUpdate();
        });
    }
  },
};
</script>