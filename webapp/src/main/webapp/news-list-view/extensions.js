extensionRegistry.registerComponent('NewsList', 'views', {
  id: 'NewsSlider',
  vueComponent: Vue.options.components['news-slider-view'],
  rank: 10,
});

extensionRegistry.registerComponent('NewsList', 'views', {
  id: 'NewsLatest',
  vueComponent: Vue.options.components['news-lastest-view'],
  rank: 20,
});
