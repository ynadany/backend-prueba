package com.lectura.backend.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

@ApplicationScoped
public class WooCommerceHeaderFactory implements ClientHeadersFactory {
    @ConfigProperty(name = "libranda.authorization-woocommerce")
    String authorizationBase64;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> multivaluedMap,
                                                 MultivaluedMap<String, String> multivaluedMap1) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("Authorization",
                "Basic " + authorizationBase64);
        return result;
    }
}
