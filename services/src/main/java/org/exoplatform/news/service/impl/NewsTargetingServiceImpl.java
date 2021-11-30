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

import org.exoplatform.news.rest.NewsTargetingEntity;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.social.metadata.MetadataService;
import org.exoplatform.social.metadata.model.Metadata;

/**
 * Service managing News Targeting
 */
public class NewsTargetingServiceImpl implements NewsTargetingService {

  public static final long        LIMIT           =  100;

  private static final String        LABEL           =  "label";

  private static final String        REFERENCED           =  "referenced";

  private MetadataService        metadataService;

  public NewsTargetingServiceImpl(MetadataService metadataService){
    this.metadataService = metadataService;
  }

  @Override
  public  List<NewsTargetingEntity> getTargets() {
    List<Metadata> targets = metadataService.getMetadatas(METADATA_TYPE.getName(),  LIMIT);
    return targets.stream().map(this::toEntity).collect(Collectors.toList());
  }

  @Override
  public  List<NewsTargetingEntity> getReferencedTargets() {
    List<Metadata> referencedTargets = metadataService.getMetadatasByProperty(REFERENCED, String.valueOf(true), LIMIT);
    return referencedTargets.stream().map(this::toEntity).collect(Collectors.toList());
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
