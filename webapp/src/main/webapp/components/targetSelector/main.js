import ExoNewsTargetsSelector from './ExoNewsTargetsSelector.vue';

const components = {
  'exo-news-targets-selector': ExoNewsTargetsSelector,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
