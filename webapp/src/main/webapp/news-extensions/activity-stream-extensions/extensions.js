/*
  Copyright (C) 2023 eXo Platform SAS.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import {canUserCreateNews} from '../../services/newsServices.js';
const lang = eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.news.News-${lang}.json`;

const i18nPromise = exoi18n.loadLanguageAsync(lang, url).then(i18n => new Vue({i18n}));

const newsActivityTypeExtensionOptions = {
  name: 'News',
  getExtendedComponent: (activity, isActivityDetail) => {
    if (activity && isActivityDetail) {
      return {
        component: Vue.options.components['exo-news-details-activity'],
        overrideHeader: true,
        overrideFooter: false,
      };
    }
  },
  extendSharedActivity: (activity, isActivityDetail) => isActivityDetail,
  showSharedInformationFooter: (activity, isActivityDetail) => isActivityDetail,
  init: (activity, isActivityDetail) => {
    let activityId = activity.id;
    if (activity.parentActivity) {
      activityId = activity.parentActivity.id;
    }
    if (!activity.news || isActivityDetail) {
      return Vue.prototype.$newsServices.getNewsByActivityId(activityId)
        .then(news => activity.news = news);
    }
  },
  canEdit: () => false,
  canShare: () => true,
  hideOnDelete: true,
  supportsThumbnail: true,
  windowTitlePrefixKey: 'news.window.title',
  getThumbnail: (activity) => activity?.news?.illustrationURL && `${activity?.news?.illustrationURL}&size=305x285` || '/news/images/news.png',
  getThumbnailProperties: (activity) => !(activity?.news?.illustrationURL) && {
    height: '90px',
    width: '90px',
    noBorder: true,
  } || null,
  isUseSameViewForMobile: (activity) => !activity?.news?.illustrationURL,
  getTitle: (activity) => {
    const news = activity?.news;
    if (news?.title) {
      return news.title;
    }
    return '';
  },
  getSourceLink: (activity) => `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/activity?id=${!activity.parentActivity ? activity.id : activity.parentActivity.id}`,
  getSummary: (activity) => {
    const news = activity?.news;
    if (news?.summary) {
      return news.summary;
    } else if (news?.body) {
      return Vue.prototype.$utils.htmlToText(news.body);
    }
    return '';
  },
  getTooltip: (activity, isActivityDetail) => !isActivityDetail && activity && 'news.activity.clickToShowDetail',
};

export function initExtensions() {
  extensionRegistry.registerExtension('activity', 'type', {
    type: 'news',
    options: newsActivityTypeExtensionOptions,
  });
  if (eXo.env.portal.spaceId) {
    canUserCreateNews(eXo.env.portal.spaceId).then(canCreateNews => {
      if (canCreateNews) {
        return i18nPromise.then(() => {
          extensionRegistry.registerComponent('ActivityComposerAction', 'activity-composer-action', {
            id: 'switchNewsButton',
            vueComponent: Vue.options.components['activity-switch-to-news'],
            rank: 10,
          });
          extensionRegistry.registerComponent('ActivityComposerFooterAction', 'activity-composer-footer-action', {
            id: 'writeNewsButton',
            vueComponent: Vue.options.components['activity-write-news-composer'],
            rank: 30,
          });
          extensionRegistry.registerComponent('ActivityToolbarAction', 'activity-toolbar-action', {
            id: 'writeNewsToolbarButton',
            vueComponent: Vue.options.components['activity-write-news-toolbar-action'],
            rank: 30,
          });
        });
      }
    });
  }
}