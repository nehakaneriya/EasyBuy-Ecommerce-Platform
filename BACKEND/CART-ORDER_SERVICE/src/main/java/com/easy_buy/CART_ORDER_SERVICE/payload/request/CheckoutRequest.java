package com.easy_buy.CART_ORDER_SERVICE.payload.request;

import com.easy_buy.CART_ORDER_SERVICE.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    @NotBlank(message = "billingName is required")
    private String billingName;

    @NotBlank(message = "BillingPhoneNumber is required")
    private String billingPhone;

    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;


    private String extraInformation;
}