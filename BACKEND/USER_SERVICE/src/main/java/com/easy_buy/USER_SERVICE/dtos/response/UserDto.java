package com.easy_buy.USER_SERVICE.dtos.response;

import com.easy_buy.USER_SERVICE.entity.Address;
import com.easy_buy.USER_SERVICE.enums.Gender;
import com.easy_buy.USER_SERVICE.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImage;
    private Gender gender;
    private Role role;
    private Boolean active;
    private Address address;
    private Instant createdAt;
    private Instant updatedAt;
}
