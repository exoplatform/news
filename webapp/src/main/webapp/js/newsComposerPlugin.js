const newsActivityComposerPlugin = {
  key: 'news',
  rank: 30,
  resourceBundle: 'locale.portlet.news.News',
  labelKey: 'news.composer.write',
  description: 'news.composer.write.description',
  iconClass: 'newsComposerIcon',
  enabled: function() {
    let isRedactor = false;
    isRedactor = getSpaceMemberships(`/spaces/${eXo.env.portal.spaceGroup}`);
    return eXo.env.portal.spaceId != null && eXo.env.portal.spaceId.length !== 0 && isRedactor;

  },
  onExecute: function() {
    let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
    if(eXo.env.portal.spaceId) {
      url += `?spaceId=${eXo.env.portal.spaceId}`;
    }

    const editor = CKEDITOR.instances['activityContent'];
    if(editor) {
      const message = CKEDITOR.instances['activityContent'].getData();
      localStorage.setItem('exo-activity-composer-message', message);
    }

    window.open(url,'_blank');
  }
};

const switchToArticleActivityComposerPlugin = {
  key: 'switchToArticle',
  rank: 10,
  resourceBundle: 'locale.portlet.news.News',
  labelKey: 'news.composer.switch.article',

  onExecute: function(attachments) {
    let url = `${eXo.env.portal.context}/${eXo.env.portal.portalName}/news/editor`;
    if(eXo.env.portal.spaceId) {
      url += `?spaceId=${eXo.env.portal.spaceId}`;
    }

    const editor = CKEDITOR.instances['activityContent'];
    if(editor) {
      const message = CKEDITOR.instances['activityContent'].getData();
      localStorage.setItem('exo-activity-composer-message', message);
      localStorage.setItem('exo-activity-composer-attachments', JSON.stringify(attachments));
    }

    window.open(url,'_blank');
  }
};
require(['SHARED/extensionRegistry'], function (extensionRegistry) {
  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-action', newsActivityComposerPlugin);
  extensionRegistry.registerExtension('ActivityComposer', 'activity-composer-hint-action', switchToArticleActivityComposerPlugin);
});

function getSpaceMemberships(spaceGroupId) {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?groupId=${spaceGroupId}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then((resp) => resp.json()).then(memberships => {
    const redactors = memberships.entities.filter(membership => {
      return membership.membershipType === 'redactor';
    });
    const spaceHasRedactors = !!redactors;
    const isCurrentUserRedactor = redactors.some(membership => membership.userName === eXo.env.portal.userName);
    return !spaceHasRedactors || isCurrentUserRedactor;
  });
}
