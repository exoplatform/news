import NewsLockReaction from './components/NewsLockReaction.vue';
const components = {
  'exo-news-lock-reaction': NewsLockReaction,
};

for (const key in components) {
  Vue.component(key, components[key]);
}