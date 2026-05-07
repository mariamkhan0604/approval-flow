package com.approvalflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource cannot be found in the database.
 *
 * @ResponseStatus(HttpStatus.NOT_FOUND) tells Spring MVC to automatically
 * respond with HTTP 404 whenever this exception is thrown from a controller.
 *
 * We also handle it in GlobalExceptionHandler for a consistent JSON body.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
