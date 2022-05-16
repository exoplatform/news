package org.exoplatform.news.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.exoplatform.social.metadata.model.MetadataItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {

  private String               id;

  private String               title;

  private String               summary;

  /* sanitizedBody with usernames */
  private String               body;

  /* originalBody with user mentions */
  private String               originalBody;

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

  private Date                 publishDate;

  private boolean              published;

  private boolean              archived;

  private boolean              canArchive;

  private String               spaceId;

  private String               spaceDisplayName;

  private String               spaceUrl;

  private boolean              isSpaceMember;

  private String               path;

  private Long                 viewsCount;

  private int                 commentsCount;

  private int                 likesCount;

  private String               activities;

  private String               activityId;

  private List<NewsAttachment> attachments;

  private String               spaceAvatarUrl;

  private boolean              canEdit;

  private boolean              canDelete;

  private boolean              canPublish;

  private List<String>         sharedInSpacesList;

  private String               url;

  private boolean              hiddenSpace;

  private String               schedulePostDate;

  private String               timeZoneId;

  private boolean              activityPosted;

  private Map<String, List<MetadataItem>> metadatas;

  private List<String>              targets;
}