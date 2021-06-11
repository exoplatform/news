import * as  newsServices from '../services/newsServices';

if (!Vue.prototype.$newsService) {
  window.Object.defineProperty(Vue.prototype, '$newsService', {
    value: newsServices,
  });
}

import {initExtensions} from './extensions.js';

export function init() {
  initExtensions();
}