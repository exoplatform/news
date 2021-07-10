<template>
  <v-app class="newsApp">
    <v-toolbar
      color="white"
      flat
      dense>
      <v-row>
        <v-spacer />
        <v-col
          cols="2"
          sm="4"
          class="d-flex flex-row justify-end my-auto flex-nowrap">
          <div :class="searchInputDisplayed ? '' : 'newsAppHideSearchInput'">
            <div class="inputNewsSearchWrapper">
              <v-scale-transition>
                <v-text-field
                  v-model="searchText"
                  :placeholder="$t('news.app.searchPlaceholder')"
                  prepend-inner-icon="fa-filter"
                  class="pa-0 my-auto" />
              </v-scale-transition>
            </div>
          </div>
        </v-col>
        <v-col
          cols="2"
          class="d-flex flex-row justify-end my-auto flex-nowrap">
          <select
            v-model="newsFilter"
            class="width-auto my-auto ms-4 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
            <option value="all">{{ $t('news.app.filter.all') }}</option>
            <option value="pinned">{{ $t('news.app.filter.pinned') }}</option>
            <option value="myPosted">{{ $t('news.app.filter.myPosted') }}</option>
            <option value="archived">{{ $t('news.app.filter.archived') }}</option>
            <option value="drafts">{{ $t('news.app.filter.drafts') }}</option>
            <option value="scheduled">{{ $t('news.app.filter.scheduled') }}</option>
          </select>
          <v-btn
            icon
            class="primary--text ms-2"
            @click="$root.$emit('news-space-selector-drawer-open')">
            <template v-if="spacesFilter && spacesFilter.length && !spacesFilter.includes('-1')">
              <i class="uiIcon uiIcon24x24 settingsIcon primary--text mb-1"></i>
              ({{ spacesFilter.length }})
            </template>
            <template v-else>
              <i class="uiIcon uiIcon24x24 settingsIcon text-color"></i>
            </template>
          </v-btn>
        </v-col>
      </v-row>
    </v-toolbar>
    <div class="newsAppFilterOptions">
      <news-filter-space-drawer
        v-model="spacesFilter" />
    </div>
    <v-app
      class="VuetifyApp">
      <v-progress-circular
        v-if="loadingNews && newsList.length === 0"
        :size="40"
        :width="4"
        indeterminate
        class="loadingRing" />
    </v-app>
    <div
      v-if="newsList.length"
      id="newsListItems"
      class="newsListItems">
      <div
        v-for="(news,index) in newsList"
        :key="news.newsId"
        class="newsItem">
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
            <exo-news-details-action-menu
              v-if="news.canEdit && !news.schedulePostDate"
              :news="news"
              :show-edit-button="news.canEdit && !isDraftsFilter"
              :show-delete-button="news.canDelete"
              :show-share-button="showShareButton && !isDraftsFilter"
              :show-resume-button="news.draft && isDraftsFilter"
              @delete="deleteConfirmDialog(index)"
              @edit="editLink(news)" />
            <exo-confirm-dialog
              ref="deleteConfirmDialog"
              :message="$t('news.message.confirmDeleteNews')"
              :title="$t('news.title.confirmDeleteNews')"
              :ok-label="$t('news.button.ok')"
              :cancel-label="$t('news.button.cancel')"
              @ok="deleteNews(news)" />
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
                @refresh-news-list="fetchNews(false)" />
            </div>
            <!-- The following bloc is needed in order to display the pin confirmation popup when acceding to news details from news app -->
            <!--begin -->
            <div class="uiPopupWrapper UISocialConfirmation" style="display: none;">
              <div class="UIPopupWindow UIDragObject uiPopup " style="width: 550px;">
                <div class="popupHeader clearfix">
                  <a class="uiIconClose pull-right" title="Close"></a>
                  <span class="PopupTitle popupTitle"></span>
                </div>
                <div class="PopupContent popupContent">
                  <ul class="singleMessage popupMessage resizable">
                    <li>
                      <span class="confirmationIcon contentMessage"></span>
                    </li>
                  </ul>
                  <div class="uiAction uiActionBorder"></div>
                </div>
              </div>
            </div>
            <!-- end -->
          </div>
        </div>
      </div>
    </div>
    <div v-if="newsList.length === 0 && !loadingNews" class="articleNotFound">
      <span class="iconNotFound"></span>
      <h3>{{ notFoundMessage }}</h3>
    </div>
    <div v-if="showLoadMoreButton" class="newsListPagination">
      <div class="btn btn-block" @click="loadMore">{{ $t('news.app.loadMore') }}</div>
    </div>
    <news-activity-sharing-spaces-drawer />
  </v-app>
