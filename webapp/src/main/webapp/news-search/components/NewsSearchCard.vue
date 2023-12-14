<template>
  <v-card
    class="d-flex flex-column border-radius box-shadow"
    flat
    min-height="227">
    <div class="d-flex flex-grow-1 px-3 py-3">
      <div
        ref="excerptNode"
        :title="excerptText"
        class="text-wrap me-3 text-break caption search-card-text">
      </div>
      <exo-news-favorite-action
        :news="result"
        :activity-id="result.activityId"
        absolute
        top="0"
        right="0"
        @removed="$emit('refresh-favorite')" />
    </div>
    <v-list class="flex-grow-0 no-border-radius pa-0">
      <v-list-item class="px-3 pt-1 pb-2 ">
        <div class="grey-color">
          <div>
            {{ $t('search.news.card.author') }} :  <a
              :href="posterProfile"
              target="_blank">
              {{ posterFullName }}
            </a>
          </div>
          <div>{{ $t('search.news.card.published') }} : {{ formatDate (this.result.postedTime) }}</div>
        </div>
      </v-list-item>
    </v-list>
    <v-list class="light-grey-background flex-grow-0 border-top-color no-border-radius pa-0">
      <v-list-item :href="this.result.newsUrl" class="px-0 pt-1 pb-2">
        <v-list-item-icon class="mx-0">
          <span class="uiIconActivity uiIconNews tertiary--text ps-2 pe-2"></span>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title>
            {{ this.result.title }}
          </v-list-item-title>
          <v-list-item-subtitle>
            {{ this.result.spaceDisplayName }}
          </v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    maxEllipsisHeight: 90,
    lineHeight: 22,
    profileActionExtensions: [],
    fullDateFormat: {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }
  }),
  computed: {
    poster() {
      return this.result && this.result.poster.profile;
    },
    posterFullName() {
      return this.poster && this.poster.fullname;
    },
    posterProfile() {
      return this.poster && `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.poster.username}`;
    },
    excerpts() {
      return this.result && this.result.excerpts;
    },
    excerptHtml() {
      return this.excerpts && this.excerpts.join('\r\n...');
    },
    excerptText() {
      return $('<div />').html(this.excerptHtml).text();
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
  mounted() {
    this.computeEllipsis();
  },
  methods: {
    computeEllipsis() {
      if ((!this.excerptHtml || this.excerptHtml.length === 0) && (!this.result || !this.result.body || this.result.body.length === 0)) {
        return;
      }
      const excerptParent = this.$refs.excerptNode;
      if (!excerptParent) {
        return;
      }
      excerptParent.innerHTML = this.excerptHtml || this.result.body;

      let charsToDelete = 20;
      let excerptParentHeight = excerptParent.getBoundingClientRect().height || this.lineHeight;
      if (excerptParentHeight > this.maxEllipsisHeight) {
        while (excerptParentHeight > this.maxEllipsisHeight) {
          const newHtml = this.deleteLastChars(excerptParent.innerHTML.replace(/&[a-z]*;/, ''), charsToDelete);
          const oldLength = excerptParent.innerHTML.length;
          excerptParent.innerHTML = newHtml;
          if (excerptParent.innerHTML.length === oldLength) {
            charsToDelete = charsToDelete * 2;
          }
          excerptParentHeight = excerptParent.getBoundingClientRect().height || this.lineHeight;
        }
        excerptParent.innerHTML = this.deleteLastChars(excerptParent.innerHTML, 4);
        excerptParent.innerHTML = `${excerptParent.innerHTML}...`;
      }
    },
    deleteLastChars(html, charsToDelete) {
      if (html.slice(-1) === '>') {
        // Replace empty tags
        html = html.replace(/<[a-zA-Z 0-9 "'=]*><\/[a-zA-Z 0-9]*>$/g, '');
      }
      html = html.replace(/<br>(\.*)$/g, '');

      charsToDelete = charsToDelete || 1;

      let newHtml = '';
      if (html.slice(-1) === '>') {
        // Delete last inner html char
        html = html.replace(/(<br>)*$/g, '');
        newHtml = html.replace(new RegExp(`([^>]{${charsToDelete}})(</)([a-zA-Z 0-9]*)(>)$`), '$2$3');
        newHtml = $('<div />').html(newHtml).html().replace(/&[a-z]*;/, '');
        if (newHtml.length === html.length) {
          newHtml = html.replace(new RegExp('([^>]*)(</)([a-zA-Z 0-9]*)(>)$'), '$2$3');
        }
      } else {
        newHtml = html.substring(0, html.trimRight().length - charsToDelete);
      }
      return newHtml;
    },
    formatDate(time) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(time),this.fullDateFormat, eXo.env.portal.language);
    },
  }
};
</script>