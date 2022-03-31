package com.lectura.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreateProductRequest {
    private String sku;
    private String name;
    private String type;
    private String regular_price;
    private String description;
    private String short_description;
    private boolean virtual;
    private List<ImageDto> images;
    private List<ItemDto> tags;
    private List<ItemDto> categories;
}
