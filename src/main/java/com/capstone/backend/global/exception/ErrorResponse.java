package com.capstone.backend.global.exception;

public record ErrorResponse(int status, String error, String message) {
}
