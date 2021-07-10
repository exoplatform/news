package org.exoplatform.news.rest;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.deprecation.DeprecatedAPI;
import org.exoplatform.news.NewsAttachmentsService;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.filter.NewsFilter;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.NewsAttachment;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.news.search.NewsESSearchResult;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.PATCH;

import org.exoplatform.social.rest.api.EntityBuilder;

import org.picocontainer.Startable;

@Path("v1/news")
@Api(tags = "v1/news", value = "v1/news", description = "Managing news")
public class NewsRestResourcesV1 implements ResourceContainer, Startable {

  private static final Log       LOG                             = ExoLogger.getLogger(NewsRestResourcesV1.class);

  private static final String    PUBLISHER_MEMBERSHIP_NAME       = "publisher";

  private final static String    PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private final static String    PLATFORM_ADMINISTRATORS_GROUP = "/platform/administrators";

  private NewsService            newsService;

  private NewsAttachmentsService newsAttachmentsService;

  private SpaceService           spaceService;

  private IdentityManager        identityManager;

  private ScheduledExecutorService scheduledExecutor;

  private PortalContainer        container;

  private Map<String, String>    newsToDeleteQueue = new HashMap<>();

  private enum FilterType {
    PINNED, MYPOSTED, ARCHIVED, DRAFTS, SCHEDULED, ALL
  }

