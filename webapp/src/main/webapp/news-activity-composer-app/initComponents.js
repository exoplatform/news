import ExoNewsActivityComposer  from './components/ExoNewsActivityComposer.vue';
import ExoNewsFileDrop from './components/ExoNewsFileDrop.vue';
import ExoNewsPublishDrawer from './components/ExoNewsPublishDrawer.vue';
import * as  newsServices from '../services/newsServices.js';
import * as  newsUtils from '../js/newsUtils.js';

const components = {
  'exo-news-activity-composer': ExoNewsActivityComposer,
  'exo-news-file-drop': ExoNewsFileDrop,
  'exo-news-publish-drawer': ExoNewsPublishDrawer,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

if (!Vue.prototype.$newsServices) {
  window.Object.defineProperty(Vue.prototype, '$newsServices', {
    value: newsServices,
  });
}

if (!Vue.prototype.$newsUtils) {
  window.Object.defineProperty(Vue.prototype, '$newsUtils', {
    value: newsUtils,
  });
}