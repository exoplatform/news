import {newsConstants} from '../js/newsConstants.js';

export function saveNews(news) {
  return fetch(`${newsConstants.NEWS_API}`, {
    headers: {
      'Content-Type': 'application/json'
    },
    credentials: 'include',
    method: 'POST',
    body: JSON.stringify(news)
  });
}