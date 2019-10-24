import {newsConstants} from '../js/newsConstants.js';
import anchorme from 'anchorme';

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

export function getNews() {
  return fetch(`${newsConstants.NEWS_API}`, {
    credentials: 'include',
    method: 'GET',
  });
}

export function getNewsDrafts() {
  return fetch(`${newsConstants.NEWS_API}?author=${newsConstants.userName}&spaceId=${newsConstants.SPACE_ID}&publicationState=draft`, {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Error getting news draft list');
    }
  });
}

export function saveNews(news) {
  return fetch(`${newsConstants.NEWS_API}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(news)
  }).then((data) => {
    return data.json();
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
  }).then(resp => resp.json());
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

export function deleteDraft(newsId) {
  return fetch(`${newsConstants.NEWS_API}/${newsId}`, {
    credentials: 'include',
    method: 'DELETE'
  });
}

export function incrementViewsNumberOfNews (newsId) {
  return fetch(`${newsConstants.NEWS_API}/${newsId}/view`, {
    credentials: 'include',
    method: 'POST',
  });
}

export function linkify (newsSummary, newsContent) {
  const newNewsContent = anchorme(newsContent,{
    attributes:[
      {
        name:'target',
        value:'_blank'
      },
    ],
    exclude:function(UrlObj){
      const newsBodyElem = CKEDITOR.instances['newsContent'].document.$.body;
      const newsBodyP = newsBodyElem.querySelectorAll('p');
      for(let indexp = 0; indexp < newsBodyP.length; indexp++) {
        const links = newsBodyP[indexp].getElementsByTagName('a');
        for(let index = 0; index < links.length; index++) {
          if(links [index].innerText === UrlObj.raw) {
            return true;
          }
        }
      }

    }
  });
  const newNewsSummary = anchorme(newsSummary,{
    attributes:[
      {
        name:'target',
        value:'_blank'
      },
    ]
  });
  return [newNewsSummary, newNewsContent];
}