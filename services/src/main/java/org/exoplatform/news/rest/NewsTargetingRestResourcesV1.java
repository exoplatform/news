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

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.news.service.NewsTargetingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

@Path("v1/news/targeting")
@Api(tags = "v1/news/targeting", value = "v1/news/targeting")
public class NewsTargetingRestResourcesV1 implements ResourceContainer {

  private static final Log       LOG                             = ExoLogger.getLogger(NewsTargetingRestResourcesV1.class);

  private NewsTargetingService newsTargetingService;

  public NewsTargetingRestResourcesV1(NewsTargetingService newsTargetingService) {
    this.newsTargetingService = newsTargetingService;
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

}
