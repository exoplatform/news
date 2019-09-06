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
      const activityId = UINewsActivity.activityId;
      const actionPin = $(`#PinActivity${activityId}`);
      if (actionPin.length > 0) {
        actionPin.on('click', function(e) {
          e.stopPropagation();
          $('.currentPinActivity:first').removeClass('currentPinActivity');
          const jElm = $(this);
          jElm.addClass('currentPinActivity');
          const id = jElm.attr('id');
          const confirmText = jElm.attr('data-confirm');
          const captionText = jElm.attr('data-caption');
          const confirmButton = jElm.attr('data-ok');
          const cancelButton = jElm.attr('data-close');
          eXo.social.PopupConfirmation.confirm(id, [{action: UINewsActivity.pinActivity, label : confirmButton}], captionText, confirmText, cancelButton);
        });
      }

    },

    pinActivity : function () {
      const jElm = $('.currentPinActivity:first');
      const idElm = jElm.attr('id');
      const activityId = idElm.replace('PinActivity', '');
      const actionPin = $(`#PinActivity${activityId}`);
      const nodeNewsId = actionPin.attr('data-newsId');
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${nodeNewsId}/pin`,{
        credentials: 'include',
        method: 'PUT',
      }).then (function() {
        const confirmationPinDiv = document.getElementById(`confirmationPin${activityId}`);
        confirmationPinDiv.style.display = '';
        setTimeout(function () {
          confirmationPinDiv.style.display='none';
        }, 5000);
      })
        .catch (function() {
          const errorPinDiv = document.getElementById(`errorPin${activityId}`);
          errorPinDiv.style.display = '';
          setTimeout(function () {
            errorPinDiv.style.display='none';
          }, 5000);
        });
    },

  };

  return UINewsActivity;
})($);
