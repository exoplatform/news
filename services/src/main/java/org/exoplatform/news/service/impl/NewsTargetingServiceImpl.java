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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.model.NewsTargetObject;
import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
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

  private static final Log    LOG         = ExoLogger.getLogger(NewsTargetingServiceImpl.class);

  public static final long    LIMIT       = 100;

  private static final String REFERENCED  = "referenced";

  private MetadataService     metadataService;

  private IdentityManager     identityManager;

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
  public void deleteTargetByName(String targetName, org.exoplatform.services.security.Identity  currentIdentity) throws IllegalAccessException {
    if (currentIdentity != null && !NewsUtils.canManageNewsPublishTargets(currentIdentity)) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId()
          + " not authorized to delete news target with name " + targetName);
    }
    MetadataKey targetMetadataKey = new MetadataKey(METADATA_TYPE.getName(), targetName, 0);
    Metadata targetMetadata = metadataService.getMetadataByKey(targetMetadataKey);
    metadataService.deleteMetadataById(targetMetadata.getId());
  }

  @Override
  public List<NewsTargetingEntity> getReferencedTargets(org.exoplatform.services.security.Identity currentIdentity) throws IllegalAccessException {
    if (!NewsUtils.canPublishNews(currentIdentity)) {
      throw new IllegalAccessException("User " + currentIdentity.getUserId() + " not authorized to get referenced news targets");
    }
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
  public void saveNewsTarget(String newsId, boolean staged, List<String> targets, String currentUserId) throws IllegalAccessException {
    org.exoplatform.services.security.Identity currentIdentity = NewsUtils.getUserIdentity(currentUserId);
    if (!NewsUtils.canPublishNews(currentIdentity)) {
      throw new IllegalAccessException("User " + currentUserId + " not authorized to save news targets");
    }
    NewsTargetObject newsTargetObject = new NewsTargetObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, newsId, null);
    Identity currentSocIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserId);
    targets.stream().forEach(targetName -> {
      try {
        MetadataKey metadataKey = new MetadataKey(NewsTargetingService.METADATA_TYPE.getName(), targetName, 0);
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put(PublicationDefaultStates.STAGED, String.valueOf(staged));
        metadataService.createMetadataItem(newsTargetObject, metadataKey, properties, Long.parseLong(currentSocIdentity.getId()));
      } catch (ObjectAlreadyExistsException e) {
        LOG.warn("Targets with name {} is already associated to object {}. Ignore error since it will not affect result.",
                targetName,
                newsTargetObject,
                e);
      }
    });
  }

  @Override
  public List<MetadataItem> getNewsTargetItemsByTargetName(String targetName,boolean isArchived, long offset, long limit) {
    return metadataService.getMetadataItemsByMetadataNameAndTypeAndObjectAndMetadataItemProperty(targetName, METADATA_TYPE.getName(), NewsUtils.NEWS_METADATA_OBJECT_TYPE, PublicationDefaultStates.STAGED, String.valueOf(false), offset, limit);
  }
  
  @Override
  public void deleteNewsTargets(String newsId) {
    NewsTargetObject newsTargetObject = new NewsTargetObject(NewsUtils.NEWS_METADATA_OBJECT_TYPE, newsId, null);
    metadataService.deleteMetadataItemsByMetadataTypeAndObject(METADATA_TYPE.getName(), newsTargetObject);
  }

  @Override
  public void deleteNewsTargets(String newsId, String currentUserId) throws IllegalAccessException {
    org.exoplatform.services.security.Identity currentIdentity = NewsUtils.getUserIdentity(currentUserId);
    if (!NewsUtils.canPublishNews(currentIdentity)) {
      throw new IllegalAccessException("User " + currentIdentity.getUserId() + " not authorized to delete news targets");
    }
    deleteNewsTargets(newsId);
  }

  @Override
  public Metadata createNewsTarget(NewsTargetingEntity newsTargetingEntity, org.exoplatform.services.security.Identity currentIdentity) throws IllegalArgumentException, IllegalAccessException {
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentIdentity.getUserId());
    long userIdentityId = identity == null ? 0 : Long.parseLong(identity.getId());
    Metadata metadata = fromEntity(newsTargetingEntity);
    metadata.setCreatorId(userIdentityId);
    if (!NewsUtils.canManageNewsPublishTargets(currentIdentity)) {
      throw new IllegalAccessException("User " + currentIdentity.getUserId() + " not authorized to create news targets");
    }
    MetadataKey targetMetadataKey = new MetadataKey(METADATA_TYPE.getName(), metadata.getName(), 0);
    Metadata storedMetadata = metadataService.getMetadataByKey(targetMetadataKey);
    if (storedMetadata != null) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " not authorized to create news target with same name "
              + metadata.getName());
    }
    return metadataService.createMetadata(metadata, userIdentityId);
  }

  @Override
  public Metadata updateNewsTargets(String originalTargetName, NewsTargetingEntity newsTargetingEntity, org.exoplatform.services.security.Identity currentIdentity) throws IllegalAccessException, IllegalStateException, IllegalArgumentException {
    if (!NewsUtils.canManageNewsPublishTargets(currentIdentity)) {
      throw new IllegalAccessException("User " + currentIdentity.getUserId() + " not authorized to get news targets");
    }
    Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentIdentity.getUserId());
    long userIdentityId = identity == null ? 0 : Long.parseLong(identity.getId());
    MetadataKey targetMetadataKey = new MetadataKey(METADATA_TYPE.getName(), originalTargetName, 0);
    Metadata storedMetadata = metadataService.getMetadataByKey(targetMetadataKey);
    if (storedMetadata == null) {
      throw new IllegalStateException("User " + currentIdentity.getUserId() + " can not get news target with this name " + originalTargetName);
    }
    boolean isSameName = StringUtils.equals(newsTargetingEntity.getName(), originalTargetName);
    if (!isSameName) {
      storedMetadata.setName(newsTargetingEntity.getName());
    }
    boolean isSameDescription = newsTargetingEntity.getProperties().entrySet().stream()
            .allMatch(e -> e.getValue().equals(storedMetadata.getProperties().get(e.getKey())));
    if (!isSameDescription) {
      storedMetadata.setProperties(newsTargetingEntity.getProperties());
    }
    if (isSameName && isSameDescription) {
      throw new IllegalArgumentException("User " + currentIdentity.getUserId() + " don't make any changes");
    }
    return metadataService.updateMetadata(storedMetadata, userIdentityId);
  }

  private NewsTargetingEntity toEntity(Metadata metadata) {
    NewsTargetingEntity newsTargetingEntity = new NewsTargetingEntity();
    newsTargetingEntity.setName(metadata.getName());
    if (metadata.getProperties() != null) {
      newsTargetingEntity.setProperties(metadata.getProperties());
    }
    return newsTargetingEntity;
  }

  private Metadata fromEntity(NewsTargetingEntity newsTargetingEntity) {
    Metadata metadata = new Metadata();
    metadata.setName(newsTargetingEntity.getName());
    metadata.setAudienceId(0);
    metadata.setType(METADATA_TYPE);
    metadata.setProperties(newsTargetingEntity.getProperties());
    metadata.setCreatorId(0);
    return metadata;
  }

}
