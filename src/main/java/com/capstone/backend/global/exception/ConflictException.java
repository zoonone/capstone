package com.capstone.backend.global.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
