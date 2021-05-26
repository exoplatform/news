import ShareNewsActivity from './components/ShareNewsActivity.vue';

const components = {
  'share-news-activity': ShareNewsActivity,
};
for (const key in components) {
  Vue.component(key, components[key]);
}
import * as  newsServices from '../services/newsServices';

if (!Vue.prototype.$newsServices) {
  window.Object.defineProperty(Vue.prototype, '$newsServices', {
    value: newsServices,
  });
}