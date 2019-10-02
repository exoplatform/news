<template>
  <div id="newsDetails">
    <div class="newsDetails-description">
      <div class="newsDetails-header">
        <exo-news-share-activity v-if="showShareButton" :activity-id="activityId" :news-id="newsId" :news-title="news.title"></exo-news-share-activity>
        <exo-news-activity-edit-composer v-if="showEditButton" :activity-id="activityId" :show-pin-input="showPinInput"></exo-news-activity-edit-composer>
        <exo-news-pin-activity v-if="showPinInput" :news-id="newsId"></exo-news-pin-activity>
        <div class="newsDetails">
          <img :src="news.illustrationURL" class="newsImage illustrationPicture" alt="News"/>

          <div class="news-top-information">
            <div class="news-header-content">
              <div id="titleNews" class="newsTitle">
                <a :href="news.titleLink" class="activityLinkColor newsTitleLink">{{ news.title }}</a>
              </div>
              <div id="informationNews" class="newsInformation">
                <div class="newsAuthor">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.postedBy') }} :</span>
                    <a :href="news.authorProfileURL" class="newsInformationValue newsAuthorName"> {{ news.authorFullName }}</a>
                  </div>
                  <div>
                    <span class="newsInformationLabel"> {{ $t('news.activity.publicationDate') }} :</span>
                    <span class="newsInformationValue newsPostedDate"> {{ news.postedDate }}</span>
                  </div>
                </div>
                <div v-if="showUpdateInfo" class="newsUpdater">
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdatedBy') }} :</span>
                    <a :href="news.updaterProfileURL" class="newsInformationValue newsUpdaterName">{{ news.updaterFullName }}</a>
                  </div>
                  <div>
                    <span class="newsInformationLabel">{{ $t('news.activity.lastUpdatedDate') }} :</span>
                    <span class="newsInformationValue newsUpdatedDate">{{ news.updatedDate }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-if="news.summary" id="newsSummary" class="summary">
            <span>{{ news.summary }}</span>
          </div>

          <div id="newsBody" class="newsBody fullDetailsBody clearfix">
            <span v-html="news.body"></span>
          </div>
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
      default: function() { return new Object(); }
    },
    newsId: {
      type: String,
      required: false,
      default: null
    },
    activityId: {
      type: String,
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
    }
  },
  data() {
    return {
      showUpdateInfo: this.news.postedDate !== this.news.updatedDate,
    };
  },
  created() {
    if(this.showPinInput) {
      document.querySelector('#pinNewsActivity').style.display = '';
    }
  }
};
</script>
