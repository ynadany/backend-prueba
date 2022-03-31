package com.lectura.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SimulateSaleResponse {
    private Long productId;
    private Double price;
    private boolean ok;
    private String message;
}
