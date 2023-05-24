package com.springboot.application.testing.exceptions;

public class UsersServiceException extends RuntimeException {
    public UsersServiceException(String message) {
        super(message);
    }
}