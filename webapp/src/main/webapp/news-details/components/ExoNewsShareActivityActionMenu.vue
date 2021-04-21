<template>
  <v-menu
    v-model="eventMenu"
    eager
    bottom
    left
    offset-y>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        class="shareNewsActionMenu"
        icon
        v-on="on">
        <v-icon>mdi-dots-vertical</v-icon>
      </v-btn>
    </template>

    <v-list>
      <v-list-item v-if="showPinInput" @click="$emit('pin')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.pin') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showEditButton" @click="$emit('delete')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.delete') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showShareButton" @click="$root.$emit('news-share-drawer-open',news)">
        <v-list-item-title>
          {{ $t('news.details.header.menu.share') }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: null
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showShareButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showPinInput: {
      type: Boolean,
      required: false,
      default: true
    },
    newsPinned: {
      type: Boolean,
      required: true,
      default: false
    },
    newsArchived: {
      type: Boolean,
      required: true,
      default: false
    },
  },
  data: () => ({
    eventMenu: null,
  }),
  mounted() {
    $('#singlePageApplicationContainer').parent().click(() => {
      this.eventMenu = false;
    });
  },
};
</script>

