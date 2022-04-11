package com.lectura.backend.service.impl;

import com.lectura.backend.entity.Category;
import com.lectura.backend.entity.Publication;
import com.lectura.backend.model.CreateProductRequest;
import com.lectura.backend.model.ImageDto;
import com.lectura.backend.model.ItemDto;
import com.lectura.backend.repository.PublicationRepository;
import com.lectura.backend.service.IWooCommerceService;
import com.lectura.backend.service.WooCommerceAPI;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//@QuarkusTest
public class WooCommerceServiceTest {
    @Inject
    IWooCommerceService wooCommerceService;

    @Inject
    @RestClient
    private WooCommerceAPI wooCommerceAPI;

    @Test
    public void synchronization() throws Exception {
        wooCommerceService.synchronization();
    }

    @Test
    public void wooCommerceApiTest() {
        var products = wooCommerceAPI.getProducts();

        var product = new CreateProductRequest();
        product.setDescription("Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo.");
        product.setShort_description("Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas.");
        product.setName("Premium Quality");
        product.setRegular_price("61.99");
        product.setType("simple");
        product.setImages(Arrays.asList(new ImageDto(null, "https://assets-libr.cantook.net/medias/23/b0e4e6854b6237f0c086c5b7ecaa6193e77a60.jpg?h=-&w=200")));

        try {
            //var response = wooCommerceAPI.postProduct(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getCategoriesTest() {
        var subjectsBIC = StringUtils.split("W|D", "|");
        Stream<Integer> distinct1 = Arrays.stream(subjectsBIC)
                .peek(c -> System.out.println("Data: " + c))
                .map(s -> ((Category) Category.findById(s)).getCategoryId()).distinct();
        var result = distinct1
                .map(c -> ItemDto.builder().id(c).build())
                .collect(Collectors.toList());
        Assert.assertTrue(result.size() > 0);
    }

    @Test
    public void simulateSale() throws Exception {
        var response = wooCommerceService.simulateSale(1946L, 4.99D);
    }
}