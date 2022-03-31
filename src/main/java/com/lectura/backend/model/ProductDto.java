package com.lectura.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductDto {
    private Long id;
    private String sku;
    private String name;
    private String type;
    private Double regular_price;
    private String description;
    private String short_description;
    private List<ImageDto> images;
    private String status;
    private LocalDateTime date_created;
    private LocalDateTime date_modified;
}
