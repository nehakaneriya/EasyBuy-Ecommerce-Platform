package com.easy_buy.USER_SERVICE.entity;

import com.easy_buy.USER_SERVICE.enums.Gender;
import com.easy_buy.USER_SERVICE.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    private Boolean active = true;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street",  column = @Column(name = "street")),
        @AttributeOverride(name = "city",    column = @Column(name = "city")),
        @AttributeOverride(name = "state",   column = @Column(name = "state")),
        @AttributeOverride(name = "country", column = @Column(name = "country")),
        @AttributeOverride(name = "pinCode", column = @Column(name = "pin_code"))
    })
    private Address address;
}
