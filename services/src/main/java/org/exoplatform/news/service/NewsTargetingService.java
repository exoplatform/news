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
package org.exoplatform.news.service;

import java.util.List;

import org.exoplatform.news.model.News;
import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataType;


public interface NewsTargetingService {
  
  public static final MetadataType METADATA_TYPE = new MetadataType(4, "newsTarget");

  /**
   * Gets the {@link List} of all {@link News} targets which can be referenced from {@link News} list portlets
   * 
   * @return {@link List} of all {@link News} targets
   */
  List<NewsTargetingEntity> getTargets();
  
  /**
   * Delete the {@link News} target by a given {@link News} target name
   * 
   * @param targetName {@link News} target name to be deleted
   */
  void deleteTargetByName(String targetName);

  /**
   * Gets the {@link List} of {@link News} targets linked to a given {@link News} id
   * 
   * @param newsId {@link News} identifier of {@link News} targets to be retrieved
   * @return {@link List} of {@link News} targets by {@link News} id
   */
  List<String> getTargetsByNewsId(String newsId);

  /**
   * Gets the {@link List} of {@link News} target items by a given target name.
   *
   * @param targetName target name of metadata to be retrieved
   * @param offset limit
   * @param limit offset
   * @return {@link List} of {@link News} target items by a target name
   */
  List<MetadataItem> getNewsTargetItemsByTargetName(String targetName, long offset, long limit);

  /**
   * Gets the {@link List} of referenced targets from {@link News} list portlets 
   *
   * @return {@link List} of referenced targets
   */
  List<NewsTargetingEntity> getReferencedTargets();

  /**
   * Save a {@link List} of {@link News} targets of a given {@link News} id by the current user
   *
   * @param newsId {@link News} identifier of {@link News} targets to be saved 
   * @param staged {@link News} is staged news
   * @param targets {@link List} of {@link News} targets to be saved
   * @param currentUser current user attempting to save {@link News} targets
   */
  void saveNewsTarget(String newsId, boolean staged, List<String> targets, String currentUser);

  /**
   * Delete the {@link List} of {@link News} targets linked to a given {@link News} id
   * 
   * @param newsId {@link News} identifier of {@link News} targets to be deleted
   */
  void deleteNewsTargets(String newsId);

}
