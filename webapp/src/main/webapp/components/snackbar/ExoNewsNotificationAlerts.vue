<template>
  <v-app>
    <v-snackbar
      :value="displayAlerts"
      color="transparent"
      elevation="0"
      app
      left>
      <exo-news-notification-alert
        v-for="alert in alerts"
        :key="alert.message"
        :alert="alert"
        @dismissed="deleteAlert(alert)" />
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
        const clickMessage = this.$t('news.details.undoDelete');
        const message = this.$t('news.details.deleteSuccess');
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
    addAlert(alert) {
      const time = 5000;
      if (alert) {
        this.alerts.push(alert);
        window.setTimeout(() => this.deleteAlert(alert), time);
      }
    },
    deleteAlert(alert) {
      const index = this.alerts.indexOf(alert);
      this.alerts.splice(index, 1);
      this.$forceUpdate();
    },
    undoDeleteNews(newsId, alert) {
      return newsServices.undoDeleteNews(newsId)
        .then(() => {
          this.$root.$emit('undoDelete');
          this.deleteAlert(alert);
          this.addAlert({
            message: this.$t('news.details.deleteCanceled'),
            type: 'success',
          });
        });
    }
  },
};
</script>