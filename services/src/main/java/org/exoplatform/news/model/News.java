package org.exoplatform.news.model;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.exoplatform.social.core.space.model.Space;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {

  private String               id;

  private String               title;

  private String               summary;

  private String               body;

  private String               author;

  private String               authorDisplayName;
  
  private String               authorAvatarUrl;

  private String               updater;

  private String               updaterFullName;

  private String               draftUpdater;

  private String               draftUpdaterDisplayName;

  private String               draftUpdaterUserName;
  
  private Date                 draftUpdateDate;
  
  private boolean              draftVisible;

  private String               uploadId;

  private byte[]               illustration;

  private Date                 illustrationUpdateDate;

  private String               illustrationURL;

  private Date                 creationDate;

  private Date                 publicationDate;
  
  private String               publicationState;

  private Date                 updateDate;

  private boolean              published;

  private boolean              archived;

  private boolean              canArchive;

  private String               spaceId;

  private String               spaceDisplayName;

  private String               spaceUrl;

  private boolean              isSpaceMember;

  private String               path;

  private Long                 viewsCount;

  private String               activities;

  private String               activityId;

  private List<NewsAttachment> attachments;

  private String               spaceAvatarUrl;

  private boolean              canEdit;

  private boolean              canDelete;

  private boolean              canPublish;

  private Set<Space>           sharedInSpacesList;

  private String               url;

  private boolean              hiddenSpace;

  private String               schedulePostDate;

  private String               timeZoneId;

  private boolean              activityPosted;

  private List<String>              targets;
}