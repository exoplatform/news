(function () {
  var newsComposerIntegration = {
    init: function(spaceId, labels) {
      const newsEditorLinkContainer = document.querySelector('.uiComposer .share-buttons-down');
      if(newsEditorLinkContainer) {
        let newsEditorLink = newsEditorLinkContainer.querySelector('.newsEditorComposerLink');
        if(!newsEditorLink) {
          newsEditorLink = document.createElement('a');
          newsEditorLink.setAttribute('class', 'newsEditorComposerLink');
          let url = '/portal/intranet/news/editor';
          if(spaceId) {
            url += '?spaceId=' + spaceId;
          }
          newsEditorLink.setAttribute('href', url);
          newsEditorLink.setAttribute('target', '_blank');
          newsEditorLink.appendChild(document.createTextNode(labels.writeLinkLabel));
          newsEditorLinkContainer.appendChild(newsEditorLink);
        }
      }
    }
  }

  return newsComposerIntegration;
})();