package com.lectura.backend.service;

import com.lectura.backend.model.CreateProductRequest;
import com.lectura.backend.model.ItemDto;
import com.lectura.backend.model.ProductDto;
import com.lectura.backend.model.UpdateProductRequest;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@RegisterRestClient(baseUri = "https://54.188.215.119/wp-json/wc/v3")
@ClientHeaderParam(name = "Authorization", value = "Basic Y2tfYWMyMmUwYTE4ODg2ODRmNjJjODc3MjAwODY3ZjJhZjVmN2JiMGY4OTpjc19iMjM1N2NmNjk4ODg3NzA1ODI0OWNkYzI3N2VjZTlhMzZiMzVlNTg5")
public interface WooCommerceAPI {
    @GET
    @Path("/products")
    List<ProductDto> getProducts();

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/products")
    ProductDto postProduct(CreateProductRequest request);

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/products/{id}")
    ProductDto putProduct(@PathParam("id") Long id, UpdateProductRequest request);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/products/tags")
    ItemDto postTag(ItemDto request);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/products/categories")
    ItemDto postCategories(ItemDto request);
}
