import './components/initComponents.js';
import { newsConstants } from '../js/newsConstants.js';

Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});

// getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

// should expose the locale resources as REST API
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
export function init(showPin) {
  // getting locale resources
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    newsActivityComposerApp = new Vue({
      el: '#NewsComposerApp',
      data: function() {
        return {
          newsId: getURLQueryParam('newsId'),
          spaceId: getURLQueryParam('spaceId'),
          activityId: getURLQueryParam('activityId'),
          showPinInput: showPin
        };
      },
      template: '<exo-news-activity-composer :news-id="newsId" :space-id="spaceId" :activity-id="activityId" :show-pin-input="showPinInput"></exo-news-activity-composer>',
      i18n,
      vuetify
    });
  });
}

export function destroy() {
  if(newsActivityComposerApp) {
    newsActivityComposerApp.$destroy();
  }
}

function getURLQueryParam(paramName) {
  const urlParams = new URLSearchParams(window.location.search);
  if(urlParams.has(paramName)) {
    return urlParams.get(paramName);
  }
}