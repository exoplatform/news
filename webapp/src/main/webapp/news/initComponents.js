import NewsApp  from './components/NewsApp.vue';
import NewsSpacesSelector from './components/NewsSpacesSelector.vue';
import NewsSpacesSharedIn from './components/NewsSpacesSharedIn.vue';
import NewsActivitySharingSpacesDrawer from './components/NewsActivitySharingSpacesDrawer.vue';

const components = {
  'news-app': NewsApp,
  'news-spaces-selector': NewsSpacesSelector,
  'news-spaces-shared-in': NewsSpacesSharedIn,
  'news-activity-sharing-spaces-drawer': NewsActivitySharingSpacesDrawer,
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