  public NewsRestResourcesV1(NewsService newsService,
                             NewsAttachmentsService newsAttachmentsService,
                             SpaceService spaceService,
                             IdentityManager identityManager,
                             PortalContainer container) {

    this.newsService = newsService;
    this.newsAttachmentsService = newsAttachmentsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.container = container;
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

    String authenticatedUser = request.getRemoteUser();
    Space space = spaceService.getSpaceById(news.getSpaceId());
    try {
      if (!canCreateNews(authenticatedUser, space)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      News createdNews;
      if (PublicationDefaultStates.PUBLISHED.equals(news.getPublicationState())) {
        createdNews = newsService.createNews(news);
      } else {
        createdNews = newsService.createNewsDraft(news);
      }

      return Response.ok(createdNews).build();
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return Response.serverError().build();
    }
  }

  @Path("schedule")
  @PATCH
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
    try {
      News news = newsService.getNewsById(scheduledNews.getId(), false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (!news.isCanEdit()) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      news = newsService.scheduleNews(scheduledNews);
      return Response.ok(news).build();
    } catch (Exception e) {
      LOG.error("Error when scheduling the news " + scheduledNews.getTitle(), e);
      return Response.serverError().build();
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
    try {
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
          if (!canViewNews(authenticatedUser, space)) {
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
        news = newsService.getNews(newsFilter);
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
      return Response.serverError().build();
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
                         @ApiParam(value = "Term to search", required = true) @QueryParam("query") String query,
                         @ApiParam(value = "Properties to expand", required = false) @QueryParam("expand") String expand,
                         @ApiParam(value = "Offset", required = false, defaultValue = "0") @QueryParam("offset") int offset,
                         @ApiParam(value = "Limit", required = false, defaultValue = "20") @QueryParam("limit") int limit) throws Exception {

    if (StringUtils.isBlank(query)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'query' parameter is mandatory").build();
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = CommonsUtils.getService(IdentityManager.class)
                                       .getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser);

    if (offset < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Offset must be 0 or positive").build();
    }
    if (limit < 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Limit must be positive").build();
    }
    List<NewsESSearchResult> searchResults = newsService.search(currentUser, query, offset, limit);
    List<NewsSearchResultEntity> results = searchResults.stream().map(searchResult -> {
      NewsSearchResultEntity entity = new NewsSearchResultEntity(searchResult);
      entity.setPoster(EntityBuilder.buildEntityIdentity(searchResult.getPoster(), uriInfo.getPath(), "all"));
      return entity;
    }).collect(Collectors.toList());

    return Response.ok(results).build();
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
    try {

      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      String authenticatedUser = request.getRemoteUser();
      News news = newsService.getNewsById(id, editMode);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Space newsSpace = spaceService.getSpaceById(news.getSpaceId());
      if (StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.STAGED)
          && !canViewScheduledNews(authenticatedUser, newsSpace, news)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      if (!news.isPinned() && StringUtils.equals(news.getPublicationState(), PublicationDefaultStates.PUBLISHED)
          && !canViewNews(authenticatedUser, newsSpace)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      
      news.setIllustration(null);

      if (StringUtils.isNotEmpty(fields) && fields.equals("spaces")) {
        News filteredNews = new News();
        Set<Space> spacesList = new HashSet<>();
        String newsActivities = news.getActivities();
        for (String act : newsActivities.split(";")) {
          String spaceId = act.split(":")[0];
          Space space = spaceService.getSpaceById(spaceId);
          spacesList.add(space);
        }
        filteredNews.setSharedInSpacesList(spacesList);
        return Response.ok(filteredNews).build();
      } else {
        return Response.ok(news).build();
      }
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().build();
    }
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
      return Response.serverError().build();
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
      return Response.serverError().build();
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
  public Response updateNews(@Context HttpServletRequest request,
                             @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                             @ApiParam(value = "News", required = true) News updatedNews) {

    if (updatedNews == null) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    try {
      News news = newsService.getNewsById(id, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      if (!news.isCanEdit()) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      news.setTitle(updatedNews.getTitle());
      news.setSummary(updatedNews.getSummary());
      news.setBody(updatedNews.getBody());
      news.setUploadId(updatedNews.getUploadId());
      news.setAttachments(updatedNews.getAttachments());
      news.setPublicationState(updatedNews.getPublicationState());

      if (updatedNews.isPinned() != news.isPinned()) {
        org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
        boolean canPinOrUnpinNews = currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME) ||
                currentIdentity.isMemberOf(PLATFORM_ADMINISTRATORS_GROUP, "*");

        if (!canPinOrUnpinNews) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        news.setPinned(updatedNews.isPinned());
        if (news.isPinned()) {
          newsService.pinNews(id);
        } else {
          newsService.unpinNews(id);
        }
      }

      news = newsService.updateNews(news);

      return Response.ok(news).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().build();
    }
  }

  @POST
  @Path("{id}/share")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "share a news", httpMethod = "POST", response = Response.class, notes = "This shares a news to a list of spaces if the authenticated user is a member of these spaces or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News shared"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to share the news"),
      @ApiResponse(code = 404, message = "News not found"),
      @ApiResponse(code = 500, message = "Internal server error")
  })
  @DeprecatedAPI(value = "Not needed endpoint and should be purged after AS migration", insist = true)
  public Response shareNews(@Context HttpServletRequest request,
                            @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                            @ApiParam(value = "Shared News", required = true) SharedNews sharedNews) {

    try {
      News news = newsService.getNewsById(id, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      sharedNews.setNewsId(id);

      if (sharedNews == null || sharedNews.getSpacesNames() == null || sharedNews.getSpacesNames().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      String authenticatedUser = request.getRemoteUser();

      List<Space> spaces = new ArrayList<>();

      boolean superManager = spaceService.isSuperManager(authenticatedUser);
      for (String spaceName : sharedNews.getSpacesNames()) {
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        if (space == null) {
          return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
          if (!superManager && !spaceService.isMember(space, authenticatedUser)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
          } else {
            spaces.add(space);
          }
        }
      }

      sharedNews.setPoster(authenticatedUser);

      newsService.shareNews(sharedNews, spaces);

      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Error when sharing the news " + id, e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{id}/illustration")
  @RolesAllowed("users")
  @ApiOperation(value = "Get a news illustration", httpMethod = "GET", response = Response.class, notes = "This gets the news illustration with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News returned"),
      @ApiResponse(code = 401, message = "User not authorized to get the news"),
      @ApiResponse(code = 404, message = "News not found"), @ApiResponse(code = 500, message = "Internal server error") })
  public Response getNewsIllustration(@Context Request request,
                                      @ApiParam(value = "News id", required = true) @PathParam("id") String id) {
    try {
      News news = newsService.getNewsById(id, false);
      if (news == null || news.getIllustration() == null || news.getIllustration().length == 0) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      if (!news.isPinned()) {
        Space space = spaceService.getSpaceById(news.getSpaceId());
        if (space == null) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
      }

      EntityTag eTag = new EntityTag(String.valueOf(news.getIllustrationUpdateDate().getTime()));
      //
      Response.ResponseBuilder builder = (eTag == null ? null : request.evaluatePreconditions(eTag));
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
      news = newsService.getNewsById(id, false);
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

    try {
      News news = newsService.getNewsById(id, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();

      if (updatedNews.isPinned() != news.isPinned()) {
        boolean canPinOrUnpinNews = currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME) ||
                currentIdentity.isMemberOf(PLATFORM_ADMINISTRATORS_GROUP, "*");

        if (!canPinOrUnpinNews) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        news.setPinned(updatedNews.isPinned());
        if (news.isPinned()) {
          newsService.pinNews(id);
        } else {
          newsService.unpinNews(id);
        }
      }

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
        newsService.updateNews(news);
      }

      return Response.ok().build();

    } catch (Exception e) {
      LOG.error("Error when trying to update the news " + id, e);
      return Response.serverError().build();
    }
  }
  
  @POST
  @Path("{id}/view")
  @RolesAllowed("users")
  @ApiOperation(value = "Update a news", httpMethod = "PUT", response = Response.class, notes = "This increments the views number of a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News updated"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to update the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response viewNews(@Context HttpServletRequest request,
                           @ApiParam(value = "News id", required = true) @PathParam("id") String id) {

    try {
      News news = newsService.getNewsById(id, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
      if (!news.isPinned()) {
        Space space = spaceService.getSpaceById(news.getSpaceId());
        if (StringUtils.isBlank(authenticatedUser) || !canViewNews(authenticatedUser, space)) {
          return Response.status(Response.Status.UNAUTHORIZED).build();
        }
      }
      newsService.markAsRead(news, authenticatedUser);
      return Response.ok().build();

    } catch (Exception e) {
      LOG.error("Error when trying to update the news " + id, e);
      return Response.serverError().build();
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
    try {
      if (StringUtils.isBlank(id)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      News news = newsService.getNewsById(id, false);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = request.getRemoteUser();

      if (!news.isCanDelete()) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      if (delay > 0) {
        newsToDeleteQueue.put(id, authenticatedUser);
        scheduledExecutor.schedule(() -> {
          if (newsToDeleteQueue.containsKey(id)) {
            ExoContainerContext.setCurrentContainer(container);
            RequestLifeCycle.begin(container);
            try {
              newsToDeleteQueue.remove(id);
              newsService.deleteNews(id, isDraft);
            } catch (IllegalAccessException e) {
              LOG.error("User '{}' attempts to delete a non authorized news", authenticatedUser, e);
            } catch (Exception e) {
              LOG.warn("Error when deleting a news", e);
            } finally {
              RequestLifeCycle.end();
            }
          }
        }, delay, TimeUnit.SECONDS);
      } else {
        newsToDeleteQueue.remove(id);
        newsService.deleteNews(id, isDraft);
      }
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Error when deleting the news with id " + id, e);
      return Response.serverError().build();
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
    if (newsToDeleteQueue.containsKey(id)) {
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
  @Path("canCreateNews/{spaceId}")
  @Produces(MediaType.TEXT_PLAIN)
  @RolesAllowed("users")
  @ApiOperation(value = "check if the current user can create a news in the given space", httpMethod = "GET", response = Response.class, notes = "This checks if the current user can create a news in the given space", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News deleted"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to create a news"),
      @ApiResponse(code = 404, message = "Space not found"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response canCreateNews(@Context HttpServletRequest request,
                                @ApiParam(value = "space id", required = true) @PathParam("spaceId") String spaceId) {
    try {
      if (StringUtils.isBlank(spaceId)) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }
      Space space = spaceService.getSpaceById(spaceId);
      if (space == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = request.getRemoteUser();

      if (StringUtils.isBlank(authenticatedUser) || !canViewNews(authenticatedUser, space)) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      return Response.ok(String.valueOf(canCreateNews(authenticatedUser, space))).build();
    } catch (Exception e) {
      LOG.error("Error when checking if the authenticated user can create a news", e);
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
          newsFilter.setPinnedNews(true);
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
  
  private boolean canCreateNews(String authenticatedUser, Space space) throws Exception {
    return space != null && 
          (spaceService.isSuperManager(authenticatedUser) || spaceService.isManager(space, authenticatedUser) || spaceService.isRedactor(space, authenticatedUser) ||
          spaceService.isMember(space, authenticatedUser) && ArrayUtils.isEmpty(space.getRedactors()));
  }
  
  private boolean canViewNews(String authenticatedUser, Space space) throws Exception {
    return spaceService.isSuperManager(authenticatedUser) || (space != null && spaceService.isMember(space, authenticatedUser));
  }

  private boolean canViewScheduledNews(String authenticatedUser, Space space, News news) {
    return StringUtils.equals(news.getAuthor(), authenticatedUser) || (space != null
        && (spaceService.isManager(space, authenticatedUser) || spaceService.isRedactor(space, authenticatedUser)));
  }
}
