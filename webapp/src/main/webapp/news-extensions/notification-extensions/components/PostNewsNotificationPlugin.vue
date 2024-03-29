<!--
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
-->
<template>
  <user-notification-template
    :notification="notification"
    :avatar-url="avatarUrl"
    :message="message"
    :loading="loading"
    :url="url"
    user-avatar>
    <template #actions>
      <div class="text-truncate">
        <v-icon size="14" class="me-1 mb-1">fa-newspaper</v-icon>
        {{ eventTitle }}
      </div>
    </template>
  </user-notification-template>
</template>
<script>
export default {
  props: {
    notification: {
      type: Object,
      default: null,
    },
  },
  computed: {
    url() {
      return this.notification?.space?.isMember ? this.notification?.parameters?.ACTIVITY_LINK
        : `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/news/detail?newsId=${this.notification?.parameters?.NEWS_ID}&type=article`;
    },
    eventTitle() {
      return this.notification?.parameters?.CONTENT_TITLE;
    },
    avatarUrl() {
      return this.notification?.parameters?.AUTHOR_AVATAR_URL;
    },
    message() {
      let message;
      const creator = this.notification?.parameters?.CONTENT_AUTHOR;
      const currentUser = this.notification?.parameters?.CURRENT_USER;
      const title = this.eventTitle;
      const space = this.notification?.parameters?.CONTENT_SPACE;
      switch (this.notification?.parameters?.CONTEXT) {
      case 'POST NEWS':
        message =  this.$t('news.notification.description', {
          0: `<a class="user-name font-weight-bold">${creator}</a>`,
          1: title,
          2: `<a class="space-name font-weight-bold">${space}</a>`
        });
        break;
      case 'MENTION IN NEWS':
        message =  this.$t('news.notification.description.mention.in.news', {
          0: title
        });
        break;
      case 'PUBLISH NEWS':
        message =  this.$t('news.notification.description.publish.news', {
          0: `<a class="user-name font-weight-bold">${currentUser}</a>`,
          1: title
        });
        break;
      default:
        break;
      }
      return message;
    }
  }
};
</script>
