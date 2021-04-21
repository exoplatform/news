<template>
  <v-menu
    v-model="actionMenu"
    eager
    bottom
    left
    offset-y>
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        class="newsDetailsActionMenu"
        icon
        v-on="on">
        <v-icon>mdi-dots-vertical</v-icon>
      </v-btn>
    </template>

    <v-list>
      <v-list-item v-if="showPinButton" @click="$emit('pin')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.pin') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showDeleteButton" @click="$emit('delete')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.delete') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showShareButton" @click="$root.$emit('news-share-drawer-open', news)">
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
    showShareButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showPinButton: {
      type: Boolean,
      required: false,
      default: true
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: true
    },
  },
  data: () => ({
    actionMenu: null,
  }),
  mounted() {
    $('#singlePageApplicationContainer').parent().click(() => {
      this.actionMenu = false;
    });
  },
};
</script>

