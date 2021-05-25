import ShareNewsActivityDrawer from './components/ShareNewsActivityDrawer.vue';

const components = {
  'share-news-activity-drawer': ShareNewsActivityDrawer,
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