package com.easy_buy.PRODUCT_SERVICE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


// This class is a base entity that can be extended by other entities in the application. It can contain common fields and methods that are shared across multiple entities, such as createdAt, updatedAt, etc. By using @MappedSuperclass, we indicate that this class is not an entity itself but provides mapping information for its subclasses.
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    // Automatically set the creation timestamp when a new product is created
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant createdAt;


    // Automatically update the timestamp whenever the product is updated
    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}
