import './components/initComponents.js';
import { newsConstants } from '../js/newsConstants.js';

// getting language of the PLF 
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale ressources as REST API 
const url = `${newsConstants.PORTAL}/${newsConstants.PORTAL_REST}/i18n/bundle/locale.portlet.news.News-${lang}.json`;

// get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('NewsActivityComposer');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

let newsActivityComposerApp;
// getting locale ressources
export function init(showPin) {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
  // init Vue app when locale ressources are ready
    newsActivityComposerApp = new Vue({
      el: '#newsActivityComposer',
      data: function() {
        return {
          showPinInput: showPin,
        };
      },
      template: '<exo-news-activity-composer :show-pin-input="showPinInput"></exo-news-activity-composer>',
      i18n
    });
  });
}

export function destroy() {
  if(newsActivityComposerApp) {
    newsActivityComposerApp.$destroy();
  }
}