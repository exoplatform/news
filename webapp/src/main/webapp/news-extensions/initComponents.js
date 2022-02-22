import AnalyticsTableCellContentValue from './components/analytics/AnalyticsTableCellContentValue.vue';
import ActivitySwitchToNews from './components/activity/ActivitySwitchToNews.vue';
import ActivityWriteNews from './components/activity/ActivityWriteNews.vue';

const components = {
  'analytics-table-cell-content-value': AnalyticsTableCellContentValue,
  'activity-switch-to-news': ActivitySwitchToNews,
  'activity-write-news': ActivityWriteNews,
};

for (const key in components) {
  Vue.component(key, components[key]);
}