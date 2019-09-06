package org.exoplatform.news.model;

import java.util.Date;

public class News {
  private String  id;

  private String  title;

  private String  summary;

  private String  body;

  private String  author;

  private String  updater;

  private String  uploadId;

  private byte[]  illustration;

  private Date    illustrationUpdateDate;

  private String  illustrationURL;

  private Date    creationDate;

  private Date    updateDate;

  private boolean pinned;

  private String  spaceId;

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
}
