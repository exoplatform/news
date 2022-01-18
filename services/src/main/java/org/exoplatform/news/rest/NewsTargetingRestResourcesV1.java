/*
 * Copyright (C) 2021 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.news.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.picocontainer.Startable;

@Path("v1/news/targeting")
@Api(tags = "v1/news/targeting", value = "v1/news/targeting")
public class NewsTargetingRestResourcesV1 implements ResourceContainer, Startable {

  private static final Log         LOG                     = ExoLogger.getLogger(NewsTargetingRestResourcesV1.class);

  private NewsTargetingService     newsTargetingService;

  private ScheduledExecutorService scheduledExecutor;

  private PortalContainer          container;

  private Map<String, String>      newsTargetToDeleteQueue = new HashMap<>();


  public NewsTargetingRestResourcesV1(NewsTargetingService newsTargetingService, PortalContainer container) {
    this.newsTargetingService = newsTargetingService;
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get all news targets by a giving type", httpMethod = "GET", response = Response.class, produces = "application/json")
  @ApiResponses(value = {
    @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
    @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") 
  })
  public Response getTargets(@Context HttpServletRequest request) {
    try {
      List<NewsTargetingEntity> targets = newsTargetingService.getTargets();
      return Response.ok(targets).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news targets", e);
      return Response.serverError().build();
    }
  }

  @Path("referenced")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Get all news targets by a giving property", httpMethod = "GET", response = Response.class, produces = "application/json")
  @ApiResponses(value = { 
    @ApiResponse(code = HTTPStatus.OK, message = "Request fulfilled"),
    @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error"),
    @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "Unauthorized operation")
  })
  public Response getReferencedTargets(@Context HttpServletRequest request) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      List<NewsTargetingEntity> referencedTargets = newsTargetingService.getReferencedTargets(currentIdentity);
      return Response.ok(referencedTargets).build();
    } catch (IllegalArgumentException e) {
      LOG.warn("User '{}' is not autorized to get referenced news targets", currentIdentity.getUserId(), e);
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (Exception e) {
      LOG.error("Error when getting the news referenced targets", e);
      return Response.serverError().build();
    }
  }

  @DELETE
  @Path("{targetName}")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @ApiOperation(value = "Delete news target", httpMethod = "DELETE", response = Response.class, notes = "This deletes the target news", consumes = "application/json")
  @ApiResponses(value = { @ApiResponse(code = HTTPStatus.OK, message = "News target deleted"),
          @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
          @ApiResponse(code = HTTPStatus.UNAUTHORIZED, message = "User not authorized to delete the news target"),
          @ApiResponse(code = HTTPStatus.INTERNAL_ERROR, message = "Internal server error") })
  public Response deleteTarget(@Context HttpServletRequest request,
                                     @ApiParam(value = "Target name", required = true)
                                     @PathParam("targetName") String targetName,
                                     @ApiParam(value = "Time to effectively delete news target", required = false)
                                     @QueryParam("delay") long delay) {
    org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
    try {
      if (StringUtils.isBlank(targetName)) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Target name ist mandatory").build();
      }
      if (delay > 0) {
        newsTargetToDeleteQueue.put(targetName, currentIdentity.getUserId());
        scheduledExecutor.schedule(() -> {
          if (newsTargetToDeleteQueue.containsKey(targetName)) {
            ExoContainerContext.setCurrentContainer(container);
            RequestLifeCycle.begin(container);
            try {
              newsTargetToDeleteQueue.remove(targetName);
              newsTargetingService.deleteTargetByName(targetName, currentIdentity);
            } catch (Exception e) {
              LOG.error("Error when deleting the news target with name " + targetName, e);
            } finally {
              RequestLifeCycle.end();
            }
          }
        }, delay, TimeUnit.SECONDS);
      } else {
        newsTargetToDeleteQueue.remove(targetName);
        newsTargetingService.deleteTargetByName(targetName, currentIdentity);
      }
      return Response.ok().build();
    } catch (Exception e) {
      LOG.error("Error when deleting the news target with name " + targetName, e);
      return Response.serverError().entity(e.getMessage()).build();
    }
  }

  @Path("{targetName}/undoDelete")
  @POST
  @RolesAllowed("users")
  @ApiOperation(
          value = "Undo deleting news target if not yet effectively deleted.",
          httpMethod = "POST",
          response = Response.class
  )
  @ApiResponses(
          value = {
                  @ApiResponse(code = HTTPStatus.BAD_REQUEST, message = "Invalid query input"),
                  @ApiResponse(code = HTTPStatus.FORBIDDEN, message = "Forbidden operation"), }
  )
  public Response undoDeleteTarget(@Context HttpServletRequest request,
                                       @ApiParam(value = "News target name identifier", required = true)
                                       @PathParam("targetName") String targetName) {
    if (StringUtils.isBlank(targetName)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("Target name ist mandatory").build();
    }
    if (newsTargetToDeleteQueue.containsKey(targetName)) {
      org.exoplatform.services.security.Identity currentIdentity = ConversationState.getCurrent().getIdentity();
      String authenticatedUser = currentIdentity.getUserId();
      String originalModifierUser = newsTargetToDeleteQueue.get(targetName);
      if (!originalModifierUser.equals(authenticatedUser)) {
        LOG.warn("User {} attempts to cancel deletion of a news target deleted by user {}", authenticatedUser, originalModifierUser);
        return Response.status(Response.Status.FORBIDDEN).build();
      }
      newsTargetToDeleteQueue.remove(targetName);
      return Response.noContent().build();
    } else {
      return Response.status(Response.Status.BAD_REQUEST)
                     .entity("Target News with name {} was already deleted or isn't planned to be deleted" + targetName)
                     .build();
    }
  }

}
