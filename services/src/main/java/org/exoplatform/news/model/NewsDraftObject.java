/*
 * Copyright (C) 2024 eXo Platform SAS.
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
package org.exoplatform.news.model;

import org.exoplatform.social.metadata.model.MetadataObject;

public class NewsDraftObject extends MetadataObject {

  public NewsDraftObject() {
  }

  public NewsDraftObject(String objectType, String objectId, String parentObjectId, long spaceId) {
    super(objectType, objectId, parentObjectId, spaceId);
  }
}
