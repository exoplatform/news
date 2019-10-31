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
          UINewsActivity.confirmAction(jElm, 'pin');
        });
      }
      const actionUnpin = $(`#unpinActivity${activityId}`);
      if (actionUnpin.length > 0) {
        actionUnpin.on('click', function(e) {
          e.stopPropagation();
          $('.currentUnpinActivity:first').removeClass('currentUnpinActivity');
          const jElm = $(this);
          jElm.addClass('currentUnpinActivity');
          UINewsActivity.confirmAction(jElm, 'unpin');
        });
      }
      $('.newsBody a').attr('target', '_blank');
    },

    confirmAction: function(jElm, action) {

      const id = jElm.attr('id');
      const confirmText = jElm.attr('data-confirm');
      const captionText = jElm.attr('data-caption');
      const confirmButton = jElm.attr('data-ok');
      const cancelButton = jElm.attr('data-close');
      if (action === 'pin') {
        eXo.social.PopupConfirmation.confirm(id, [{action: UINewsActivity.pinActivity, label : confirmButton}], captionText, confirmText, cancelButton);
      }
      else if (action === 'unpin') {
        eXo.social.PopupConfirmation.confirm(id, [{action: UINewsActivity.unpinActivity, label : confirmButton}], captionText, confirmText, cancelButton);
      }

    },

    pinActivity : function () {
      const jElm = $('.currentPinActivity:first');
      const idElm = jElm.attr('id');
      const activityId = idElm.replace('PinActivity', '');
      const actionPin = $(`#PinActivity${activityId}`);
      const nodeNewsId = actionPin.attr('data-newsId');
      const messagePinBodyElm = document.getElementById(`messagePinBody${activityId}`);
      const successPinMessage = messagePinBodyElm.getAttribute('data-success-pin');
      const errorPinMessage = messagePinBodyElm.getAttribute('data-error-pin');
      const news = {
        pinned: true,
      };
      UINewsActivity.updatePinnedField(news, nodeNewsId, activityId, messagePinBodyElm, successPinMessage, errorPinMessage);
      },

    unpinActivity : function () {
      const jElm = $('.currentUnpinActivity:first');
      const idElm = jElm.attr('id');
      const activityId = idElm.replace('unpinActivity', '');
      const actionUnpin = $(`#unpinActivity${activityId}`);
      const nodeNewsId = actionUnpin.attr('data-newsId');
      const messagePinBodyElm = document.getElementById(`messagePinBody${activityId}`);
      const successUnpinMessage = messagePinBodyElm.getAttribute('data-success-unpin');
      const errorUnpinMessage = messagePinBodyElm.getAttribute('data-error-unpin');
      const news = {
        pinned: false,
      };
      UINewsActivity.updatePinnedField(news, nodeNewsId, activityId, messagePinBodyElm, successUnpinMessage, errorUnpinMessage);
    },

    updatePinnedField:function (news, nodeNewsId, activityId, messageBodyElem, successMeassage, errorMessage) {
      const timeout = 5000;
      const messagePinDiv = document.getElementById(`messagePin${activityId}`);
      const messagePinAlert = document.getElementById(`alertMessagePin${activityId}`);
      const iconMessagePin = document.getElementById(`iconMessagePin${activityId}`);
      fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/news/${nodeNewsId}`,{
        headers: {
          'Content-Type': 'application/json'
        },
        credentials: 'include',
        method: 'PATCH',
        body: JSON.stringify(news)
      }).then (function() {
        messagePinDiv.style.display = '';
        messagePinAlert.classList.add('alert-success');
        iconMessagePin.classList.add('uiIconSuccess');
        messageBodyElem.innerHTML = successMeassage;
        setTimeout(function () {
          messagePinDiv.style.display='none';
          messagePinAlert.classList.remove('alert-success');
          iconMessagePin.classList.remove('uiIconSuccess');
          UINewsActivity.updateActivityStream();
        }, timeout);
      })
        .catch (function() {
          messagePinDiv.style.display = '';
          messagePinAlert.classList.add('alert-error');
          iconMessagePin.classList.add('uiIconError');
          messageBodyElem.innerHTML= errorMessage;
          setTimeout(function () {
            messagePinDiv.style.display='none';
            messagePinAlert.classList.remove('alert-error');
            iconMessagePin.classList.remove('uiIconError');
            UINewsActivity.updateActivityStream();
          }, timeout);
        });
    },

    updateActivityStream: function(){
      const refreshButton = document.querySelector('.uiActivitiesDisplay #RefreshButton');
      if (refreshButton) {
        refreshButton.click();
      }
    },

    clickOnNews: function (newsId) {
      const env = eXo.social.portal;
      const baseRestUrl = `${env.context  }/${  env.rest  }/v1/news/${newsId}/click`;
      $(`#seeMoreId${newsId}`).click(function () {
        $.ajax({
          url: baseRestUrl,
          contentType: 'application/json',
          type: 'POST',
          data: JSON.stringify({'name': 'readMore'}),
          cache: false,
        });
      });
      $(`#titleId${newsId}`).click(function () {
        $.ajax({
          url: baseRestUrl,
          contentType: 'application/json',
          type: 'POST',
          data: JSON.stringify({'name': 'title'}),
          cache: false,
        });
      });

    },
  };

  return UINewsActivity;
})($);
