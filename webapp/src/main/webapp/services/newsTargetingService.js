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
import {newsConstants} from '../js/newsConstants.js';

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

export function getReferencedTarget() {
  return fetch(`${newsConstants.NEWS_API}/targeting/referenced`, {
    credentials: 'include',
    method: 'GET',
  }).then((resp) => {
    if (resp && resp.ok) {
      return resp.json();
    }
  });
}