package com.hospital_management.exception;


import org.springframework.http.HttpStatus;

public class InvalidOperationException extends BaseException {
    public InvalidOperationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_OPERATION");
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST, "INVALID_OPERATION");
    }
}
