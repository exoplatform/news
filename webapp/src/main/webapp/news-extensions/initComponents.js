import AnalyticsTableCellContentValue from './components/analytics/AnalyticsTableCellContentValue.vue';

const components = {
  'analytics-table-cell-content-value': AnalyticsTableCellContentValue,
};

for (const key in components) {
  Vue.component(key, components[key]);
}