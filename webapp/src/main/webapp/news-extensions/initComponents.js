import AnalyticsTableCellContentValue from './components/analytics/AnalyticsTableCellContentValue.vue';
import ActivitySwitchToNews from './components/activity/ActivitySwitchToNews.vue';
import ActivityWriteNews from './components/activity/ActivityWriteNews.vue';
import NewsFavoriteItem from './components/favorites/NewsFavoriteItem.vue';
const components = {
  'analytics-table-cell-content-value': AnalyticsTableCellContentValue,
  'activity-switch-to-news': ActivitySwitchToNews,
  'activity-write-news': ActivityWriteNews,
  'news-favorite-item': NewsFavoriteItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}