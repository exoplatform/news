<template>
  <div class="newsApp">
    <div class="newsAppToolBar">
      <div class="newsAppToolbarLeft">
        <h3 class="newsAppToolBarTitle">
          {{ $t('news.app.title') }}
        </h3>
        <div class="inputNewsSearchWrapper">
          <i class="uiIconSearchSpaces"></i>
          <input
            id="searchInput"
            v-model="searchText"
            :placeholder="$t('news.app.searchPlaceholder')"
            type="text"
            class="searchNewsInput mr-3">
        </div>
      </div>
      <div class="newsAppToolbarRight">
        <div class="newsTypes">
          <div class="btn-group newsTypesSelectBox">
            <button class="btn dropdown-toggle" data-toggle="dropdown">{{ newsStatusLabel }}
              <i class="uiIconMiniArrowDown uiIconLightGray"></i>
            </button>
            <ul class="dropdown-menu">
              <li><a @click="newsFilter = 'all'">{{ $t('news.app.filter.all') }}</a></li>
              <li><a @click="newsFilter = 'pinned'">{{ $t('news.app.filter.pinned') }}</a></li>
            </ul>
          </div>
        </div>
        <div class="newsAppFilterOptions">
          <news-app-filter :search-text="searchText" :news-filter="newsFilter" @newsFilteredBySpaces="onFilterApplied"></news-app-filter>
        </div>
      </div>
    </div>
    <div v-if="newsList.length" id="newsListItems" class="newsListItems">
      <div v-for="news in newsList" :key="news.newsId" class="newsItem">
        <a :href="news.newsUrl" :style="{ 'background-image': 'url(' + news.newsIllustration + ')' }" class="newsItemIllustration"></a>
        <div class="newsItemContent">
          <div class="newsItemContentHeader">
            <h3>
              <a :href="news.newsUrl">{{ news.newsTitle }} </a>
            </h3>
            <div class="newsSpace">
              <span class="newsSpaceLabel">Posted in </span>
              <a :href="news.spaceUrl" class="newsSpaceName" target="_blank">{{ news.spaceDisplayName }}</a>
            </div>
          </div>
          <div class="newsItemContentDetails">
            <a :href="news.newsUrl">
              <p class="newsSummary" v-html="news.newsText"></p>
            </a>
            <div class="newsItemInfo">
              <div class="newsLeftInfo">
                <p class="newsOwner">
                  <a :href="news.profileURL" target="_blank">
                    <img :src="news.avatar">
                    <span>{{ news.author }}</span>
                  </a>
                </p>
                <p class="newsDate">
                  <i class="iconPLFCalendar"></i>
                  <span>{{ news.creationDate }}</span>
                </p>
                <p class="newsViews">
                  <i class="uiIconWatch"></i>
                  <span class="viewsText">{{ news.viewsCount }}  {{ $t('news.app.views') }}</span>
                </p>
              </div>
              <div class="share pull-right">
                <exo-news-share-activity :activity-id="news.activityId" :news-id="news.newsId" :news-title="news.newsTitle">
                </exo-news-share-activity>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="articleNotFound">
      <span class="iconNotFound"></span>
      <h3>{{ notFoundMessage }}</h3>
    </div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';
export default {
  name: 'NewsApp',
  data() {
    return {
      newsList: [],
      errors: [],
      showDropDown: false,
      NEWS_TEXT_MAX_LENGTH: 300,
      searchText: '',
      searchNews: '',
      searchDelay: 300,
      notFoundMessage: this.$t('news.app.noNews'),
      newsFilter: 'all',
      newsSpaces: [],
      spacesFilter: [],
      newsStatusLabel: this.$t('news.app.filter.all')
    };
  },
  watch: {
    searchText() {
      if (this.searchText && this.searchText.trim().length) {
        clearTimeout(this.searchNews);
        this.searchNews = setTimeout(() => {
          this.fetchNews();
        }, this.searchDelay);
      } else {
        if (this.newsFilter === 'pinned') {
          this.getPinnedNews();
        } else {
          this.getNewsList();
        }
      }
    },
    newsFilter() {
      window.history.pushState('', 'News' , this.setQueryParam('filter', this.newsFilter));

      this.newsSpaces = [];
      this.newsStatusLabel = this.newsFilter === 'pinned' ? this.$t('news.app.filter.pinned') : this.$t('news.app.filter.all');

      if (this.searchText && this.searchText.trim().length) {
        this.fetchNews();
      } else {
        if (this.newsFilter === 'pinned') {
          this.getPinnedNews();
        } else {
          this.getNewsList();
        }
      }
    }
  },
  created() {
    const filterQueryParam = this.getQueryParam('filter');
    if(filterQueryParam) {
      // set filter value, which will trigger news fetching
      this.newsFilter = filterQueryParam;
    } else {
      this.getNewsList();
    }
  },
  methods: {
    getNewsList() {
      newsServices.getNews(this.spacesFilter, '' , '')
        .then((data) => {
          this.initNewsList(data);
        }).catch(e =>
          this.errors.push(e)
        );
    },
    getPinnedNews() {
      newsServices.getFilteredNews('pinned', this.spacesFilter)
        .then((data) => {
          this.initNewsList(data);
        }).catch(e =>
          this.errors.push(e)
        );
    },
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
    initNewsList(data) {
      const result = [];
      const language = eXo.env.portal.language;
      const local = `${language}-${language.toUpperCase()}`;
      const options = {year: 'numeric', month: 'short', day: 'numeric'};

      data.forEach((item) => {
        let newsUrl = '';
        const newsCreatedDate = new Date(item.creationDate.time).toLocaleDateString(local, options);
        newsUrl = `${newsUrl}${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/detail?content-id=${encodeURI(item.path)}`;
        const newsIllustration = item.illustrationURL == null ? '/news/images/newsImageDefault.png' : item.illustrationURL;
        const newsIllustrationUpdatedTime = item.illustrationUpdateDate == null ? '' : item.illustrationUpdateDate.time;
        result.push({
          newsId: item.id,
          newsText: this.getNewsText(item.summary, item.body),
          newsIllustration: `${newsIllustration}?${newsIllustrationUpdatedTime}`,
          newsTitle: item.title,
          creationDate: newsCreatedDate,
          spaceDisplayName: item.spaceDisplayName,
          spaceUrl: item.spaceUrl,
          newsUrl: newsUrl,
          author: item.authorDisplayName,
          avatar: `/portal/rest/v1/social/users/${item.author}/avatar`,
          profileURL: `/portal/intranet/profile/${item.author}`,
          viewsCount: item.viewsCount == null ? 0 : item.viewsCount,
        });
      });
      this.newsList = result;
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
    fetchNews() {
      const searchTerm = this.searchText.trim().toLowerCase();
      newsServices.searchNews(searchTerm, this.newsFilter, this.spacesFilter).then(data => {
        if (data.length) {
          this.initNewsList(data);
        } else {
          this.newsList = [];
          this.notFoundMessage = this.$t('news.app.searchNotFound').replace('{0}', this.searchText);
        }
      });
    },
    onFilterApplied: function(newsList, selectedSpaces) {
      this.spacesFilter = selectedSpaces;
      this.initNewsList(newsList);
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
    }
  }
};
</script>
