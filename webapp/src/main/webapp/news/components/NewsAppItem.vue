<!--
Copyright (C) 2021 eXo Platform SAS.

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
          :message="confirmDeleteNewsDialogMessage"
          :title="confirmDeleteNewsDialogTitle"
          :ok-label="$t('news.button.ok')"
          :cancel-label="$t('news.button.cancel')"
          @ok="$emit('delete-news',news)" />
      </div>
      <div class="newsInfo d-flex pb-1">
        <div class="newsOwner d-flex align-center pe-4">
          <exo-user-avatar
            :profile-id="newsAuthor"
            :size="25"
            class="align-center width-full my-auto text-truncate flex-grow-0 flex"
            small-font-size
            popover />
          <i v-if="!news.hiddenSpace" class="uiIconArrowNext pt-1"></i>
          <exo-space-avatar
            v-if="!news.hiddenSpace"
            :space-id="spaceId"
            class="width-full text-truncate"
            :size="25"
            extra-class="ps-1"
            small-font-size
            popover />
        </div>
        <div class="newsDate pe-4" v-if="news.activityId">
          <i v-if="displayClock" class="uiIconClock"></i>
          <span v-if="news && news.schedulePostDate">
            <date-format
              :value="news.schedulePostDate"
              :format="dateFormat"
              class="newsTime caption" />
            -
            <date-format
              :value="news.schedulePostDate"
              :format="dateTimeFormat"
              class="newsTime caption" />
          </span>
          <span v-else>
            <date-format
              :value="news.updatedDate"
              :format="dateFormat"
              class="newsTime caption" />
          </span>
        </div>
        <div class="newsViews " v-if="!news.scheduled && news.activityId">
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
            :published="news.published"
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
    confirmDeleteNewsDialogMessage() {
      return this.isDraftsFilter ? this.$t('news.message.confirmDeleteDraftNews') : this.$t('news.message.confirmDeleteNews');
    },
    confirmDeleteNewsDialogTitle() {
      return this.isDraftsFilter ? this.$t('news.title.confirmDeleteDraftNews') : this.$t('news.title.confirmDeleteNews');
    },
    displayClock() {
      return this.news && (this.news.schedulePostDate || this.news.updatedDate);
    },
    draftNews() {
      return this.news && this.news.draft;
    },
    spaceId() {
      return this.news && this.news.spaceId;
    },
    newsAuthor() {
      return this.news && this.news.authorProfileURL && this.news.authorProfileURL.split('/').pop();
    }
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
