
package org.exoplatform.news.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.portal.application.localization.LocalizationFilter;
import org.exoplatform.social.core.utils.MentionUtils;
import org.exoplatform.social.metadata.favorite.model.Favorite;
import org.exoplatform.social.metadata.tag.TagService;
import org.exoplatform.social.metadata.tag.model.TagFilter;
import org.exoplatform.social.metadata.tag.model.TagName;
import org.exoplatform.social.rest.api.RestUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.news.service.NewsService;
import org.exoplatform.news.storage.NewsAttachmentsStorage;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.services.cms.thumbnail.ThumbnailService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.http.PATCH;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.metadata.favorite.FavoriteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Path("v1/news")
@Tag(name = "v1/news", description = "Managing news")
public class NewsRestResourcesV1 implements ResourceContainer, Startable {

  private static final Log          LOG                             = ExoLogger.getLogger(NewsRestResourcesV1.class);

  private static final String       PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String       PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private NewsService               newsService;

  private NewsAttachmentsStorage    newsAttachmentsService;

  private SpaceService              spaceService;

  private IdentityManager           identityManager;

  private ScheduledExecutorService  scheduledExecutor;

  private PortalContainer           container;

  private FavoriteService           favoriteService;

  private ThumbnailService          thumbnailService;
  
  private Map<String, String>       newsToDeleteQueue               = new HashMap<>();

  private static final int          CACHE_DURATION_SECONDS          = 31536000;

  private static final long         CACHE_DURATION_MILLISECONDS     = CACHE_DURATION_SECONDS * 1000L;

  private static final CacheControl ILLUSTRATION_CACHE_CONTROL      = new CacheControl();

  static {
    ILLUSTRATION_CACHE_CONTROL.setMaxAge(CACHE_DURATION_SECONDS);
  }

  private enum FilterType {
    PINNED, MYPOSTED, ARCHIVED, DRAFTS, SCHEDULED, ALL
  }
  

  public NewsRestResourcesV1(NewsService newsService,
                             NewsAttachmentsStorage newsAttachmentsService,
                             SpaceService spaceService,
                             IdentityManager identityManager,
                             PortalContainer container,
                             FavoriteService favoriteService,
                             ThumbnailService thumbnailService) {

    this.newsService = newsService;
    this.newsAttachmentsService = newsAttachmentsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.container = container;
    this.favoriteService = favoriteService;
    this.thumbnailService = thumbnailService;
  }

  @Override
  public void start() {
    scheduledExecutor = Executors.newScheduledThreadPool(1);
  }

