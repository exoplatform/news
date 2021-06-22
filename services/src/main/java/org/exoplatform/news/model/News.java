package org.exoplatform.news.model;

import org.exoplatform.social.core.space.model.Space;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class News {
  private String               id;

  private String               title;

  private String               summary;

  private String               body;

  private String               author;

  private String               authorDisplayName;

  private String               updater;

  private String               draftUpdater;

  private String               draftUpdaterDisplayName;

  private String               uploadId;

  private byte[]               illustration;

  private Date                 illustrationUpdateDate;

  private String               illustrationURL;

  private Date                 creationDate;

  private Date                 publicationDate;

  private Date                 updateDate;

  private boolean              pinned;

  private boolean              archived;

  private boolean              canArchive;

  private String               spaceId;

  private String               spaceDisplayName;

  private String               spaceUrl;

  private String               path;

  private String               publicationState;

  private Long                 viewsCount;

  private String               activities;
  
  private String               activityId;

  private List<NewsAttachment> attachments;

  private String spaceAvatarUrl;

  private String authorAvatarUrl;

  private Boolean canEdit;
  
  private Boolean canDelete;

  private Set<Space> sharedInSpacesList;

  private String url;

  private boolean hiddenSpace;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getBody() {
    return body;
  }
  
  public void setBody(String body) {
    this.body = body;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getUpdater() {
    return updater;
  }

  public void setUpdater(String updater) {
    this.updater = updater;
  }

  public String getDraftUpdater() {
    return draftUpdater;
  }

  public void setDraftUpdater(String draftUpdater) {
    this.draftUpdater = draftUpdater;
  }
  public String getDraftUpdaterDisplayName() {
    return draftUpdaterDisplayName;
  }

  public void setDraftUpdaterDisplayName(String draftUpdaterDisplayName) {
    this.draftUpdaterDisplayName = draftUpdaterDisplayName;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public byte[] getIllustration() {
    return illustration;
  }

  public void setIllustration(byte[] illustration) {
    this.illustration = illustration;
  }

  public Date getIllustrationUpdateDate() {
    return illustrationUpdateDate;
  }

  public void setIllustrationUpdateDate(Date illustrationUpdateDate) {
    this.illustrationUpdateDate = illustrationUpdateDate;
  }

  public String getIllustrationURL() {
    return illustrationURL;
  }

  public void setIllustrationURL(String illustrationURL) {
    this.illustrationURL = illustrationURL;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public Date getPublicationDate() {
    return publicationDate;
  }

  public void setPublicationDate(Date publicationDate) {
    this.publicationDate = publicationDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public boolean isPinned() {
    return pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getSpaceDisplayName() {
    return spaceDisplayName;
  }

  public void setSpaceDisplayName(String spaceDisplayName) {
    this.spaceDisplayName = spaceDisplayName;
  }

  public String getSpaceUrl() {
    return spaceUrl;
  }

  public void setSpaceUrl(String spaceUrl) {
    this.spaceUrl = spaceUrl;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPublicationState() {
    return publicationState;
  }

  public void setPublicationState(String publicationState) {
    this.publicationState = publicationState;
  }

  public String getAuthorDisplayName() {
    return authorDisplayName;
  }

  public void setAuthorDisplayName(String authorDisplayName) {
    this.authorDisplayName = authorDisplayName;
  }

  public Long getViewsCount() {
    return viewsCount;
  }

  public void setViewsCount(Long viewsCount) {
    this.viewsCount = viewsCount;
  }

  public String getActivities() {
    return activities;
  }

  public void setActivities(String activities) {
    this.activities = activities;
  }
  
  /**
   * @return the activityId
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * @param activityId the activityId to set
   */
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public List<NewsAttachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<NewsAttachment> attachments) {
    this.attachments = attachments;
  }

  public String getSpaceAvatarUrl() {
    return spaceAvatarUrl;
  }

  public void setSpaceAvatarUrl(String spaceAvatarUrl) {
    this.spaceAvatarUrl = spaceAvatarUrl;
  }

  public Boolean isCanEdit() {
    return canEdit;
  }

  public void setCanEdit(Boolean canEdit) {
    this.canEdit = canEdit;
  }
  
  public boolean isArchived() {
    return archived;
  }

  public void setArchived(boolean archived) {
    this.archived = archived;
  }

  public boolean isCanArchive() {
    return canArchive;
  }

  public void setCanArchive(boolean canArchive) {
    this.canArchive = canArchive;
  }

  public Set<Space> getSharedInSpacesList() {
    return sharedInSpacesList;
  }

  public void setSharedInSpacesList(Set<Space> sharedInSpacesList) {
    this.sharedInSpacesList = sharedInSpacesList;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isHiddenSpace() {
    return hiddenSpace;
  }

  public void setHiddenSpace(boolean hiddenSpace) {
    this.hiddenSpace = hiddenSpace;
  }

  public String getAuthorAvatarUrl() {
    return authorAvatarUrl;
  }

  public void setAuthorAvatarUrl(String authorAvatarUrl) {
    this.authorAvatarUrl = authorAvatarUrl;
  }

  public Boolean isCanDelete() {
    return canDelete;
  }

  public void setCanDelete(Boolean canDelete) {
    this.canDelete = canDelete;
  }
}
