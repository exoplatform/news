import ExoNewsArchive  from './components/ExoNewsArchive.vue';
import ExoNewsDetails  from './components/ExoNewsDetails.vue';
import ExoNewsDetailsActionMenu from './components/ExoNewsDetailsActionMenu.vue';
import ExoNewsPin from './components/ExoNewsPin.vue';
import ExoNewsShareActivity  from './components/ExoNewsShareActivity.vue';
import ExoNewsShareActivityDrawer from './components/ExoNewsShareActivityDrawer.vue';
import ExoNewsSpaceItem from './components/ExoNewsSpaceItem.vue';
import ExoNewsSpaceSuggester from './components/ExoNewsSpaceSuggester.vue';

const components = {
  'exo-news-archive': ExoNewsArchive,
  'exo-news-details': ExoNewsDetails,
  'exo-news-details-action-menu': ExoNewsDetailsActionMenu,
  'exo-news-pin': ExoNewsPin,
  'exo-news-share-activity': ExoNewsShareActivity,
  'exo-news-share-activity-drawer': ExoNewsShareActivityDrawer,
  'exo-news-space-item': ExoNewsSpaceItem,
  'exo-news-space-suggester': ExoNewsSpaceSuggester,
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