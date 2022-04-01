package com.lectura.backend.resource;

import com.lectura.backend.model.SynchronizationRequest;
import com.lectura.backend.service.ICantookService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;

@RolesAllowed("Admin")
@Path("/lectura/api/publications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicationResource {
    @Inject
    ICantookService cantookService;

    @POST
    @Path("full")
    @Transactional
    public Response post() throws Exception {
        cantookService.fullSynchronization();
        return Response.ok().build();
    }

    @POST
    @Path("delta")
    @Transactional
    public Response post(SynchronizationRequest request) throws Exception {
        cantookService.deltaSynchronization(request.getDateTime());
        return Response.ok().build();
    }
}
