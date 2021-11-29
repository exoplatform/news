/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import {newsConstants} from '../../js/newsConstants.js';

export function getNewsList() {
  let newsList = null;
  newsList = [
    {
      body: 'The eXo Platform is widely adopted by employees thanks to its simplicity and ease of use.\n',
      id: '0a1853307f00010130cb81f7419ab5f0',
      illustrationURL: 'https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg',
      title: 'Welcome to your new digital workplace platform\n ',
      url: '/portal/dw/activity?id=21',
      postDate: 'March 18',
      author: 'root',
      authorFullName: 'root root',
      authorDisplayName: 'Root Root',
      spaceId: '3'
    },
    {
      body: 'Lorem Ipsum is simply dummy text of the printing and typesetting',
      id: '0a1853307f00010130cb81f7419ab5f0',
      illustrationURL: 'https://cdn.vuetifyjs.com/images/carousel/sky.jpg',
      title: 'testing work2 ',
      url: '/portal/dw/activity?id=21',
      postDate: 'November 30',
      author: 'root',
      authorFullName: 'Root Root',
      authorDisplayName: 'John Wick',
      spaceId: '3'
    },
    {
      body: 'Lorem Ipsum is simply dummy text of the printing and typesetting ',
      id: '0a1853307f00010130cb81f7419ab5f0',
      illustrationURL: 'https://cdn.vuetifyjs.com/images/carousel/bird.jpg',
      title: 'testing work3 ',
      url: '/portal/dw/activity?id=21',
      postDate: 'April 20',
      author: 'root',
      authorFullName: 'Root Root',
      authorDisplayName: 'John Wick',
      spaceId: '3'
    },
    {
      body: 'Lorem Ipsum is simply dummy text of the printing and typesetting',
      id: '0a1853307f00010130cb81f7419ab5f0',
      illustrationURL: 'https://cdn.vuetifyjs.com/images/carousel/planet.jpg',
      title: 'testing work4 ',
      url: '/portal/dw/activity?id=21',
      postDate: 'May 20',
      author: 'root',
      authorFullName: 'Root Root',
      authorDisplayName: 'John Wick',
      spaceId: '3'
    },
  ];
  return Promise.resolve(newsList);
}

export function getNewsTarget() {
  return fetch(`${newsConstants.NEWS_API}/targeting`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    }
  });
}

export function saveSettings(saveSettingsURL, settings) {
  const formData = new FormData();
  if (settings) {
    Object.keys(settings).forEach(name => {
      formData.append(name, settings[name]);
    });
  }
  return fetch(saveSettingsURL, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: new URLSearchParams(formData).toString(),
  }).then(resp => {
    if (!resp || !resp.ok) {
      throw new Error('Response code indicates a server error', resp);
    }
  });
}