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
    this.$root.$on('confirm-news-deletion', news => {
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
    this.$root.$on('news-shared', (newsId, spaces) => {
      const spacesList = [];
      if (newsId && spaces && spaces.length > 0) {
        spaces.forEach(space => spacesList.push(this.truncateString(space)));
        const message = `${this.$t('news.share.message')} ${spacesList.join(', ')}`;
        this.$root.$emit('news-notification-alert', {
          message,
          type: 'success',
          spaces,
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
      return this.$newsServices.undoDeleteNews(newsId)
        .then(() => {
          this.deleteAlert(alert);
          this.addAlert({
            message: this.$t('news.details.deleteCanceled'),
            type: 'success',
          });
        });
    },
    truncateString(str) {
      if (str.length <= 10) {
        return str;
      }
      return str.slice(0, 10).concat('...');
    }
  },
};
</script>