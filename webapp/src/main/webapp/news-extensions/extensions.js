import {canUserCreateNews} from '../services/newsServices.js';

const newsActivityComposerPlugin = {
  key: 'news',
  rank: 30,
  resourceBundle: 'locale.portlet.news.News',
  labelKey: 'news.composer.write',
  description: 'news.composer.write.description',
  iconClass: 'newsComposerIcon',
  enabled: false,
  onExecute: function (attachments) {
    let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
    if (eXo.env.portal.spaceId) {
      url += `?spaceId=${eXo.env.portal.spaceId}`;
    }
    const editor = CKEDITOR.instances['activityContent'];
    if (editor) {
      const message = CKEDITOR.instances['activityContent'].getData();
      localStorage.setItem('exo-activity-composer-message', message);
      localStorage.setItem('exo-activity-composer-attachments', JSON.stringify(attachments));
    }
    window.open(url, '_blank');
  }
};

const switchToArticleActivityComposerPlugin = Object.assign({}, newsActivityComposerPlugin, {
  key: 'switchToArticle',
  rank: 10,
  labelKey: 'news.composer.switch.article'
});

const newsActivityTypeExtensionOptions = {
  getExtendedComponent: (activity, isActivityDetail) => {
    if (activity && isActivityDetail) {
      return {
        component: Vue.options.components['exo-news-details-activity'],
        overrideHeader: true,
        overrideFooter: false,
      };
    }
  },
  extendSharedActivity: (activity, isActivityDetail) => isActivityDetail,
  showSharedInformationFooter: (activity, isActivityDetail) => isActivityDetail,
  init: (activity, isActivityDetail) => {
    if (!isActivityDetail) {
      let activityId = activity.id;
      if (activity.parentActivity) {
        activityId = activity.parentActivity.id;
      }
      return Vue.prototype.$newsServices.getNewsByActivityId(activityId)
        .then(news => activity.news = news);
    }
  },
  canEdit: () => false,
  canShare: () => true,
  supportsThumbnail: true,
  getThumbnail: (activity) => activity && activity.news && activity.news.illustrationURL || '/news/images/news.png',
  getThumbnailProperties: (activity) => !(activity && activity.news && activity.news.illustrationURL) && {
    height: '90px',
    width: '90px',
    noBorder: true,
  } || null,
  isUseSameViewForMobile: (activity) => !activity || !activity.news || !activity.news.illustrationURL,
  getTitle: (activity) => {
    const news = activity && activity.news;
    if (news && news.title) {
      return news.title;
    }
    return '';
  },
  getSourceLink: (activity) => `${eXo.env.portal.context}/${eXo.env.portal.portalName}/activity?id=${activity.id}`,
  getSummary: (activity) => {
    const news = activity && activity.news;
    if (news && news.summary) {
      return news.summary;
    } else if (news && news.body) {
      return Vue.prototype.$utils.htmlToText(news.body);
    }
    return '';
  },
  getTooltip: (activity, isActivityDetail) => !isActivityDetail && activity && 'news.activity.clickToShowDetail',
};

const lang = eXo.env.portal.language || 'en';
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.news.News-${lang}.json`;

exoi18n.loadLanguageAsync(lang, url).then(i18n => new Vue({i18n}));

export function initExtensions() {
  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-hint-action', switchToArticleActivityComposerPlugin);
  document.dispatchEvent(new CustomEvent('activity-composer-extension-updated'));

  if (eXo.env.portal.spaceId) {
    canUserCreateNews(eXo.env.portal.spaceId)
      .then(canCreateNews => {
        switchToArticleActivityComposerPlugin.enabled = newsActivityComposerPlugin.enabled = eXo.env.portal.spaceId && canCreateNews;
        extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-action', newsActivityComposerPlugin);
        document.dispatchEvent(new CustomEvent('activity-composer-extension-updated'));
      });
  } else {
    switchToArticleActivityComposerPlugin.enabled = newsActivityComposerPlugin.enabled = false;
  }

  extensionRegistry.registerExtension('activity', 'type', {
    type: 'news',
    options: newsActivityTypeExtensionOptions,
  });

}
