import NewsListView from './components/NewsListView.vue';
import NewsLatestView from './components/views/NewsLatestView.vue';
import NewsSliderView from './components/views/NewsSliderView.vue';
import NewsSettings from './components/settings/NewsSettings.vue';
import NewsSettingsDrawer from './components/settings/NewsSettingsDrawer.vue';

const components = {
  'news-list-view': NewsListView,
  'news-lastest-view': NewsLatestView,
  'news-slider-view': NewsSliderView,
  'news-settings': NewsSettings,
  'news-settings-drawer': NewsSettingsDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
