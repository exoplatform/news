(function ($) {
  var UINewsActivity = {
    onLoad: function (params) {
      UINewsActivity.configure(params);
      UINewsActivity.init();
    },
    configure: function(params) {
      UINewsActivity.activityId = params.activityId || null;
      UINewsActivity.spaceURL = params.spaceURL;
      UINewsActivity.spaceGroupId = params.spaceGroupId;
      UINewsActivity.nodeNewsId = params.nodeNewsId;
      if (UINewsActivity.activityId == null) {
        return;
      }

    },
    
    init: function() {
      $('.newsBody a').not(".readMore a").attr('target', '_blank');
    },

    clickOnNews: function (newsId) {
      const env = eXo.social.portal;
      const baseRestUrl = `${env.context  }/${  env.rest  }/v1/news/${newsId}/click`;
      $(`#seeMoreId${newsId}`).click(function () {
        $.ajax({
          url: baseRestUrl,
          type: 'POST',
          data: 'readMore',
          cache: false,
        });
      });
      $(`#titleId${newsId}`).click(function () {
        $.ajax({
          url: baseRestUrl,
          type: 'POST',
          data: 'title',
          cache: false,
        });
      });

    },
  };

  return UINewsActivity;
})($);
