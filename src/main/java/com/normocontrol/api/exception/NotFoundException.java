package com.normocontrol.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    private final String errorCode;
    private final Long entityId;

    public NotFoundException(String message) {
        super(message);
        this.errorCode = "NOT_FOUND";
        this.entityId = null;
    }

    public NotFoundException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.entityId = null;
    }

    public NotFoundException(String message, Long entityId) {
        super(message);
        this.errorCode = "NOT_FOUND";
        this.entityId = entityId;
    }

    public NotFoundException(String errorCode, String message, Long entityId) {
        super(message);
        this.errorCode = errorCode;
        this.entityId = entityId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Long getEntityId() {
        return entityId;
    }
}