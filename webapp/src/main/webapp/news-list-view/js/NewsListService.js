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
export function getNewsList(newsTarget, limit) { // eslint-disable-line no-unused-vars
  // TODO
  return Promise.resolve([]);
}

export function getNewsTarget() { // eslint-disable-line no-unused-vars
  // TODO
  return Promise.resolve(['snapshotSliderNews', 'snapshotLatestNews']);
}

export function saveSettings(saveSettingsURL ,settings) {
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