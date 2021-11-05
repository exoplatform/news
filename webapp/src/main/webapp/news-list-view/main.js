import './initComponents.js';
import './extensions.js';

import * as newsListService from './js/NewsListService.js';
if (!Vue.prototype.$newsListService) {
  window.Object.defineProperty(Vue.prototype, '$newsListService', {
    value: newsListService,
  });
}

// get override components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('NewsListView');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

// getting language of the PLF
const lang = eXo && eXo.env && eXo.env.portal.language || 'en';

// should expose the locale resources as REST API
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.news.News-${lang}.json`;

// getting locale resources
export function init(params) {
  const appId = params.appId;
  const viewTemplate = params.viewTemplate;
  const saveSettingsURL = params.saveSettingsURL;
  const newsTarget = params.newsTarget;
  const limit = params.limit;

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    Vue.createApp({
      data: {
        saveSettingsURL,
        viewTemplate,
        newsTarget,
        limit,
      },
      template: `<news-list-view
                  id="${appId}"
                  :view-template="viewTemplate"
                  :news-target="newsTarget"
                  :limit="limit" />`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, `News List View - ${viewTemplate}`);
  });
}
