package com.easy_buy.USER_SERVICE.dtos.request;

import com.easy_buy.USER_SERVICE.entity.Address;
import com.easy_buy.USER_SERVICE.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

    private String firstName;
    private String lastName;
    private String phone;
    private String profileImage;
    private Gender gender;
    private Address address;
}
