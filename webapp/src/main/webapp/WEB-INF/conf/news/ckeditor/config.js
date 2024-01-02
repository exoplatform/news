// force env when using the eXo Android app (the eXo Android app uses a custom user agent which
// is not known by CKEditor and which makes it not initialize the editor)
const oldEditorConfigFn = CKEDITOR.editorConfig;
CKEDITOR.editorConfig = function (config) {

  oldEditorConfigFn(config);


  // style inside the editor
  config.contentsCss = [];
  document.querySelectorAll('[skin-type=portal-skin]').forEach(link => config.contentsCss.push(link.href));
  config.contentsCss.push('/news/ckeditorCustom/contents.css'); // load last

  CKEDITOR.plugins.addExternal('switchView','/news/js/ckeditor/plugins/switchView/','plugin.js');
  CKEDITOR.plugins.addExternal('attachFile','/news/js/ckeditor/plugins/attachment/','plugin.js');

  const toolbar = [
    {name: 'accessibility', items: ['A11ychecker']},
    {name: 'format', items: ['Format']},
    {name: 'fontSize', items: ['FontSize']},
    {
      name: 'basicstyles',
      groups: ['basicstyles', 'cleanup'],
      items: ['Bold', 'Italic', 'Underline', 'Strike', 'TextColor', 'RemoveFormat', 'CopyFormatting']
    },
    {
      name: 'paragraph',
      groups: ['align', 'list', 'indent'],
      items: ['JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', 'NumberedList', 'BulletedList', 'Outdent', 'Indent'],
    },
    {name: 'links', items: ['Link', 'Anchor']},
    {
      name: 'blocks',
      items: ['Blockquote', 'tagSuggester', 'emoji', 'selectImage', 'Table', 'EmbedSemantic', 'CodeSnippet', 'InsertOptions']
    },
  ];

  const mobileToolbar = [
    {name: 'switchView', items: ['switchView']},
    {
      name: 'basicstyles',
      groups: ['basicstyles', 'cleanup'],
      items: ['Bold', 'Italic', 'TextColor', 'RemoveFormat', 'CopyFormatting']
    },
    {
      name: 'paragraph',
      groups: ['list', 'indent'],
      items: ['NumberedList', 'BulletedList', 'Outdent', 'Indent'],
    },
    {name: 'links', items: ['Link', 'Anchor']},
    {
      name: 'blocks',
      items: ['Blockquote', 'tagSuggester', 'emoji', 'selectImage', 'Table', 'EmbedSemantic', 'InsertOptions']
    },
    {name: 'attachFile', items: ['attachFile']},
  ];

  let extraPlugins = 'a11ychecker,balloonpanel,indent,switchView,attachFile,googleDocPastePlugin,copyformatting,indentblock,indentlist,sharedspace,' +
        'autolink,colordialog,tagSuggester,emoji,link,font,justify,widget,contextmenu,table,codesnippet,tabletools,tableresize,embedsemantic,suggester';

  let removePlugins = 'image,confirmBeforeReload,maximize,resize,autoembed';

  require(['SHARED/extensionRegistry'], function (extensionRegistry) {
    const ckEditorExtensions = extensionRegistry.loadExtensions('WYSIWYGPlugins', 'image');
    if (ckEditorExtensions?.length) {
      const ckEditorExtraPlugins = ckEditorExtensions.map(ckEditorExtension => ckEditorExtension.extraPlugin).join(',');
      const ckEditorRemovePlugins = ckEditorExtensions.map(ckEditorExtension => ckEditorExtension.removePlugin).join(',');
      if (ckEditorExtraPlugins) {
        extraPlugins = `${extraPlugins},${ckEditorExtraPlugins}`;
      }
      if (ckEditorRemovePlugins) {
        removePlugins = `${extraPlugins},${ckEditorRemovePlugins}`;
      }
    }
  });

  config.extraPlugins = extraPlugins;
  config.removePlugins = removePlugins;
  if (window.innerWidth < 960) {
    config.toolbar = mobileToolbar;
  } else {
    config.toolbar = toolbar;
  }
  config.toolbarGroups = [
    { name: 'accessibility'},
    { name: 'format'},
    { name: 'fontSize'},
    { name: 'basicstyles', groups: [ 'basicstyles', 'cleanup' ] },
    { name: 'paragraph', groups: ['align', 'list', 'indent', ] },
    { name: 'links'},
    { name: 'blocks'},
  ];

  config.autoGrow_minHeight = 250;
  config.height = 250;
  config.format_tags = 'p;h1;h2;h3';
};