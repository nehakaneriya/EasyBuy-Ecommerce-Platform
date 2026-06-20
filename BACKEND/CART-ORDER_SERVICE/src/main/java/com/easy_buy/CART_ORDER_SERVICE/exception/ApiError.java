package com.easy_buy.CART_ORDER_SERVICE.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(

        Instant timestamp,

        int status,

        String error,

        String message,

        String path,

        Map<String, String> validationErrors

) {
}