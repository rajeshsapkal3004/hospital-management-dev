package com.hospital_management.exception;


import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends BaseException {
    public UnauthorizedAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS");
    }
}

