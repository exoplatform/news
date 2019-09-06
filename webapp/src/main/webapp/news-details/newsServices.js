import {newsConstants} from '../js/newsConstants.js';

export function getActivityById(id) {
  return fetch(`${newsConstants.SOCIAL_ACTIVITY_API}/${id}`, {
    credentials: 'include',
    method: 'GET',
  });
}

export function getNewsById(id) {
  return fetch(`${newsConstants.NEWS_API}/${id}`, {
    credentials: 'include',
    method: 'GET',
  });
}

export function importFileFromUrl(url) {
  return fetch(url, {
    headers: {
      'Content-Type': 'blob'
    },
    credentials: 'include',
    method: 'GET',
  });
}

export function updateNews(news) {
  return fetch(`${newsConstants.NEWS_API}/${news.id}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(news)
  });
}

export function updateAndPostNewsActivity(activity) {
  return fetch(`${newsConstants.SOCIAL_ACTIVITY_API}/${activity.id}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'PUT',
    body: JSON.stringify(activity)
  });
}

export function clickOnEditButton(id) {
  return fetch(`${newsConstants.NEWS_API}/${id}/click`, {
    credentials: 'include',
    method: 'POST',
    body: 'edit'
  });
}

export function findUserSpaces(spaceName) {
  return fetch(`${newsConstants.SOCIAL_SPACES_SUGGESTION_API}?conditionToSearch=${spaceName}&currentUser=${newsConstants.userName}&typeOfRelation=confirmed`,{
    headers:{
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then(resp =>  resp.json()).then(json => json.options);
}

export function shareNews(newsId, activityId, sharedDescription, sharedSpaces) {
  const sharedNews = {
    description: sharedDescription,
    spacesNames: sharedSpaces,
    activityId: activityId
  };
  return fetch(`${newsConstants.NEWS_API}/${newsId}/share`,{
    headers:{
      'Content-Type': 'application/json'
    },
    method: 'POST',
    body: JSON.stringify(sharedNews)
  });
}