</template>

<script>
export default {
  name: 'NewsApp',
  data() {
    return {
      newsList: [],
      newsPerPage: 10,
      showLoadMoreButton: false,
      errors: [],
      showDropDown: false,
      NEWS_TEXT_MAX_LENGTH: 300,
      searchText: '',
      searchNews: '',
      searchDelay: 300,
      searchInputDisplayed: false,
      newsFilter: 'all',
      spacesFilter: [],
      newsStatusLabel: this.$t('news.app.filter.all'),
      showArchiveButton: true,
      showShareButton: true,
      loadingNews: true,
      initialized: false,
      dateTimeFormat: {
        hour: '2-digit',
        minute: '2-digit',
      },
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      },
    };
  },
  computed: {
    notFoundMessage() {
      if (this.searchText.trim().length) {
        return this.$t('news.app.searchNotFound').replace('{0}', this.searchText);
      } else {
        return this.$t('news.app.noNews');
      }
    },
    isDraftsFilter() {
      return this.newsFilter === 'drafts';
    },
  },
  watch: {
    searchText() {
      if (this.searchText && this.searchText.trim().length) {
        clearTimeout(this.searchNews);
        this.searchNews = setTimeout(() => {
          this.fetchNews(false);
        }, this.searchDelay);
      } else {
        this.fetchNews(false);
      }
    },
    newsFilter() {
      window.history.pushState('', 'News' , this.setQueryParam('filter', this.newsFilter));

      this.newsStatusLabel = this.$t(`news.app.filter.${this.newsFilter}`);

      this.fetchNews(false);
    },
    loadingNews() {
      if (this.loadingNews) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
    initialized() {
      if (this.initialized) {
        this.$root.$emit('application-loaded');
      }
    },
    spacesFilter: {
      handler: function () {
        this.fetchNews(false);
        if (this.spacesFilter === false) {
          this.spacesFilter = ['-1'];
        }
        if (this.spacesFilter.length > 0) {
          window.history.pushState('', 'News', this.setQueryParam('spaces', this.spacesFilter.toString().replace(/,/g, '_')));
        } else {
          window.history.pushState('', 'News', this.removeQueryParam('spaces'));
        }

      },
      deep: true
    }
  },
  created() {
    const filterQueryParam = this.getQueryParam('filter');
    const searchQueryParam = this.getQueryParam('search');
    this.removeQueryParam('spaces');
    if (filterQueryParam || searchQueryParam) {
      if (filterQueryParam) {
        // set filter value, which will trigger news fetching
        this.newsFilter = filterQueryParam;
      }
      if (searchQueryParam) {
        // set search value
        this.searchText = searchQueryParam;
      }
    } else {
      this.fetchNews();
    }
    this.$root.$on('news-shared', (news) => {
      if (news && news.spaceId) {
        this.fetchNews(false);
      }
    });
  },
  methods: {
    getNewsText(newsSummary, newsBody) {
      let text = newsSummary;
      if (!text) {
        text = newsBody;
      }

      text = text.length > this.NEWS_TEXT_MAX_LENGTH ? `${text.substring(0, this.NEWS_TEXT_MAX_LENGTH)}...` : text;

      const div = document.createElement('div');
      div.innerHTML = text;
      text = div.textContent || div.innerText || '';

      return text;
    },
    updateNewsList(data, append = true) {
      const result = [];
      const language = eXo.env.portal.language;
      const local = `${language}-${language.toUpperCase()}`;
      const options = {year: 'numeric', month: 'short', day: 'numeric'};

      data.forEach((item) => {
        const newsPublicationDate = item.publicationDate != null ? new Date(item.publicationDate.time).toLocaleDateString(local, options) : null;
        const newsUpdateDate = new Date(item.updateDate.time).toLocaleDateString(local, options);
        const newsIllustration = item.illustrationURL == null ? '/news/images/newsImageDefault.png' : item.illustrationURL;
        const newsIllustrationUpdatedTime = item.illustrationUpdateDate == null ? '' : item.illustrationUpdateDate.time;
        const activityId = item.activities ? item.activities.split(';')[0].split(':')[1] : '';
        result.push({
          newsId: item.id,
          newsText: this.getNewsText(item.summary, item.body),
          illustrationURL: `${newsIllustration}?${newsIllustrationUpdatedTime}`,
          title: item.title,
          updatedDate: this.isDraftsFilter ? newsPublicationDate : newsUpdateDate,
          spaceDisplayName: item.spaceDisplayName,
          spaceUrl: item.spaceUrl,
          url: this.isDraftsFilter ? `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?spaceId=${item.spaceId}&newsId=${item.id}&activityId=${activityId}` : item.url,
          authorFullName: item.authorDisplayName,
          authorProfileURL: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${item.author}`,
          viewsCount: item.viewsCount == null ? 0 : item.viewsCount,
          activityId: activityId,
          canEdit: item.canEdit,
          canDelete: item.canDelete,
          archived: item.archived,
          draft: item.publicationState === 'draft',
          scheduled: item.publicationState === 'staged',
          schedulePostDate: item.schedulePostDate,
          canArchive: item.canArchive,
          pinned: item.pinned,
          activities: item.activities,
          authorAvatarUrl: item.authorAvatarUrl,
          spaceAvatarUrl: item.spaceAvatarUrl,
          hiddenSpace: item.hiddenSpace,
          spaceId: item.spaceId,
          target: this.newsFilter === 'drafts' ? '_blank' : '_self',
        });
      });
      if (append) {
        this.newsList = this.newsList.concat(result);
      } else {
        this.newsList = result;
      }

      window.require(['SHARED/social-ui-profile'], function (socialProfile) {
        const labels = {
          StatusTitle: 'Loading...',
          Connect: 'Connect',
          Confirm: 'Confirm',
          CancelRequest: 'Cancel Request',
          RemoveConnection: 'Remove Connection',
          Ignore: 'Ignore'
        };
        socialProfile.initUserProfilePopup('newsListItems', labels);
      });
    },
    fetchNews(append = true) {
      this.loadingNews = true;
      const searchTerm = this.searchText.trim().toLowerCase();
      const offset = append ? this.newsList.length : 0;
      return this.$newsServices.getNews(this.newsFilter, this.spacesFilter, searchTerm, offset, this.newsPerPage + 1, false).then(data => {
        if (data.news && data.news.length) {
          if (data.news.length > this.newsPerPage) {
            this.showLoadMoreButton = true;
            data.news.pop();
            this.updateNewsList(data.news, append);
          } else {
            this.showLoadMoreButton = false;
            this.updateNewsList(data.news, append);
          }
        } else if (!append) {
          this.showLoadMoreButton = false;
          this.newsList = [];
        }
        if (searchTerm){
          window.history.pushState('', 'News', this.setQueryParam('search', this.searchText));
        } else {
          window.history.pushState('', 'News', this.removeQueryParam('search'));
        }
        return this.$nextTick();
      }).catch(() => this.loadingNews = false)
        .finally(() => {
          this.loadingNews = false;
          this.initialized = true;
        });
    },
    loadMore: function() {
      this.fetchNews();
    },
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
    setQueryParam(paramName, paramValue) {
      const url = new URL(window.location);
      url.searchParams.set(paramName, paramValue);
      return url.href;
    },
    removeQueryParam(paramName) {
      const url = new URL(window.location);
      url.searchParams.delete(paramName);
      return url.href;
    },
    editLink(news) {
      const editUrl = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor?spaceId=${news.spaceId}&newsId=${news.newsId}&activityId=${news.activityId}`;
      window.open(editUrl, '_blank');
    },
    deleteConfirmDialog(index) {
      this.$refs.deleteConfirmDialog[index].open();
    },
    deleteNews(news) {
      const deleteDelay = 6;
      const redirectionTime = 6100;
      this.$newsServices.deleteNews(news.newsId, this.newsFilter === 'drafts', deleteDelay)
        .then(() => {
          this.$root.$emit('confirm-news-deletion', news);
        });
      setTimeout(() => {
        const deletedNews = localStorage.getItem('deletedNews');
        if (deletedNews != null) {
          this.fetchNews(false);
        }
      }, redirectionTime);
    }
  },
};
</script>
