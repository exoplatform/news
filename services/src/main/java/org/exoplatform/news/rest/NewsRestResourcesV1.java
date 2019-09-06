package org.exoplatform.news.rest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.news.NewsService;
import org.exoplatform.news.model.News;
import org.exoplatform.news.model.SharedNews;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Path("v1/news")
@Api(tags = "v1/news", value = "v1/news", description = "Managing news")
public class NewsRestResourcesV1 implements ResourceContainer {

  private static final Log LOG = ExoLogger.getLogger(NewsRestResourcesV1.class);
  
  private static final String REDACTOR_MEMBERSHIP_NAME = "redactor";

  private static final String MANAGER_MEMBERSHIP_NAME = "manager";

  private static final String PUBLISHER_MEMBERSHIP_NAME = "publisher";

  private final static String PLATFORM_WEB_CONTRIBUTORS_GROUP = "/platform/web-contributors";

  private NewsService newsService;

  private SpaceService spaceService;

  private IdentityManager identityManager;

  private ActivityManager activityManager;

  public NewsRestResourcesV1(NewsService newsService, SpaceService spaceService, IdentityManager identityManager, ActivityManager activityManager) {
    this.newsService = newsService;
    this.spaceService = spaceService;
    this.identityManager = identityManager;
    this.activityManager=activityManager;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Create a news",
          httpMethod = "POST",
          response = Response.class,
          notes = "This creates the news if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "News created"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 401, message = "User not authorized to create the news"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response createNews(@ApiParam(value = "News", required = true) News news) {
    if (news == null || StringUtils.isEmpty(news.getSpaceId())) {
      return Response.status(Response.Status.BAD_REQUEST).build();
    }

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();

    Space space = spaceService.getSpaceById(news.getSpaceId());
    if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      News createdNews = newsService.createNews(news);

      return Response.ok(createdNews).build();
    } catch (Exception e) {
      LOG.error("Error when creating the news " + news.getTitle(), e);
      return Response.serverError().build();
    }
  }

  @GET
  @Path("{id}")
  @RolesAllowed("users")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get a news",
          httpMethod = "GET",
          response = Response.class,
          notes = "This gets the news with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "News returned"),
          @ApiResponse (code = 401, message = "User not authorized to get the news"),
          @ApiResponse (code = 404, message = "News not found"),
          @ApiResponse (code = 500, message = "Internal server error") })
  public Response getNews(@Context HttpServletRequest request,
                          @ApiParam(value = "News id", required = true) @PathParam("id") String id) {
    try {
      News news = newsService.getNews(id);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = request.getRemoteUser();

      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if(news.getIllustration() != null && news.getIllustration().length > 0) {
        news.setIllustrationURL("/portal/rest/v1/news/" + news.getId() + "/illustration");
      } else {
        news.setIllustrationURL(null);
      }
      // do not send the illustration by default since it can be heavy
      news.setIllustration(null);

      return Response.ok(news).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news " + id, e);
      return Response.serverError().build();
    }
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Create a news",
          httpMethod = "PUT",
          response = Response.class,
          notes = "This updates the news if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "News updated"),
          @ApiResponse (code = 400, message = "Invalid query input"),
          @ApiResponse (code = 401, message = "User not authorized to update the news"),
          @ApiResponse (code = 500, message = "Internal server error")})
  public Response updateNews(@Context HttpServletRequest request,
                             @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                             @ApiParam(value = "News", required = true) News updatedNews) {
    if (updatedNews == null) {
      Response.status(Response.Status.BAD_REQUEST).build();
    }

    try {
      News news = newsService.getNews(id);
      if(news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = request.getRemoteUser();

      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      news.setTitle(updatedNews.getTitle());
      news.setSummary(updatedNews.getSummary());
      news.setBody(updatedNews.getBody());
      news.setUploadId(updatedNews.getUploadId());

      newsService.updateNews(news);

      return Response.ok().build();
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
          @ApiResponse (code = 404, message = "News not found"),
          @ApiResponse(code = 500, message = "Internal server error") })
  public Response shareNews(@Context HttpServletRequest request,
                            @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                            @ApiParam(value = "Shared News", required = true) SharedNews sharedNews) {

    try {
      News news = newsService.getNews(id);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }
      sharedNews.setNewsId(id);

      if(sharedNews == null || sharedNews.getSpacesNames() == null || sharedNews.getSpacesNames().isEmpty()) {
        return Response.status(Response.Status.BAD_REQUEST).build();
      }

      String authenticatedUser = request.getRemoteUser();

      List<Space> spaces = new ArrayList<>();

      boolean superManager = spaceService.isSuperManager(authenticatedUser);
      for(String spaceName : sharedNews.getSpacesNames()) {
        Space space = spaceService.getSpaceByPrettyName(spaceName);
        if (space == null) {
          return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
          if(!superManager && !spaceService.isMember(space, authenticatedUser)) {
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
  @ApiOperation(value = "Get a news illustration",
          httpMethod = "GET",
          response = Response.class,
          notes = "This gets the news illustration with the given id if the authenticated user is a member of the space or a spaces super manager.")
  @ApiResponses(value = {
          @ApiResponse (code = 200, message = "News returned"),
          @ApiResponse (code = 401, message = "User not authorized to get the news"),
          @ApiResponse (code = 404, message = "News not found"),
          @ApiResponse (code = 500, message = "Internal server error") })
  public Response getNewsIllustration(@Context Request request,
                                      @ApiParam(value = "News id", required = true) @PathParam("id") String id) {
    try {
      News news = newsService.getNews(id);
      if (news == null || news.getIllustration() == null || news.getIllustration().length == 0) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();

      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null || (! spaceService.isMember(space, authenticatedUser) && ! spaceService.isSuperManager(authenticatedUser))) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
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
  @ApiOperation(value = "Log a click action on a news",
          httpMethod = "POST",
          response = Response.class,
          notes = "This logs a message when the user performs a click on a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Click logged"),
                          @ApiResponse(code = 400, message = "Invalid query input"),
                          @ApiResponse(code = 500, message = "Internal server error")})
  public Response clickOnNews(@Context UriInfo uriInfo,
                              @ApiParam(value = "News id", required = true) @PathParam("id") String id,
                              @ApiParam(value = "The clicked element", required = true) String clickedElement) {

    String authenticatedUser = ConversationState.getCurrent().getIdentity().getUserId();
    Identity currentUser = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, authenticatedUser, false);

    News news;
    try {
      news = newsService.getNews(id);
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
  
  @PUT
  @Path("{id}/pin")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Pin a news", httpMethod = "PUT", response = Response.class, notes = "This pins a news by updating the isPinned attribute of a news")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "News pinned"),
      @ApiResponse(code = 400, message = "Invalid query input"),
      @ApiResponse(code = 401, message = "User not authorized to pin the news"),
      @ApiResponse(code = 500, message = "Internal server error") })
  public Response pinNews(@Context HttpServletRequest request,
                          @ApiParam(value = "Node news id", required = true) @PathParam("id") String id) {

    try {
      News news = newsService.getNews(id);
      if (news == null) {
        return Response.status(Response.Status.NOT_FOUND).build();
      }

      String authenticatedUser = request.getRemoteUser();
      Space space = spaceService.getSpaceById(news.getSpaceId());
      if (space == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      boolean canPinNews = currentIdentity.isMemberOf(space.getGroupId(), REDACTOR_MEMBERSHIP_NAME)
          || currentIdentity.isMemberOf(space.getGroupId(), MANAGER_MEMBERSHIP_NAME)
          || currentIdentity.isMemberOf(PLATFORM_WEB_CONTRIBUTORS_GROUP, PUBLISHER_MEMBERSHIP_NAME)
          || spaceService.isSuperManager(authenticatedUser);
      if (!canPinNews) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      if (!news.isPinned()) {
        newsService.pinNews(id);
      }
      return Response.ok().build();

    } catch (Exception e) {
      LOG.error("Error when trying to pin the news " + id, e);
      return Response.serverError().build();
    }
  }

}
