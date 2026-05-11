package com.splitforlater.common.exceptionhandling;

public class ErrorResponse {
    private String message;
    private String error;

    public ErrorResponse(String userNotFound, String message) {
        this.error = userNotFound;
        this.message = message;
    }
}
