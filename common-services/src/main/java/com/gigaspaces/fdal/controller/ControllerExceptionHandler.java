package com.gigaspaces.fdal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Svitlana_Pogrebna
 *
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    private static final Logger LOGGER = Logger.getLogger(ControllerExceptionHandler.class.getName());

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<String> handle(HttpStatusCodeException e, WebRequest request) {
        LOGGER.log(Level.INFO, String.format("Request %s was failed by Kinvey", request.getDescription(true)), e);
        return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handle(Throwable e, WebRequest request) {
        LOGGER.log(Level.SEVERE, String.format("Request %s was failed due to unexpected system exception", request.getDescription(true)), e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
