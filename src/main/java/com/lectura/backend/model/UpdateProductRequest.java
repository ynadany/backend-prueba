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
public class UpdateProductRequest {
    private String name;
    private String regular_price;
    private String description;
    private String short_description;
    private List<ItemDto> tags;
    private List<ItemDto> categories;
}
