package com.lectura.backend.service;

import com.lectura.backend.model.SaleRequest;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@RegisterRestClient(baseUri = "https://cantook.libranda.com/api/organisations/libreriaslectura")
@ClientHeaderParam(name = "Authorization", value = "Basic bGlibGVjdHVyYTpYI3lJelYhbF4kIXE=")
public interface CantookServiceAPI {

    @GET
    @Path("/publications/full.onix")
    Response getFullPublications(@QueryParam("start") String start);

    @GET
    @Path("/publications/delta.onix")
    Response getDeltaPublications(@QueryParam("from") String datetimeIso, @QueryParam("start") String start, @QueryParam("to") String to);

    @GET
    @Path("/publications/{isbn}.onix")
    Response getPublication(@PathParam("isbn") String isbn);

    @GET
    @Path("/publications/{isbn}/sales/new")
    Response simulateSale(@PathParam("isbn") String isbn, @QueryParam("format") String format, @QueryParam("cost") Integer cost,
                          @QueryParam("protection") String protection, @QueryParam("country") String country,
                          @QueryParam("currency") String currency, @QueryParam("price_type") String priceType);

    @POST
    @Path("/publications/{isbn}/sales")
    Response salePublication(@PathParam("isbn") String isbn, SaleRequest request);

    @GET
    @Path("/customers/{customer}/transactions/{transaction}/publications/{isbn}/download_links/{format}")
    Response getDownloadPublication(@PathParam("customer") String customer, @PathParam("transaction") String transaction,
                                    @PathParam("isbn") String isbn, @PathParam("format") String format, @QueryParam("uname") String uname);
}
