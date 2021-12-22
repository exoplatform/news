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
package org.exoplatform.news.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.exoplatform.news.model.NewsTargetObject;
import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ObjectAlreadyExistsException;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;
import org.exoplatform.social.metadata.model.MetadataItem;
import org.exoplatform.social.metadata.model.MetadataKey;

/**
 * Service managing News Targeting
 */
public class NewsTargetingServiceImpl implements NewsTargetingService {

  private static final Log LOG                             = ExoLogger.getLogger(NewsTargetingServiceImpl.class);

  public static final long        LIMIT           =  100;

  private static final String        LABEL           =  "label";

  private static final String        REFERENCED           =  "referenced";

  private MetadataService        metadataService;
  
  private IdentityManager identityManager;

  public NewsTargetingServiceImpl(MetadataService metadataService, IdentityManager identityManager){
    this.metadataService = metadataService;
    this.identityManager = identityManager;
  }

  @Override
  public List<NewsTargetingEntity> getTargets() {
    List<Metadata> targets = metadataService.getMetadatas(METADATA_TYPE.getName(), LIMIT);
    return targets.stream().map(this::toEntity).collect(Collectors.toList());
  }
  
  @Override
  public void deleteTargetByName(String targetName) {
    MetadataKey targetMetadataKey = new MetadataKey(METADATA_TYPE.getName(), targetName, 0);
    Metadata targetMetadata = metadataService.getMetadataByKey(targetMetadataKey);
    metadataService.deleteMetadataById(targetMetadata.getId());
  }

  @Override
  public List<NewsTargetingEntity> getReferencedTargets() {
    List<Metadata> referencedTargets = metadataService.getMetadatasByProperty(REFERENCED, String.valueOf(true), LIMIT);
    return referencedTargets.stream().map(this::toEntity).collect(Collectors.toList());
  }

  @Override
  public List<String> getTargetsByNewsId(String newsId) {
    NewsTargetObject newsTargetObject = new NewsTargetObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, newsId, null);
    List<MetadataItem> newsTargets = metadataService.getMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(), newsTargetObject);
    return newsTargets.stream().map(MetadataItem::getMetadata).map(Metadata::getName).collect(Collectors.toList());
  }

  @Override
  public void saveNewsTarget(String newsId, List<String> targets, String currentUser) {
    NewsTargetObject newsTargetObject = new NewsTargetObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, newsId, null);
    Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUser);
    targets.stream().forEach(targetName -> {
      try {
        MetadataKey metadataKey = new MetadataKey(NewsTargetingService.METADATA_TYPE.getName(), targetName, 0);
        metadataService.createMetadataItem(newsTargetObject, metadataKey, Long.parseLong(currentIdentity.getId()));
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("Targets with name {} is already associated to object {}. Ignore error since it will not affect result.",
                targetName,
                newsTargetObject,
                e);
      }
    });
  }

  @Override
  public void deleteNewsTargets(String newsId) {
    NewsTargetObject newsTargetObject = new NewsTargetObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, newsId, null);
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(), newsTargetObject);
  }

  private NewsTargetingEntity toEntity(Metadata metadata) {
    NewsTargetingEntity newsTargetingEntity = new NewsTargetingEntity();
    newsTargetingEntity.setName(metadata.getName());
    if(metadata.getProperties() != null) {
      newsTargetingEntity.setLabel(metadata.getProperties().get(LABEL));
    }
    return newsTargetingEntity;
  }

}
