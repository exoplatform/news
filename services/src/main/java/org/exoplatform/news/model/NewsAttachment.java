package org.exoplatform.news.model;

public class NewsAttachment {

  private String id;

  private String uploadId;

  private String name;

  private String mimetype;

  private int size;

  public NewsAttachment() {
  }

  public NewsAttachment(String id, String uploadId, String name, String mimetype, int size) {
    this.id = id;
    this.uploadId = uploadId;
    this.name = name;
    this.mimetype = mimetype;
    this.size = size;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUploadId() {
    return uploadId;
  }

  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMimetype() {
    return mimetype;
  }

  public void setMimetype(String mimetype) {
    this.mimetype = mimetype;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
