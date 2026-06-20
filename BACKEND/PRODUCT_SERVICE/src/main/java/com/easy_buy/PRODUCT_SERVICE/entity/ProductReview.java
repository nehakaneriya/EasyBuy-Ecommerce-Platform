package com.easy_buy.PRODUCT_SERVICE.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class ProductReview extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String comment;

     //default assume: x/5
    private Integer rating;

    @ManyToOne
    private Product product;
}

