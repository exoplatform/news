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

export function initExtensions() {
  if (eXo.env.portal.spaceId) {
    canUserCreateNews(eXo.env.portal.spaceId)
      .then(canCreateNews => {
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
