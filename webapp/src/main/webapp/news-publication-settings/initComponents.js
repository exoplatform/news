import NewsTargetsSettings from './components/NewsTargetsSettings.vue';

const components = {
  'news-targets-settings': NewsTargetsSettings,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

