import '../css/news.less';
import './components/initComponents.js';

import NewsApp from './components/NewsApp.vue';
import {newsConstants} from '../js/newsConstants';

Vue.component('news-app', NewsApp);

Vue.use(Vuetify);

const vuetify = new Vuetify({
  iconfont: '',
});

// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('News');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
// should expose the locale resources as REST API
const url = `${newsConstants.PORTAL}/${newsConstants.PORTAL_REST}/i18n/bundle/locale.portlet.news.News-${lang}.json`;

const appId = 'NewsApp';

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    new Vue({
      template: `<news-app id="${appId}" v-cacheable />`,
      i18n,
      vuetify,
    }).$mount(appElement);
  });
}