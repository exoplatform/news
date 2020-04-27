(function (extensionRegistry) {
  const newsActivityComposerPlugin = {
    key: 'news',
    rank: 30,
    resourceBundle: 'locale.portlet.news.News',
    labelKey: 'news.composer.write',
    description: 'news.composer.write.description',
    iconClass: 'newsComposerIcon',
    enabled: function() {
      return eXo.env.portal.spaceId != null && eXo.env.portal.spaceId.length !== 0;
    },
    onExecute: function() {
      let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
      if(eXo.env.portal.spaceId) {
        url += `?spaceId=${eXo.env.portal.spaceId}`
      }

      const editor = CKEDITOR.instances['activityContent'];
      if(editor) {
        const message = CKEDITOR.instances['activityContent'].getData();
        localStorage.setItem('exo-activity-composer-message', message);
      }

      window.open(url,'_blank');
    }
  };

  const hintActivityComposerPlugin = {
    key: 'hint',
    rank: 10,
    resourceBundle: 'locale.portlet.news.News',
    labelKey: 'news.composer.switch.article',

    onExecute: function() {
      let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
      if(eXo.env.portal.spaceId) {
        url += `?spaceId=${eXo.env.portal.spaceId}`
      }

      const editor = CKEDITOR.instances['activityContent'];
      if(editor) {
        const message = CKEDITOR.instances['activityContent'].getData();
        localStorage.setItem('exo-activity-composer-message', message);
      }

      window.open(url,'_blank');
    }
  }

  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-action', newsActivityComposerPlugin);
  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-hint-action', hintActivityComposerPlugin);
})(extensionRegistry);