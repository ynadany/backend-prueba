package com.lectura.backend.model;

import lombok.Data;

import java.util.Map;

@Data
public class CreateSalesRequest {
    private Long orderNumber;
    private String customer;
    private Map<String, String> items;
}
