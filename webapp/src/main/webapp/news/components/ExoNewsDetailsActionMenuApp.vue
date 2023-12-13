<template>
  <v-menu
    v-model="actionMenu"
    attach
    eager
    bottom
    left
    offset-y
    min-width="108px"
    class="px-0 mx-2">
    <template #activator="{ on, attrs }">
      <v-btn
        v-bind="attrs"
        :title="$t('news.details.menu.open')"
        class="newsDetailsActionMenu pull-right"
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
      <v-list-item v-if="showShareButton && news.activityId" @click="$root.$emit('activity-share-drawer-open', news.activityId, 'newsApp')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.share') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showResumeButton" @click="$emit('edit')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.resume') }}
        </v-list-item-title>
      </v-list-item>
      <v-list-item v-if="showDeleteButton" @click="$emit('delete')">
        <v-list-item-title>
          {{ $t('news.details.header.menu.delete') }}
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
      default: false
    },
    showEditButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showResumeButton: {
      type: Boolean,
      required: false,
      default: false
    },
    showDeleteButton: {
      type: Boolean,
      required: false,
      default: false
    },
  },
  data: () => ({
    actionMenu: null,
  }),
  created() {
    $(document).mousedown(() => {
      if (this.actionMenu) {
        window.setTimeout(() => {
          this.actionMenu = false;
        }, 200);
      }
    });
  },
};
</script>