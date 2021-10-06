<template>
  <div id="newsAppItem">
    <a
      :href="news.url"
      :style="{ 'background-image': 'url(' + news.illustrationURL + ')' }"
      class="newsSmallIllustration"
      :target="news.target"></a>
    <div class="newsItemContent">
      <div class="newsItemContentHeader">
        <h3>
          <a :href="news.url" :target="news.target">{{ news.title }} </a>
        </h3>
        <news-spaces-shared-in
          v-if="news.activities && news.activities.split(';')[1]"
          :news-id="news.newsId"
          :activities="news.activities" />
        <exo-news-details-action-menu-app
          v-if="!news.schedulePostDate"
          :news="news"
          :show-edit-button="news.canEdit && !isDraftsFilter"
          :show-delete-button="news.canDelete"
          :show-share-button="showShareButton && !isDraftsFilter"
          :show-resume-button="news.draft && isDraftsFilter"
          @delete="deleteConfirmDialog"
          @edit="editLink(news)" />
        <exo-confirm-dialog
          ref="deleteConfirmDialog"
          :message="$t('news.message.confirmDeleteNews')"
          :title="$t('news.title.confirmDeleteNews')"
          :ok-label="$t('news.button.ok')"
          :cancel-label="$t('news.button.cancel')"
          @ok="$emit('delete-news',news)" />
      </div>
      <div class="newsInfo">
        <div class="newsOwner">
          <a :href="news.authorProfileURL">
            <img :src="news.authorAvatarUrl">
            <span>{{ news.authorFullName }}</span>
          </a>
          <i v-if="!news.hiddenSpace" class="uiIconArrowNext"></i>
          <span v-if="!news.hiddenSpace" class="newsSpace">
            <a :href="news.spaceUrl" class="newsSpaceName">
              <img :src="news.spaceAvatarUrl">
              <span>{{ news.spaceDisplayName }}</span>
            </a>
          </span>
        </div>
        <div class="newsDate">
          <i class="uiIconClock"></i>
          <span v-if="news && news.schedulePostDate">
            <date-format
              :value="news.schedulePostDate"
              :format="dateFormat"
              class="newsTime" />
            -
            <date-format
              :value="news.schedulePostDate"
              :format="dateTimeFormat"
              class="newsTime" />
          </span>
          <span v-else>{{ news.updatedDate }}</span>
        </div>
        <div class="newsViews" v-if="!news.draft && !news.scheduled">
          <i class="uiIconWatch"></i>
          <span class="viewsCount">{{ news.viewsCount }}  {{ $t('news.app.views') }}</span>
        </div>
      </div>
      <div class="newsItemContentDetails">
        <a :href="news.url" :target="news.target">
          <p class="newsSummary" v-sanitized-html="news.newsText"></p>
        </a>
        <div class="newsActions" v-if="!news.draft">
          <exo-news-archive
            v-if="news.canArchive"
            :news-id="news.newsId"
            :news-archived="news.archived"
            :news-title="news.title"
            :pinned="news.pinned"
            @refresh-news-list="$emit('update-news-list', false)" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    news: {
      type: Object,
      required: false,
      default: null
    },
    newsFilter: {
      type: String,
      required: false,
      default: null
    },
    newsList: {
      type: Array,
      required: false,
      default: null
    },
  },
  data: () => ({
    showShareButton: true,
    dateTimeFormat: {
      hour: '2-digit',
      minute: '2-digit',
    },
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    },
  }),
  computed: {
    isDraftsFilter() {
      return this.newsFilter === 'drafts';
    },
  },
  methods: {
    editLink(news) {
      const editUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?spaceId=${news.spaceId}&newsId=${news.newsId}&activityId=${news.activityId}`;
      window.open(editUrl, '_blank');
    },
    deleteConfirmDialog() {
      this.$refs.deleteConfirmDialog.open();
    },
  }
};
</script>