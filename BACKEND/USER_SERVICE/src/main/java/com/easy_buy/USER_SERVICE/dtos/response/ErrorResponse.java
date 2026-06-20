package com.easy_buy.USER_SERVICE.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<String> fieldErrors;

    public ErrorResponse() {
        this.timestamp = Instant.now();
    }

}
