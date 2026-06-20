package com.easy_buy.COMMON_SERVICE.payload;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
// This class is used to represent a snapshot of product details that can be shared across services without exposing the entire product entity.
public class ProductSnapshot {

    private UUID id;

    private String title;

    private Double price;

    private Integer discount;

    private Boolean live;

    private List<String> productImages;

}
