package com.lectura.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderDto {
    private Long productId;
    private String username;
    private String orderId;
    private String sku;
    private Double price;
}
