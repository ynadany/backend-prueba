package com.lectura.backend.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleRequest {
    private String format;
    private Integer cost;
    private String protection;
    private String customer_id;
    private String transaction_id;
    private String sale_state;
    private String country;
    private String price_type;
    private String currency;
}