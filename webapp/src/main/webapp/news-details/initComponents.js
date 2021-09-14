import ExoNewsArchive  from './components/ExoNewsArchive.vue';
import ExoNewsDetails  from './components/ExoNewsDetails.vue';
import ExoNewsDetailsActionMenu from './components/ExoNewsDetailsActionMenu.vue';
import ExoNewsPin from './components/ExoNewsPin.vue';
import ExoNewsDetailsActivity from './components/ExoNewsDetailsActivity.vue';
import ExoNewsDetailsToolBar from './components/ExoNewsDetailsToolBar.vue';
import ExoNewsDetailsToolBarMobile from './components/mobile/ExoNewsDetailsToolBarMobile.vue';
import ExoNewsDetailsBodyMobile from './components/mobile/ExoNewsDetailsBodyMobile.vue';
import ExoNewsDetailsBody from './components/ExoNewsDetailsBody.vue';
import ExoNewsDetailsTime from './components/ExoNewsDetailsTime.vue';

const components = {
  'exo-news-archive': ExoNewsArchive,
  'exo-news-details': ExoNewsDetails,
  'exo-news-details-activity': ExoNewsDetailsActivity,
  'exo-news-details-action-menu': ExoNewsDetailsActionMenu,
  'exo-news-pin': ExoNewsPin,
  'exo-news-details-toolbar': ExoNewsDetailsToolBar,
  'exo-news-details-toolbar-mobile': ExoNewsDetailsToolBarMobile,
  'exo-news-details-body-mobile': ExoNewsDetailsBodyMobile,
  'exo-news-details-body': ExoNewsDetailsBody,
  'exo-news-details-time': ExoNewsDetailsTime,
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