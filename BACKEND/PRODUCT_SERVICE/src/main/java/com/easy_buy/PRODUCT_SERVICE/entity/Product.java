package com.easy_buy.PRODUCT_SERVICE.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product extends BaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

   private String title;

   @Column(columnDefinition = "TEXT")
   private String shortDesc;

   @Column(columnDefinition = "TEXT")
   private String longDesc;

   private Integer discount;
   private Double price;

   private Boolean live=false;

   @ElementCollection(fetch = FetchType.EAGER)
   @CollectionTable(
           name = "product_images",
           joinColumns = @JoinColumn(name = "product_id")
   )
   @Column(name = "image_url")
   private List<String> productImages = new ArrayList<>();

   @ManyToMany
   @JoinTable(
           name = "product_category",
           joinColumns = @JoinColumn(name = "product_id"),
           inverseJoinColumns = @JoinColumn(name = "category_id")
   )
   private List<Category> categories=new ArrayList<>();

   @OneToMany(mappedBy = "product" ,cascade = CascadeType.ALL,orphanRemoval = true)
   private List<ProductReview> reviews=new ArrayList<>();

}
