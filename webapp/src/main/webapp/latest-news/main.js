import '../css/news.less';
import './components/initComponents.js';
import { newsConstants } from '../js/newsConstants.js';
// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('latestNews');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

Vue.use(Vuetify);

const vuetify = new Vuetify({
  dark: true,
  iconfont: '',
});


let latestNewsDetails;
export function initLatestNews(params) {
  // getting language of the PLF
  const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  // should expose the locale resources as REST API
  const url = `${newsConstants.PORTAL}/${newsConstants.PORTAL_REST}/i18n/bundle/locale.portlet.news.News-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    latestNewsDetails = new Vue({
      el: '#latestNewsDetails',
      data: function() {
        return {
          newsInfo: params.newsInfo,
          seeAllLabel: params.seeAllLabel,
          header: params.header,
          url: params.url,
          isShowHeader: params.isShowHeader
        };
      },
      template: '<exo-news-latest :news-info="newsInfo" :header="header" :see-all="seeAllLabel" :url="url"  :is-show-header="isShowHeader"></exo-news-latest>',
      i18n,
      vuetify,
    });
  });
}

export function destroy() {
  if(latestNewsDetails) {
    latestNewsDetails.$destroy();
  }
}