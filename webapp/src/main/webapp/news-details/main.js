import '../css/news.less';
import './components/initComponents.js';
import { newsConstants } from '../js/newsConstants.js';
// get overridden components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('NewsDetails');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

let newsDetails;
// getting locale resources
export function init(params) {
  // getting language of the PLF
  const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  // should expose the locale resources as REST API
  const url = `${newsConstants.PORTAL}/${newsConstants.PORTAL_REST}/i18n/bundle/locale.portlet.news.News-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    newsDetails = new Vue({
      el: '#newsDetails',
      data: function() {
        return {
          news: params.news,
          newsId: params.newsId,
          activityId: params.activityId,
          showEditButton: params.showEditButton,
          showPinInput: params.showPinInput,
          showShareButton : params.showShareButton,
        };
      },
      template: '<exo-news-details :news="news" :news-id="newsId" :activity-id="activityId" :show-edit-button="showEditButton" :show-pin-input="showPinInput" :show-share-button="showShareButton"></exo-news-details>',
      i18n
    });
  });
}

export function destroy() {
  if(newsDetails) {
    newsDetails.$destroy();
  }
}