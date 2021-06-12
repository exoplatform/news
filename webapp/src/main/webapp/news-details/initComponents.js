import ExoNewsArchive  from './components/ExoNewsArchive.vue';
import ExoNewsDetails  from './components/ExoNewsDetails.vue';
import ExoNewsDetailsActionMenu from './components/ExoNewsDetailsActionMenu.vue';
import ExoNewsPin from './components/ExoNewsPin.vue';
import ExoNewsDetailsActivity from './components/ExoNewsDetailsActivity.vue';

const components = {
  'exo-news-archive': ExoNewsArchive,
  'exo-news-details': ExoNewsDetails,
  'exo-news-details-activity': ExoNewsDetailsActivity,
  'exo-news-details-action-menu': ExoNewsDetailsActionMenu,
  'exo-news-pin': ExoNewsPin,
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