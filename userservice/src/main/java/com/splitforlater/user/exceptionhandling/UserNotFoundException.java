package com.splitforlater.user.exceptionhandling;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
