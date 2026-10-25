package com.youns.portfoliospr.service;

import org.springframework.http.HttpStatus;

public class CommunityException extends RuntimeException {

    private final HttpStatus status;

    public CommunityException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
