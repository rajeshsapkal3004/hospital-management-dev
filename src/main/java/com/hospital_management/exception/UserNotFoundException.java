package com.hospital_management.exception;


import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }

    public UserNotFoundException(String field, String value) {
        super("User not found with " + field + ": " + value, HttpStatus.NOT_FOUND, "USER_NOT_FOUND");
    }
}
