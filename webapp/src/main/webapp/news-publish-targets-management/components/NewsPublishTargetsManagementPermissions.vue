/*
 * Copyright (C) 2022 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <gnu.org/licenses>.
 */
<template>
  <div class="d-flex">
    <v-chip
      class="identitySuggesterItem mt-2"
      close
      @click:close="$emit('remove-permission', permission)">
      <v-icon
        v-if="permission.providerId ==='group'"
        size="19"
        color="white"
        class="fas fa-users" />
      <v-avatar
        v-else
        size="32"
        class="ma-0">
        <img
          :src="avatarUrl"
          :alt="displayName"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div v-if="displayName || $slots.subTitle" class="ms-2 overflow-hidden">
        <p
          v-if="displayName"
          class="text-truncate subtitle-2  text-left mb-0 font-weight-bold">
          {{ displayName }}
        </p>
      </div>
    </v-chip>
  </div>
</template>

<script>

export default {
  props: {
    permission: {
      type: Object,
      default: () => ({}),
    },
  },
  computed: {
    avatarUrl() {
      const profile = this.permission && (this.permission.profile || this.permission.space);
      return profile && (profile.avatarUrl || profile.avatar) || this.permission  && this.permission.avatar ;
    },
    displayName() {
      const profile = this.permission && (this.permission.profile || this.permission.space);
      return profile && (profile.displayName || profile.fullName && profile.fullName.substring(0,profile.fullName.lastIndexOf(' ('))) || this.permission &&  (this.permission.name || this.permission.displayName);
    },
  }
};
</script>