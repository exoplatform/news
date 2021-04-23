<template>
  <v-menu
    v-model="actionMenu"
    eager
    bottom
    left
    offset-y
    min-width="108px" class="px-0 text-right mx-2">
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
      <v-list-item v-if="showEditButton" @click="$emit('edit')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.edit') }}
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
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data: () => ({
    actionMenu: null,
  }),
  mounted() {
    $('#UIPortalApplication').parent().click(() => {
      this.actionMenu = false;
    });
  },
};
</script>