  @Override
  public void stop() {
    if (scheduledExecutor != null) {
      scheduledExecutor.shutdown();
    }
  }


  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Create a news", method = "POST", description = "This creates the news if the authenticated user is a member of the space or a spaces super manager. The news is created in draft status, unless the publicationState property is set to 'published'.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News created"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "User not authorized to create the news"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response createNews(@Context HttpServletRequest request, @RequestBody(description = "News object to create", required = true) News news) {
    if (news == null || StringUtils.isEmpty(news.getSpaceId())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      News createdNews = newsService.createNews(news, currentIdentity);

      return Response.ok(createdNews).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to create news", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @Path("canCreateNews/{spaceId}")
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "check if the current user can create a news in the given space", method = "GET", description = "This checks if the current user can create a news in the given space")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User ability to create a news is returned"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "User not authorized to create a news"),
      @ApiResponse(responseCode = "404", description = "Space not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response canCreateNews(@Context HttpServletRequest request,
                                @Parameter(description = "space id", required = true) @PathParam("spaceId") String spaceId) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      if (StringUtils.isBlank(spaceId)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(String.valueOf(newsService.canCreateNews(space, currentIdentity))).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to check if we can create news", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when checking if the authenticated user can create a news", e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Create a news", method = "PUT", description = "This updates the news if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News updated"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "User not authorized to update the news"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response updateNews(@Parameter(description = "News id", required = true) @PathParam("id") String id,
                             @Parameter(description = "Post news", required = false) @QueryParam("post") boolean post,
                             @Parameter(description = "News object type to be updated", required = false) @QueryParam("type") String newsObjectType,
                             @RequestBody(description = "News object to be updated", required = true) News updatedNews) {

    if (updatedNews == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      News news = newsService.getNewsById(id, currentIdentity, false, newsObjectType);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      news.setTitle(updatedNews.getTitle());
      news.setSummary(updatedNews.getSummary());
      news.setBody(updatedNews.getBody());
      news.setUploadId(updatedNews.getUploadId());
      news.setAttachments(updatedNews.getAttachments());
      news.setPublicationState(updatedNews.getPublicationState());
      news.setUpdaterFullName(updatedNews.getUpdaterFullName());
      news.setActivityPosted(updatedNews.isActivityPosted());
      news.setTargets(updatedNews.getTargets());
      news.setAudience(updatedNews.getAudience());


      news = newsService.updateNews(news, currentIdentity.getUserId(), post, updatedNews.isPublished(), newsObjectType);

      return Response.ok(news).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to update news", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    }  catch (Exception e) {
      LOG.error("Error when updating the news " + id, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @DELETE
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Delete news", method = "DELETE", description = "This deletes the news")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News deleted"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "User not authorized to delete the news"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response deleteNews(@Context HttpServletRequest request,
                             @Parameter(description = "News id", required = true)
                             @PathParam("id") String id,
                             @Parameter(description = "Is draft to delete") @Schema(defaultValue = "false") @QueryParam("isDraft") boolean isDraft,
                             @Parameter(description = "Time to effectively delete news", required = false)
                             @QueryParam(
                                     "delay"
                             ) long delay) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      News news = newsService.getNewsById(id, currentIdentity, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      if (delay > 0) {//TODO Move to service layer
        newsToDeleteQueue.put(id, currentIdentity.getUserId());
        scheduledExecutor.schedule(() -> {
          if (newsToDeleteQueue.containsKey(id)) {
            ExoContainerContext.setCurrentContainer(container);
            RequestLifeCycle.begin(container);
            try {
              newsToDeleteQueue.remove(id);
              newsService.deleteNews(id, currentIdentity, isDraft);
            } catch (IllegalAccessException e) {
              LOG.error("User '{}' attempts to delete a non authorized news", currentIdentity.getUserId(), e);
            } catch (Exception e) {
              LOG.warn("Error when deleting the news with id " + id, e);
            } finally {
              RequestLifeCycle.end();
            }
          }
        }, delay, TimeUnit.SECONDS);
      } else {
        newsToDeleteQueue.remove(id);
        newsService.deleteNews(id, currentIdentity, isDraft);
      }
      return Response.ok().build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to delete news", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when deleting the news with id " + id, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @Path("{id}/undoDelete")
  @POST
  @RolesAllowed("users")
  @Operation(
      summary = "Undo deleting news if not yet effectively deleted",
      method = "POST",
      description = "Undo deleting news if not yet effectively deleted"
  )
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "403", description = "Forbidden operation"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), }
  )
  public Response undoDeleteNews(
                                  @Context HttpServletRequest request,
                                  @Parameter(description = "News node identifier", required = true)
                                  @PathParam(
                                    "id"
                                  ) String id) {
    if (StringUtils.isBlank(id)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("News identifier must not be null or empty").build();
    }
    if (newsToDeleteQueue.containsKey(id)) {//TODO Move to service layer
      String authenticatedUser = request.getRemoteUser();
      String originalModifierUser = newsToDeleteQueue.get(id);
      if (!originalModifierUser.equals(authenticatedUser)) {
        LOG.warn("User {} attempts to cancel deletion of a news deleted by user {}", authenticatedUser, originalModifierUser);
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      newsToDeleteQueue.remove(id);
      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("News with id {} was already deleted or isn't planned to be deleted" + id)
                     .build();
    }
  }
  
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get a news", method = "GET", description = "This gets the news with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNewsById(@Context HttpServletRequest request,
                              @Parameter(description = "News id", required = true) @PathParam("id") String id,
                              @Parameter(description = "fields", required = true) @QueryParam("fields") String fields,
                              @Parameter(description = "News object type to be fetched", required = false) @QueryParam("type") String newsObjectType,
                              @Parameter(description = "Is edit mode") @Schema(defaultValue = "false") @QueryParam("editMode") boolean editMode) {
    String authenticatedUser = request.getRemoteUser();
    try {
      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, editMode, newsObjectType);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Locale userLocale = LocalizationFilter.getCurrentLocale();
      news.setBody(MentionUtils.substituteRoleWithLocale(news.getBody(), userLocale));
      news.setIllustration(null);
      // check favorite
      Identity userIdentity = identityManager.getOrCreateUserIdentity(currentIdentity.getUserId());
      if(userIdentity != null) {
        news.setFavorite(favoriteService.isFavorite(new Favorite("news", news.getId(), "", Long.parseLong(userIdentity.getId()))));
      }

      if (StringUtils.isNotEmpty(fields) && fields.equals("spaces")) {//TODO Move to service layer
        News filteredNews = new News();
        List<String> spacesList = new ArrayList<>();
        String newsActivities = news.getActivities();
        for (String newsActivity : newsActivities.split(";")) {
          String spaceId = newsActivity.split(":")[0];
          spacesList.add(spaceId);
        }
        filteredNews.setSharedInSpacesList(spacesList);
        return Response.ok(filteredNews).build();
      } else {
        return Response.ok(news).build();
      }
    } catch (IllegalAccessException e) {
      LOG.warn("User {} attempt to access unauthorized news with id {}", authenticatedUser, id);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @POST
  @Path("markAsRead/{id}")
  @RolesAllowed("users")
  @Produces(MediaType.TEXT_PLAIN)
  @Operation(summary = "mark a news article as read", method = "POST", description = "This marks a news article as read by the user who accessed its details.")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
          @ApiResponse(responseCode = "404", description = "News not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error")})

  public Response markNewsAsRead(@Context HttpServletRequest request,
                                 @Parameter(description = "News id", required = true) @PathParam("id") String id) {
    String authenticatedUser = request.getRemoteUser();
    try {
      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      newsService.markAsRead(news, authenticatedUser);
      return Response.ok("ok").type(MediaType.TEXT_PLAIN).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User {} has no access rights on news with id {}", authenticatedUser, id);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error while marking news with id: {} as read", id, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }
  
  @GET
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get news list", method = "GET", description = "This gets the list of news with the given search text, of the given author, in the given space or spaces, with the given publication state, with the given pinned state if the authenticated user is a member of the spaces or a super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News list returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news list"),
      @ApiResponse(responseCode = "404", description = "News list not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNews(@Context HttpServletRequest request,
                          @Parameter(description = "News author", required = true) @QueryParam("author") String author,
                          @Parameter(description = "News spaces", required = true) @QueryParam("spaces") String spaces,
                          @Parameter(description = "News filter", required = true) @QueryParam("filter") String filter,
                          @Parameter(description = "search text", required = true) @QueryParam("text") String text,
                          @Parameter(description = "News pagination offset") @Schema(defaultValue = "0") @QueryParam("offset") int offset,
                          @Parameter(description = "News pagination limit") @Schema(defaultValue = "10") @QueryParam("limit") int limit,
                          @Parameter(description = "News total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
    try {//TODO Move to service layer
      String authenticatedUser = request.getRemoteUser();
      if (StringUtils.isBlank(author) || !authenticatedUser.equals(author)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      NewsEntity newsEntity = new NewsEntity();
      //Get news drafts by space
      List<String> spacesList = new ArrayList<>();
      // Set spaces to search news in
      if (StringUtils.isNotEmpty(spaces)) {
        for (String spaceId : spaces.split(",")) {
          Space space = spaceService.getSpaceById(spaceId);
          if (space == null || (!spaceService.isSuperManager(authenticatedUser) && !spaceService.isMember(space, authenticatedUser))) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
          }
          spacesList.add(spaceId);
        }
      }
      NewsFilter newsFilter = buildFilter(spacesList, filter, text, author, limit, offset);
      List<News> news;
      //Set text to search news with
      if (StringUtils.isNotEmpty(text)) {
        String lang = request.getLocale().getLanguage();
        TagService tagService = CommonsUtils.getService(TagService.class);
        long userIdentityId = RestUtils.getCurrentUserIdentityId();
        if (text.indexOf("#") == 0) {
          String tagName = text.replace("#","");
          List<TagName> tagNames = tagService.findTags(new TagFilter(tagName, 0), userIdentityId);
          if (tagNames != null && !tagNames.isEmpty()) newsFilter.setTagNames(tagNames.stream().map(e -> e.getName()).toList());
        }

        news = newsService.searchNews(newsFilter, lang);
      } else {
        org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        news = newsService.getNews(newsFilter, currentIdentity);
      }

      if (news != null && news.size() != 0) {
        for (News newsItem : news) {
          newsItem.setIllustration(null);
        }
      }
      if (news != null) {
        Locale userLocale = LocalizationFilter.getCurrentLocale();
        news.forEach(news1 -> news1.setBody(MentionUtils.substituteRoleWithLocale(news1.getBody(), userLocale)));
      }
      newsEntity.setNews(news);
      newsEntity.setOffset(offset);
      newsEntity.setLimit(limit);
      if (returnSize) {
        newsEntity.setSize(newsService.getNewsCount(newsFilter));
      }
      return Response.ok(newsEntity).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news with params author=" + author + ", spaces=" + spaces, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("byTarget/{targetName}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get news list", method = "GET", description = "This gets the list of news by the given target.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News list returned"),
          @ApiResponse(responseCode = "401", description = "User not authorized to get the news list"),
          @ApiResponse(responseCode = "404", description = "News list not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNewsByTarget(@Context HttpServletRequest request,
                                  @Parameter(description = "News target name", required = true) @PathParam("targetName") String targetName,
                                  @Parameter(description = "News pagination offset") @Schema(defaultValue = "0") @QueryParam("offset") int offset,
                                  @Parameter(description = "News pagination limit") @Schema(defaultValue = "10") @QueryParam("limit") int limit,
                                  @Parameter(description = "News total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
    try {
      String authenticatedUser = request.getRemoteUser();
      if (StringUtils.isBlank(targetName)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      if (offset < 0) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Offset must be 0 or positive").build();
      }
      if (limit < 0) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Limit must be positive").build();
      }
      NewsFilter newsFilter = buildFilter(null, "", "", authenticatedUser, limit, offset);
      NewsEntity newsEntity = new NewsEntity();
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      List<News> news = newsService.getNewsByTargetName(newsFilter, targetName, currentIdentity);
      Locale userLocale = LocalizationFilter.getCurrentLocale();
      news.forEach(news1 -> news1.setBody(MentionUtils.substituteRoleWithLocale(news1.getBody(),userLocale)));
      newsEntity.setNews(news);
      newsEntity.setOffset(offset);
      newsEntity.setLimit(limit);
      if (returnSize) {
        newsEntity.setSize(news.size());
      }
      return Response.ok(newsEntity).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news with target name=" + targetName, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("byActivity/{activityId}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get a news identified by its activity or shared activity identifier", method = "GET", description = "This gets the news with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response getNewsByActivityId(@Parameter(description = "Activity id", required = true) @PathParam("activityId") String activityId) {
    if (StringUtils.isBlank(activityId)) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      News news = newsService.getNewsByActivityId(activityId, currentIdentity);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      news.setIllustration(null);
      Locale userLocale = LocalizationFilter.getCurrentLocale();
      news.setBody(MentionUtils.substituteRoleWithLocale(news.getBody(),userLocale));

      Identity userIdentity = identityManager.getOrCreateUserIdentity(currentIdentity.getUserId());
      if (userIdentity != null) {
        news.setFavorite(favoriteService.isFavorite(new Favorite("news",
                                                                 news.getId(),
                                                                 "",
                                                                 Long.parseLong(userIdentity.getId()))));
      }
      return Response.ok(news).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User {} attempt to access unauthorized news with id {}", currentIdentity.getUserId(), activityId);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + activityId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @PATCH
  @Path("schedule")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Schedule a news", method = "POST", description = "This schedules the news if the authenticated user is a member of the space or a spaces super manager. The news is created in staged status, after reaching a date of publication startPublishedDate, the publicationState property is set to 'published'.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News scheduled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "User not authorized to schedule the news"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response scheduleNews(@Context HttpServletRequest request,
                               @RequestBody(description = "News object to be scheduled", required = true) News scheduledNews) {
    if (scheduledNews == null || StringUtils.isEmpty(scheduledNews.getId())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      News news = newsService.getNewsById(scheduledNews.getId(), currentIdentity, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      news = newsService.scheduleNews(scheduledNews, currentIdentity);
      return Response.ok(news).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to schedule news", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when scheduling the news " + scheduledNews.getTitle(), e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @Path("search")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Search the list of news available with query", method = "GET", description = "Search the list of news available with query")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Request fulfilled"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response search(@Context UriInfo uriInfo,
                         @Context HttpServletRequest request,
                         @Parameter(description = "Term to search", required = true) @QueryParam("query") String query,
                         @Parameter(description = "Properties to expand") @QueryParam("expand") String expand,
                         @Parameter(description = "Offset") @Schema(defaultValue = "0") @QueryParam("offset") int offset,
                         @Parameter(description = "Tag names used to search news", required = true) @QueryParam("tags") List<String> tagNames,
                         @Parameter(description = "Limit") @Schema(defaultValue = "20") @QueryParam("limit") int limit,
                         @Parameter(description = "Favorites") @Schema(defaultValue = "false") @QueryParam("favorites") boolean favorites) {

    if (StringUtils.isBlank(query) && !favorites && CollectionUtils.isEmpty(tagNames)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'query' parameter is mandatory").build();
    }

    String authenticatedUser = request.getRemoteUser();
    Identity currentIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    if (offset < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Offset must be 0 or positive").build();
    }
    if (limit < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Limit must be positive").build();
    }
    NewsFilter filter = new NewsFilter();
    filter.setSearchText(query);
    filter.setFavorites(favorites);
    filter.setLimit(limit);
    filter.setOffset(offset);
    filter.setTagNames(tagNames);
    List<NewsESSearchResult> searchResults = newsService.search(currentIdentity, filter);
    List<NewsSearchResultEntity> results =
                                         searchResults.stream()
                                                      .map(searchResult -> org.exoplatform.news.utils.EntityBuilder.fromNewsSearchResult(favoriteService,
                                                                                                                                         searchResult,
                                                                                                                                         currentIdentity,
                                                                                                                                         uriInfo))
                                                      .collect(Collectors.toList());

    return Response.ok(results).build();
  }

  @GET
  @Path("attachments/{attachmentId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(summary = "Get a news attachment", method = "GET", description = "This gets the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNewsAttachmentById(@Context HttpServletRequest request,
                                        @Parameter(description = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
    try {
      NewsAttachment attachment = newsAttachmentsService.getNewsAttachment(attachmentId);
      if (attachment == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(attachment).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news attachment " + attachmentId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("attachments/{attachmentId}/file")
  @RolesAllowed("users")
  @Operation(summary = "Download a news attachment", method = "GET", description = "This downloads the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNewsAttachmentBinaryById(@Context HttpServletRequest request,
                                              @Parameter(description = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
    try {
      NewsAttachment attachment = newsAttachmentsService.getNewsAttachment(attachmentId);
      if (attachment == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      InputStream stream = newsAttachmentsService.getNewsAttachmentStream(attachmentId);

      Response.ResponseBuilder responseBuilder = Response.ok(stream, attachment.getMimetype());
      responseBuilder.header("Content-Disposition", "attachment; filename=\"" + attachment.getName() + "\"");

      return responseBuilder.build();
    } catch (Exception e) {
      LOG.error("Error when getting the news attachment " + attachmentId, e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("attachments/{attachmentId}/open")
  @RolesAllowed("users")
  @Operation(summary = "Opens a news attachment", method = "GET", description = "This opens the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response openNewsAttachmentById(@Context HttpServletRequest request,
                                         @Parameter(description = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
    try {
      NewsAttachment attachment = newsAttachmentsService.getNewsAttachment(attachmentId);
      if (attachment == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String openUrl = newsAttachmentsService.getNewsAttachmentOpenUrl(attachmentId);

      return Response.temporaryRedirect(URI.create(openUrl)).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news attachment " + attachmentId, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("{id}/illustration")
  @Operation(summary = "Get a news illustration", method = "GET", description = "This gets the news illustration with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News returned"),
      @ApiResponse(responseCode = "401", description = "User not authorized to get the news"),
      @ApiResponse(responseCode = "404", description = "News not found"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response getNewsIllustration(@Context Request rsRequest,
                                      @Context HttpServletRequest request,
                                      @Parameter(description = "News id", required = true) @PathParam("id") String id,
                                      @Parameter(description = "last modified date") @QueryParam("v") long lastModified,
                                      @Parameter(description = "News object type to be fetched", required = false) @QueryParam("type") String newsObjectType,
                                      @Parameter(description = "resized image size") @QueryParam("size") String size) {
    try {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, false, newsObjectType);
      if (news == null || news.getIllustration() == null || news.getIllustration().length == 0) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      if (!news.isPublished()) {//TODO Check if necessary
        Space space = spaceService.getSpaceById(news.getSpaceId());
        if (space == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      }

      long lastUpdated = news.getIllustrationUpdateDate().getTime();
      EntityTag eTag = (size == null || size.isBlank()) ? new EntityTag(String.valueOf(lastUpdated)) : new EntityTag(lastUpdated+"-"+size);
      Response.ResponseBuilder builder = rsRequest.evaluatePreconditions(eTag);
      if (builder == null) {
        if (size != null) {
          //if there is no cache (browser cache or server cache with the etag), the image is resized
          //it can be improved a little by storing the thumbnail in the news.
          //in this case we need to add a security mechanism to prevent a user to generate one image for each (width,height) combination
          String[] dimension = size.split("x");
          byte[] thumbnail = thumbnailService.createCustomThumbnail(news.getIllustration(),
                                                                    Integer.parseInt(dimension[0]),
                                                                    Integer.parseInt(dimension[1]),
                                                                    news.getIllustrationMimeType());
          news.setIllustration(thumbnail);
        }
        builder = Response.ok(news.getIllustration(), news.getIllustrationMimeType());
      }

      if (lastModified > 0) {
        builder.lastModified(new Date(lastUpdated));
        builder.expires(new Date(System.currentTimeMillis() + CACHE_DURATION_MILLISECONDS));
        builder.cacheControl(ILLUSTRATION_CACHE_CONTROL);
      }
      builder.tag(eTag);
      return builder.build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("{id}/click")
  @RolesAllowed("users")
  @Operation(summary = "Log a click action on a news", method = "POST", description = "This logs a message when the user performs a click on a news")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Click logged"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response clickOnNews(@Context UriInfo uriInfo,
                              @Parameter(description = "News id", required = true) @PathParam("id") String id,
                              @Parameter(description = "The clicked element", required = true) String clickedElement) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);

    News news;
    try {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      news = newsService.getNewsById(id, currentIdentity, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
    } catch (Exception e) {
      LOG.error("Error while getting news with id " + id, e);
      return Response.serverError().build();
    }

    Space space = spaceService.getSpaceById(news.getSpaceId());

    LOG.info("service=news operation=click_on_{} parameters=\"news_id:{},space_name:{},space_id:{},user_id:{}\"",
             clickedElement,
             news.getId(),
             space != null ? space.getPrettyName() : "",
             space != null ? space.getId() : "",
             currentUser.getId());

    return Response.status(Response.Status.OK).build();
  }

  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Update a news", method = "PATCH", description = "This updates the sent fields of a news")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "News updated"),
      @ApiResponse(responseCode = "400", description = "Invalid query input"),
      @ApiResponse(responseCode = "401", description = "User not authorized to update the news"),
      @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response patchNews(@Context HttpServletRequest request,
                            @Parameter(description = "News id", required = true) @PathParam("id") String id,
                            @RequestBody(description = "News object", required = true) News updatedNews) {
    if (updatedNews == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    String authenticatedUser = request.getRemoteUser();
    try {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      //TODO Move to service layer
      if (updatedNews.isArchived() != news.isArchived()) {
        boolean canArchiveOrUnarchiveNews = currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
            || currentIdentity.getUserId().equals(news.getAuthor());
        if (!canArchiveOrUnarchiveNews) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        news.setArchived(updatedNews.isArchived());
        if (news.isArchived()) {
          newsService.archiveNews(id, currentIdentity.getUserId());
        } else {
          newsService.unarchiveNews(id, currentIdentity.getUserId());
        }
      }

      boolean isUpdatedTitle = (updatedNews.getTitle() != null) && !updatedNews.getTitle().equals(news.getTitle());
      boolean isUpdatedSummary = (updatedNews.getSummary() != null) && !updatedNews.getSummary().equals(news.getSummary());
      boolean isUpdatedBody = (updatedNews.getBody() != null) && !updatedNews.getBody().equals(news.getBody());
      boolean isUpdatedIllustration =
                                    (updatedNews.getUploadId() != null) && !updatedNews.getUploadId().equals(news.getUploadId());
      if (isUpdatedTitle || isUpdatedSummary || isUpdatedBody || isUpdatedIllustration) {
        if (!news.isCanEdit()) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (isUpdatedTitle) {
          news.setTitle(updatedNews.getTitle());
        }
        if (isUpdatedSummary) {
          news.setSummary(updatedNews.getSummary());
        }
        if (isUpdatedBody) {
          news.setBody(updatedNews.getBody());
        }
        if (isUpdatedIllustration) {
          news.setUploadId(updatedNews.getUploadId());
        }
        news = newsService.updateNews(news, authenticatedUser, null, updatedNews.isPublished());
      }

      return Response.ok(news).build();
    } catch (IllegalAccessException e) {
      LOG.warn("User '{}' is not autorized to patch news", authenticatedUser, e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when trying to update the news " + id, e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("canScheduleNews/{spaceId}")
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "check if the current user can schedule a news in the given space", method = "GET", description = "This checks if the current user can schedule a news in the given space")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User ability to schedule a news"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "User not authorized to schedule a news"),
          @ApiResponse(responseCode = "404", description = "Space not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response canScheduleNews(@Parameter(description = "space id", required = true) @PathParam("spaceId") String spaceId) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      if (StringUtils.isBlank(spaceId)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(String.valueOf(newsService.canScheduleNews(space, currentIdentity))).build();
    } catch (Exception e) {
      LOG.error("Error when checking if the authenticated user can schedule a news", e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("canPublishNews")
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @Operation(summary = "check if the current user can publish a news to all users", method = "GET", description = "This checks if the current user can publish a news to all users")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "User ability to publish a news is returned"),
          @ApiResponse(responseCode = "401", description = "User not authorized to publish a news")})
  public Response canPublishNews(@Parameter(description = "space id", required = true) @QueryParam("spaceId") String spaceId) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      if (!StringUtils.isBlank(spaceId)) {
        Space space = spaceService.getSpaceById(spaceId);
        if (space == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      }
      return Response.ok(String.valueOf(NewsUtils.canPublishNews(spaceId, currentIdentity))).build();
    } catch (Exception e) {
      LOG.error("Error when checking if the authenticated user can publish a news to all users", e);
      return Response.serverError().build();
    }
  }

  private NewsFilter buildFilter(List<String> spaces, String filter, String text, String author, int limit, int offset) {
    NewsFilter newsFilter = new NewsFilter();

    newsFilter.setSpaces(spaces);
    if (StringUtils.isNotEmpty(filter)) {
      FilterType filterType = FilterType.valueOf(filter.toUpperCase());
      switch (filterType) {
        case PINNED: {
          newsFilter.setPublishedNews(true);
          break;
        }

        case MYPOSTED: {
          if (StringUtils.isNotEmpty(author)) {
            newsFilter.setAuthor(author);
          }
          break;
        }

        case ARCHIVED: {
          newsFilter.setArchivedNews(true);
          break;
        }
        case DRAFTS: {
          if (StringUtils.isNotEmpty(author)) {
            newsFilter.setAuthor(author);
          }
          newsFilter.setDraftNews(true);
          break;
        }
        case SCHEDULED: {
          if (StringUtils.isNotEmpty(author)) {
            newsFilter.setAuthor(author);
          }
          newsFilter.setScheduledNews(true);
          break;
        }
      }
      if ("DRAFTS".equals(filterType.toString())) {
        newsFilter.setOrder("exo:dateModified");
      } else {
        newsFilter.setOrder("publication:liveDate");
      }
    }
    // Set text to search news with
    if (StringUtils.isNotEmpty(text) && text.indexOf("#") != 0) {
      newsFilter.setSearchText(text);
    }
    newsFilter.setLimit(limit);
    newsFilter.setOffset(offset);

    return newsFilter;
  }
}
