package com.lectura.backend.resource;

import com.lectura.backend.model.SynchronizationRequest;
import com.lectura.backend.service.ICantookService;
import org.eclipse.microprofile.faulttolerance.Fallback;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RolesAllowed("Admin")
@Path("/lectura/api/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {
    @Inject
    ICantookService cantookService;

    @POST
    @Fallback(fallbackMethod = "fallbackResponse")
    @Path("full")
    public Response post() throws Exception {
        var result = cantookService.fullSynchronization();
        if (result) {
            return Response.ok("The operation is processing another task ...").build();
        }
        return Response.ok("Processed").build();
    }

    @POST
    @Fallback(fallbackMethod = "fallbackResponse")
    @Path("delta")
    public Response post(SynchronizationRequest request) throws Exception {
        var result = cantookService.deltaSynchronization(request.getDateTime());
        if (result) {
            return Response.ok("The operation is processing another task ...").build();
        }
        return Response.ok("Processed").build();
    }

    public Response fallbackResponse(SynchronizationRequest request) {
        return fallbackResponse();
    }

    public Response fallbackResponse() {
        return Response.accepted().entity("The operation is processing in background ...").build();
    }
}
