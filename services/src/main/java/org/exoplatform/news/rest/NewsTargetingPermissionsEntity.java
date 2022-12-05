package org.exoplatform.news.rest;

import lombok.Data;
import lombok.NoArgsConstructor;
@NoArgsConstructor
@Data
public class NewsTargetingPermissionsEntity {
    private String id;
    private String name;
    private String remoteId;
    private String providerId;
    private String avatar;
}

