package com.hospital_management.exception;


import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }

    public UserAlreadyExistsException(String field, String value) {
        super("User already exists with " + field + ": " + value, HttpStatus.CONFLICT, "USER_ALREADY_EXISTS");
    }
}
