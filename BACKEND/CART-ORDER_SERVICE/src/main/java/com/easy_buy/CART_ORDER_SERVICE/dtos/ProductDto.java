package com.easy_buy.CART_ORDER_SERVICE.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {

    private UUID id;

    private String title;

    private String shortDesc;

    private Integer discount;

    private Double price;

    private Boolean live;

    private List<String> productImages;

    private List<String> categories;
}
