package org.jee8ng.issues.boundary;

import java.net.URI;
import java.util.Optional;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jee8ng.security.boundary.JWTRequired;
import org.jee8ng.issues.entity.Issue;
import org.jee8ng.issues.entity.IssueEvent;

/**
 *
 * @author prashantp.org
 */
@Path("issues")
@Api(value = "Issues")
public class IssuesResource {

    @DefaultValue("v1")
    @HeaderParam("X-apiversion")
    private String apiVersion;

    @Inject
    private IssuesService service;
    
    @Inject
    private Event<IssueEvent> event;

    @GET
    @JWTRequired
    @ApiOperation(value = "Get all issues")
    public Response getAll() {
        return Response.ok(service.getAll()).build();
    }

    @GET
    @Path("{id}")
    @ApiOperation(value = "Get issue by id")
    public Response get(@PathParam("id") Long id) {
        final Optional<Issue> issueFound = service.get(id);
        if (issueFound.isPresent()) {
            return Response.ok(issueFound.get()).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @ApiOperation(value = "Add new issue")
    public Response add(@ApiParam(value = "Issue that needs to be added", required = true) Issue newIssue, @Context UriInfo uriInfo) {
        service.add(newIssue);
        event.fire(new IssueEvent(newIssue));
        return Response.created(getLocation(uriInfo, newIssue.getId())).build();
    }

    @PUT
    @Path("{id}")
    @ApiOperation(value = "Update existing issue")
    public Response update(@PathParam("id") Long id, Issue updated) {
        updated.setId(id);
        boolean done = service.update(updated);

        return done
                ? Response.ok(updated).build()
                : Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    @ApiOperation(value = "Delete issue at id")
    public Response remove(@PathParam("id") Long id) {
        service.remove(id);
        return Response.ok().build();
    }

    URI getLocation(UriInfo uriInfo, Long id) {
        return uriInfo.getAbsolutePathBuilder().path("" + id).build();
    }
}
