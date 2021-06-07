import {canUserCreateNews} from '../services/newsServices.js';

export function initExtensions() {
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

  const switchToArticleActivityComposerPlugin = Object.assign(newsActivityComposerPlugin, {
    key: 'switchToArticle',
    rank: 10,
    labelKey: 'news.composer.switch.article'
  });

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

  const newsActivityTypeExtensionOptions = {
    init: null,
    canEdit: () => false,
    getBody: activity => activity && activity.templateParams && activity.templateParams.comment || '',
  };

  extensionRegistry.registerExtension('activity', 'type', {
    type: 'news',
    options: newsActivityTypeExtensionOptions,
  });

  extensionRegistry.registerExtension('activity', 'type', {
    type: 'shared_news',
    options: newsActivityTypeExtensionOptions,
  });
}
