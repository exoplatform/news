package org.exoplatform.news.notification.plugin;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.news.model.News;
import org.exoplatform.news.notification.utils.NotificationConstants;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.metadata.model.MetadataObject;
import org.exoplatform.social.notification.model.SpaceWebNotificationItem;
import org.exoplatform.social.notification.plugin.SpaceWebNotificationPlugin;

public class NewsSpaceWebNotificationPlugin extends SpaceWebNotificationPlugin {

    private static final Log LOG              = ExoLogger.getLogger(NewsSpaceWebNotificationPlugin.class);

    public static final String ID = "NewsSpaceWebNotificationPlugin";

    private ActivityManager activityManager;

    private NewsService newsService;

    public NewsSpaceWebNotificationPlugin(ActivityManager activityManager, NewsService newsService, IdentityManager identityManager, InitParams params) {
        super(identityManager, params);
        this.activityManager = activityManager;
        this.newsService = newsService;
    }

    @Override
    public SpaceWebNotificationItem getSpaceApplicationItem(NotificationInfo notification) {
        String newsId = notification.getValueOwnerParameter(NotificationConstants.NEWS_ID);
        News news = null;
        try {
            news = newsService.getNewsById(newsId, false);
        } catch (Exception e) {
            LOG.warn("Error retrieving news by id {}", newsId, e);
            return null;
        }
        if (news == null) {
            LOG.debug("News by id {} wasn't found. The space web notification will not be sent.", newsId);
            return null;
        }
        String activityId = news.getActivityId();
        if (StringUtils.isBlank(activityId)) {
            return null;
        }
        ExoSocialActivity activity = activityManager.getActivity(activityId);
        MetadataObject metadataObject;
        if (activity.isHidden()) {
            return null;
        }
        if (activity.isComment()) {
            ExoSocialActivity parentActivity = activityManager.getActivity(activity.getParentId());
            metadataObject = parentActivity.getMetadataObject();
        } else {
            metadataObject = activity.getMetadataObject();
        }
        SpaceWebNotificationItem spaceWebNotificationItem = new SpaceWebNotificationItem(metadataObject.getType(),
                metadataObject.getId(),
                0,
                metadataObject.getSpaceId());
        spaceWebNotificationItem.setActivityId(activityId);
        if (activity.isComment()) {
            spaceWebNotificationItem.addApplicationSubItem(activity.getId());
        }
        return spaceWebNotificationItem;
    }
}
