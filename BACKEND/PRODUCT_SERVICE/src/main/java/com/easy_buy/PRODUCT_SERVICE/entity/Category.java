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
@Table(name = "categories")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Category extends BaseEntity{

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String title;

    @ManyToMany(mappedBy = "categories")
    private List<Product> products=new ArrayList<>();
}
