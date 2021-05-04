import ExoNewsActivityComposer  from './components/ExoNewsActivityComposer.vue';
import ExoNewsDraft from './components/ExoNewsDraft.vue';
import ExoNewsFileDrop from './components/ExoNewsFileDrop.vue';

const components = {
  'exo-news-activity-composer': ExoNewsActivityComposer,
  'exo-news-draft': ExoNewsDraft,
  'exo-news-file-drop': ExoNewsFileDrop,
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