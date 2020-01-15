<template>
  <div class="sharedIn">
    <span class="newsSpaceLabel">{{ $t('news.app.sharedIn') }}</span>
    <div class="sharedInSpaces">
      <a v-for="(act,index) in sharedActivities.slice(0, 2)" :key="index" class="space" target="_blank"
         @click="openSharedInSpacesDrawer()">
        <img :src="act.spaceAvatar" :title="act.spaceDisplayName" rel="tooltip" data-placement="top">
      </a>
      <a v-if="activitiesList.length > 2" class="plusSpaces" target="_blank" @click="openSharedInSpacesDrawer()">
        +{{ activitiesList.length - 2 }}
      </a>
    </div>
    <div :class="{open}" class="drawer">
      <div class="header">
        <span>{{ $t('news.app.sharedInSpaces') }}</span>
        <a class="closebtn" href="javascript:void(0)" @click="closeSharedInSpacesDrawer()">×</a>
      </div>
      <div class="content">
        <div class="spacesList">
          <div v-for="(act) in sharedActivities" :key="act" class="spaceSharedIn">
            <span class="space">
              <img :src="act.spaceAvatar" :alt="act.spaceAvatar" class="avatarMini">
              {{ act.spaceDisplayName }}
            </span>
            <a :href="act.activityUrl" type="button" class="btn" target="_blank">{{ $t('news.app.viewArticle') }}</a>
            <br>
          </div>
        </div>
      </div>
      <div class="footer"></div>
    </div>
    <div v-show="open" class="drawer-backdrop" @click="closeSharedInSpacesDrawer()"></div>
  </div>
</template>

<script>
import * as  newsServices from '../../services/newsServices';
export default {
  props:{
    newsId: {
      type: String,
      default: ''
    },
    activities: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      sharedActivities: [],
      sharedInSpacesUpdated: false,
      open: false,
      maxDisplayedSpaces: 2,
      activitiesList: this.activities.split(';').filter((activity,index) => index > 0 && activity)
    };
  },
  watch: {
    activities() {
      this.activitiesList = this.activities.split(';').filter((activity,index) => index > 0 && activity);
      this.updateSharedInSpaces();
    }
  },
  created() {
    let activitiesList = this.activitiesList.slice();
    if (this.activitiesList.length > this.maxDisplayedSpaces){
      activitiesList = activitiesList.slice(0, this.maxDisplayedSpaces);
    }
    activitiesList.forEach(activity => {
      const spaceId = activity.split(':')[0];
      if (spaceId) {
        newsServices.getSpaceById(spaceId).then(space => {
          space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
          this.sharedActivities.push({
            spaceId: spaceId,
            spaceAvatar: space.avatarUrl,
            spaceDisplayName: space.displayName,
            activityUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${activity.split(':')[1]}`
          });
          this.sharedActivities.sort((a, b) => a.spaceDisplayName.toLowerCase().localeCompare(b.spaceDisplayName.toLowerCase()));
        });
      }
    });
  },
  methods: {
    openSharedInSpacesDrawer() {
      if (!this.sharedInSpacesUpdated) {
        this.updateSharedInSpaces();
      }
      this.open = true;
      document.body.style.overflow = 'hidden';
    },
    closeSharedInSpacesDrawer(){
      this.open = false;
      document.body.style.overflow = 'auto';
    },
    updateSharedInSpaces() {
      this.sharedActivities = [];
      newsServices.getNewsSpaces(this.newsId).then(news => {
        this.activitiesList.forEach(activity => {
          const spaceId = activity.split(':')[0];
          if (spaceId) {
            const space = news.sharedInSpacesList.find(space => space.id === spaceId);
            if (space) {
              space.avatarUrl = space.avatarUrl ? space.avatarUrl : '/eXoSkin/skin/images/system/SpaceAvtDefault.png';
              this.sharedActivities.push({
                spaceId: spaceId,
                spaceAvatar: space.avatarUrl,
                spaceDisplayName: space.displayName,
                activityUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${activity.split(':')[1]}`
              });
              this.sharedActivities.sort((a, b) => a.spaceDisplayName.toLowerCase().localeCompare(b.spaceDisplayName.toLowerCase()));
            }
          }
        });
        this.sharedInSpacesUpdated = true;
      });
    }
  }
};
</script>