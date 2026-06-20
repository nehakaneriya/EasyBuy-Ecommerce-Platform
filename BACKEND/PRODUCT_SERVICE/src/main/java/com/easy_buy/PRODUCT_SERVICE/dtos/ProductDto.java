package com.easy_buy.PRODUCT_SERVICE.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.easy_buy.PRODUCT_SERVICE.dtos.ProductReviewDto;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {

    private UUID id;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "shortDesc is required")
    @Size(max = 500, message = "shortDesc must be at most 500 characters")
    private String shortDesc;

    @NotBlank(message = "longDesc is required")
    private String longDesc;

    @NotNull(message = "discount is required")
    @Min(value = 0, message = "discount must be greater than or equal to 0")
    @Max(value = 100, message = "discount must be less than or equal to 100")
    private Integer discount;

    @NotNull(message = "price is required")
    @Positive(message = "price must be greater than 0")
    private Double price;


    private Boolean live;
    private List<String> productImages;
    private List<CategoryDto> categories;
    private List<ProductReviewDto> reviews;
    

}


