
package org.exoplatform.news.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.utils.NewsUtils;
import org.exoplatform.social.metadata.favorite.FavoriteService;
import org.picocontainer.Startable;

import org.exoplatform.common.http.HTTPStatus;
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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.swagger.annotations.*;
import io.swagger.jaxrs.PATCH;

@Path("v1/news")
@Api(tags = "v1/news", value = "v1/news", description = "Managing news")
public class NewsRestResourcesV1 implements ResourceContainer, Startable {

  private static final Log         LOG                             = ExoLogger.getLogger(NewsRestResourcesV1.class);

  private static final String      PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String      PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private NewsService              newsService;

  private NewsAttachmentsStorage   newsAttachmentsService;

  private SpaceService             spaceService;

  private IdentityManager          identityManager;

  private ScheduledExecutorService scheduledExecutor;

  private PortalContainer          container;

  private FavoriteService          favoriteService;

  private Map<String, String>      newsToDeleteQueue               = new HashMap<>();

  private enum FilterType {
    PINNED, MYPOSTED, ARCHIVED, DRAFTS, SCHEDULED, ALL
  }

  public NewsRestResourcesV1(NewsService newsService,
                             NewsAttachmentsStorage newsAttachmentsService,
                             SpaceService spaceService,
                             IdentityManager identityManager,
                             PortalContainer container,
                             FavoriteService favoriteService) {

    this.newsService = newsService;
    this.newsAttachmentsService = newsAttachmentsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.container = container;
    this.favoriteService = favoriteService;
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
  @ApiOperation(value = "Create a news", httpMethod = "POST", response = Response.class, notes = "This creates the news if the authenticated user is a member of the space or a spaces super manager. The news is created in draft status, unless the publicationState property is set to 'published'.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News created"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to create the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response createNews(@Context HttpServletRequest request, @ApiParam(value = "News", required = true) News news) {
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
  @ApiOperation(value = "check if the current user can create a news in the given space", httpMethod = "GET", response = Response.class, notes = "This checks if the current user can create a news in the given space", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "User ability to create a news is returned"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to create a news"),
      @ApiResponse(code = 404, message = "Space not found"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response canCreateNews(@Context HttpServletRequest request,
                                @ApiParam(value = "space id", required = true) @PathParam("spaceId") String spaceId) {
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
  @ApiOperation(value = "Create a news", httpMethod = "PUT", response = Response.class, notes = "This updates the news if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News updated"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to update the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response updateNews(@ApiParam(value = "News id", required = true) @PathParam("id") String id,
                             @ApiParam(value = "Post news", required = false) @QueryParam("post") boolean post,
                             @ApiParam(value = "News", required = true) News updatedNews) {

    if (updatedNews == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      News news = newsService.getNewsById(id, currentIdentity, false);
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
      news.setDraftVisible(updatedNews.isDraftVisible());
      news.setActivityPosted(updatedNews.isActivityPosted());
      news.setTargets(updatedNews.getTargets());


      news = newsService.updateNews(news, currentIdentity.getUserId(), post, updatedNews.isPublished());

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
  @ApiOperation(value = "Delete news", httpMethod = "DELETE", response = Response.class, notes = "This deletes the news", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News deleted"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to delete the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response deleteNews(@Context HttpServletRequest request,
                             @ApiParam(value = "News id", required = true)
                             @PathParam("id") String id,
                             @ApiParam(value = "Is draft to delete", defaultValue = "false") @QueryParam("isDraft") boolean isDraft,
                             @ApiParam(value = "Time to effectively delete news", required = false)
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
  @ApiOperation(
      value = "Undo deleting news if not yet effectively deleted.",
      httpMethod = "POST",
      response = Response.class
  )
  @ApiResponses(
      value = {
          @ApiResponse(code = HTTPStatus.NO_CONTENT, message = "Request fulfilled"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.FORBIDDEN, message = "Forbidden operation"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), }
  )
  public Response undoDeleteNews(
                                  @Context HttpServletRequest request,
                                  @ApiParam(value = "News node identifier", required = true)
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
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get a news", httpMethod = "GET", response = Response.class, notes = "This gets the news with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsById(@Context HttpServletRequest request,
                              @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "fields", required = true) @QueryParam("fields") String fields,
                              @ApiParam(value = "Is edit mode", defaultValue = "false") @QueryParam("editMode") boolean editMode) {
    String authenticatedUser = request.getRemoteUser();
    try {
      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, editMode);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      news.setIllustration(null);
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
  @ApiOperation(value = "mark a news article as read", httpMethod = "POST", response = Response.class, notes = "This marks a news article as read by the user who accessed its details.")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Request fulfilled"),
          @ApiResponse(code = 401, message = "User not authorized to get the news"),
          @ApiResponse(code = 404, message = "News not found"),
          @ApiResponse(code = 500, message = "Internal server error")})

  public Response markNewsAsRead(@Context HttpServletRequest request,
                                 @ApiParam(value = "News id", required = true) @PathParam("id") String id) {
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
  @ApiOperation(value = "Get news list", httpMethod = "GET", response = Response.class, notes = "This gets the list of news with the given search text, of the given author, in the given space or spaces, with the given publication state, with the given pinned state if the authenticated user is a member of the spaces or a super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News list returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news list"),
      @ApiResponse(code = 404, message = "News list not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNews(@Context HttpServletRequest request,
                          @ApiParam(value = "News author", required = true) @QueryParam("author") String author,
                          @ApiParam(value = "News spaces", required = true) @QueryParam("spaces") String spaces,
                          @ApiParam(value = "News filter", required = true) @QueryParam("filter") String filter,
                          @ApiParam(value = "search text", required = true) @QueryParam("text") String text,
                          @ApiParam(value = "News pagination offset", defaultValue = "0") @QueryParam("offset") int offset,
                          @ApiParam(value = "News pagination limit", defaultValue = "10") @QueryParam("limit") int limit,
                          @ApiParam(value = "News total size", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
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
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get news list", httpMethod = "GET", response = Response.class, notes = "This gets the list of news by the given target.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News list returned"),
          @ApiResponse(code = 401, message = "User not authorized to get the news list"),
          @ApiResponse(code = 404, message = "News list not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsByTarget(@Context HttpServletRequest request,
                                  @ApiParam(value = "News target name", required = true) @PathParam("targetName") String targetName,
                                  @ApiParam(value = "News pagination offset", defaultValue = "0") @QueryParam("offset") int offset,
                                  @ApiParam(value = "News pagination limit", defaultValue = "10") @QueryParam("limit") int limit,
                                  @ApiParam(value = "News total size", defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {
    try {
      String authenticatedUser = request.getRemoteUser();
      if (StringUtils.isBlank(authenticatedUser)) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
      }
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
  @ApiOperation(value = "Get a news identified by its activity or shared activity identifier", httpMethod = "GET", response = Response.class, notes = "This gets the news with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"),
      @ApiResponse(code = 500, message = "Internal server error")
  })
  public Response getNewsByActivityId(@ApiParam(value = "Activity id", required = true) @PathParam("activityId") String activityId) {
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
  @ApiOperation(value = "Schedule a news", httpMethod = "POST", response = Response.class, notes = "This schedules the news if the authenticated user is a member of the space or a spaces super manager. The news is created in staged status, after reaching a date of publication startPublishedDate, the publicationState property is set to 'published'.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News scheduled"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "User not authorized to schedule the news"),
          @ApiResponse(code = 500, message = "Internal server error") })
  public Response scheduleNews(@Context HttpServletRequest request,
                               @ApiParam(value = "News", required = true) News scheduledNews) {
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
  @ApiOperation(value = "Search the list of news available with query", httpMethod = "GET", response = Response.class, produces = "application/json")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
      @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
      @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"), })
  public Response search(@Context UriInfo uriInfo,
                         @Context HttpServletRequest request,
                         @ApiParam(value = "Term to search", required = true) @QueryParam("query") String query,
                         @ApiParam(value = "Properties to expand", required = false) @QueryParam("expand") String expand,
                         @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                         @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit,
                         @ApiParam(value = "Favorites", required = false, defaultValue = "false") @QueryParam("favorites") boolean favorites) {

    if (StringUtils.isBlank(query) && !favorites) {
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
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get a news attachment", httpMethod = "GET", response = Response.class, notes = "This gets the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsAttachmentById(@Context HttpServletRequest request,
                                        @ApiParam(value = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
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
  @ApiOperation(value = "Download a news attachment", httpMethod = "GET", response = Response.class, notes = "This downloads the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsAttachmentBinaryById(@Context HttpServletRequest request,
                                              @ApiParam(value = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
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
  @ApiOperation(value = "Opens a news attachment", httpMethod = "GET", response = Response.class, notes = "This opens the news attachment with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response openNewsAttachmentById(@Context HttpServletRequest request,
                                         @ApiParam(value = "News attachment id", required = true) @PathParam("attachmentId") String attachmentId) {
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
  @RolesAllowed("users")
  @ApiOperation(value = "Get a news illustration", httpMethod = "GET", response = Response.class, notes = "This gets the news illustration with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsIllustration(@Context Request rsRequest,
                                      @Context HttpServletRequest request,
                                      @ApiParam(value = "News id", required = true) @PathParam("id") String id) {
    try {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      News news = newsService.getNewsById(id, currentIdentity, false);
      if (news == null || news.getIllustration() == null || news.getIllustration().length == 0) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      if (!news.isPublished()) {//TODO Check if necessary
        Space space = spaceService.getSpaceById(news.getSpaceId());
        if (space == null) {
          return Response.status(Response.Status.NOT_FOUND).build();
        }
      }

      EntityTag eTag = new EntityTag(String.valueOf(news.getIllustrationUpdateDate().getTime()));
      //
      Response.ResponseBuilder builder = (eTag == null ? null : rsRequest.evaluatePreconditions(eTag));
      if (builder == null) {
        builder = Response.ok(news.getIllustration(), "image/png");
        builder.tag(eTag);
      }

      return builder.build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("{id}/click")
  @RolesAllowed("users")
  @ApiOperation(value = "Log a click action on a news", httpMethod = "POST", response = Response.class, notes = "This logs a message when the user performs a click on a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Click logged"),
      @ApiResponse(code = 400, message = "Invalid query input"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response clickOnNews(@Context UriInfo uriInfo,
                              @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "The clicked element", required = true) String clickedElement) {

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
  @ApiOperation(value = "Update a news", httpMethod = "PATCH", response = Response.class, notes = "This updates the sent fields of a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News updated"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to update the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response patchNews(@Context HttpServletRequest request,
                            @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                            @ApiParam(value = "News", required = true) News updatedNews) {
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
          newsService.archiveNews(id);
        } else {
          newsService.unarchiveNews(id);
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
  @ApiOperation(value = "check if the current user can schedule a news in the given space", httpMethod = "GET", response = Response.class, notes = "This checks if the current user can schedule a news in the given space", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "User ability to schedule a news"),
          @ApiResponse(code = 400, message = "Invalid query input"),
          @ApiResponse(code = 401, message = "User not authorized to schedule a news"),
          @ApiResponse(code = 404, message = "Space not found"),
          @ApiResponse(code = 500, message = "Internal server error") })
  public Response canScheduleNews(@ApiParam(value = "space id", required = true) @PathParam("spaceId") String spaceId) {
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
  @ApiOperation(value = "check if the current user can publish a news to all users", httpMethod = "GET", response = Response.class, notes = "This checks if the current user can publish a news to all users", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "User ability to publish a news is returned"),
          @ApiResponse(code = 401, message = "User not authorized to publish a news")})
  public Response canPublishNews(@Context HttpServletRequest request) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      return Response.ok(String.valueOf(NewsUtils.canPublishNews(currentIdentity))).build();
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
    }
    // Set text to search news with
    if (StringUtils.isNotEmpty(text)) {
      newsFilter.setSearchText(text);
    }

    newsFilter.setOrder("exo:dateModified");
    newsFilter.setLimit(limit);
    newsFilter.setOffset(offset);

    return newsFilter;
  }
}
