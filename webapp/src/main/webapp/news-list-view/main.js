/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import './initComponents.js';
import './extensions.js';

import * as newsListService from './js/NewsListService.js';
import * as  newsServices from '../services/newsServices';
import * as newsTargetingService from '../services/newsTargetingService.js';

if (!Vue.prototype.$newsTargetingService) {
  window.Object.defineProperty(Vue.prototype, '$newsTargetingService', {
    value: newsTargetingService,
  });
}

if (!Vue.prototype.$newsServices) {
  window.Object.defineProperty(Vue.prototype, '$newsServices', {
    value: newsServices,
  });
}

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
  const header = params.header;
  const limit = params.limit === '' ? '4' : params.limit;
  const showHeader = viewTemplate === 'NewsSlider' ? false: params.showHeader;
  const showSeeAll = viewTemplate === 'NewsSlider' ? false: params.showSeeAll;
  const showArticleTitle = params.showArticleTitle === '' ? true : params.showArticleTitle;
  const showSummary = viewTemplate === 'NewsLatest' ? false: params.showSummary;
  const showArticleImage = params.showArticleImage === '' ? true : params.showArticleImage;
  const showArticleAuthor = viewTemplate === 'NewsLatest' ? false: params.showArticleAuthor;
  const showArticleSpace = params.showArticleSpace === '' ? true : params.showArticleSpace;
  const showArticleReactions = params.showArticleReactions === '' ? true : params.showArticleReactions;
  const showArticleDate  = params.showArticleDate === '' ? true : params.showArticleDate;
  const seeAllUrl = params.seeAllUrl;
  const selectedType = params.selectedType === '' ? 'lastPublished' : params.selectedType;

  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    // init Vue app when locale resources are ready
    Vue.createApp({
      data: {
        saveSettingsURL,
        viewTemplate,
        newsTarget,
        header,
        limit,
        showHeader,
        showSeeAll,
        showArticleTitle,
        showSummary,
        showArticleImage,
        showArticleAuthor,
        showArticleSpace,
        showArticleReactions,
        showArticleDate,
        seeAllUrl,
        selectedType,
      },
      template: `<news-list-view
                  id="${appId}"
                  :view-template="viewTemplate"
                  :news-target="newsTarget"
                  :header="header"
                  :show-article-author="showArticleAuthor"
                  :show-article-image="showArticleImage"
                  :show-article-reactions="showArticleReactions"
                  :show-article-space="showArticleSpace"
                  :show-article-title="showArticleTitle"
                  :show-header="showHeader"
                  :show-see-all="showSeeAll"
                  :show-summary="showSummary"
                  :limit="limit"/>`,
      vuetify: Vue.prototype.vuetifyOptions,
      i18n,
    }, `#${appId}`, `News List View - ${viewTemplate}`);
  });
}
