/*
 * Copyright (C) 2023 eXo Platform SAS
 *
 *  This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <gnu.org/licenses>.
 */

extensionRegistry.registerExtension('WebNotification', 'notification-group-extension', {
  rank: 50,
  name: 'news',
  plugins: [
    'MentionInNewsNotificationPlugin',
    'NewsSpaceWebNotificationPlugin',
    'PostNewsNotificationPlugin',
    'PublishNewsNotificationPlugin'
  ],
  icon: 'fa-newspaper',
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'MentionInNewsNotificationPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-post-news'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'PostNewsNotificationPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-post-news'],
});
extensionRegistry.registerExtension('WebNotification', 'notification-content-extension', {
  type: 'PublishNewsNotificationPlugin',
  rank: 10,
  vueComponent: Vue.options.components['user-notification-post-news'],
});
