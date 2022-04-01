package com.lectura.backend.resource;

import com.lectura.backend.model.OrderDto;
import com.lectura.backend.model.SynchronizationRequest;
import com.lectura.backend.service.IWooCommerceService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.*;
import javax.transaction.NotSupportedException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RolesAllowed("Admin")
@Path("/lectura/api/woocommerce")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WoocommerceResource {
    @Inject
    private IWooCommerceService wooCommerceService;

    @POST
    public Response post(SynchronizationRequest request) throws Exception {
        wooCommerceService.synchronization(request.getDateTime());
        return Response.ok().build();
    }

    @GET
    @Path("/simulate-sale/{productId}")
    public Response simulateSale(@PathParam("productId") Long productId, @QueryParam("price") Double price,
                                 @QueryParam("sku") String sku) throws Exception {
        var response = wooCommerceService.simulateSale(productId, price);
        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Path("/sale/{productId}")
    public Response registerSale(@PathParam("productId") Long productId, OrderDto order) throws Exception {
        if (productId.equals(order.getProductId())) {
            throw new BadRequestException("ProductID should be the same in the body");
        }
        wooCommerceService.registerSale(order);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Transactional
    @Path("/download/{orderId}")
    public Response getDownloadUrl(@PathParam("orderId") Long orderId, @QueryParam("uname") String uname) throws HeuristicRollbackException,
            SystemException, HeuristicMixedException, NotSupportedException, RollbackException {
        var downloadUrl = wooCommerceService.getDownloadUrl(orderId, uname);
        return Response.temporaryRedirect(downloadUrl).build();
    }
}
