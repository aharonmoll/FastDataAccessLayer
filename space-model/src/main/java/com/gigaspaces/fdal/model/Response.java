package com.gigaspaces.fdal.model;

import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import java.io.Serializable;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class Response<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private String json;
    private HttpHeaders headers;
    private HttpStatus code;
    private T entity;

    public Response() {}
    
    public Response(String json, HttpHeaders headers, HttpStatus code) {
        this(json, null, headers, code);
    }
    
    public Response(String json, T entity, HttpHeaders headers) {
        this(json, entity, headers, HttpStatus.OK);
    }

    public Response(String json, T entity, HttpHeaders headers, HttpStatus code) {
        this.json = json;
        this.headers = headers;
        this.code = code;
        this.entity = entity;
    }

    public ResponseEntity<String> toRestResponse() {
        return new ResponseEntity<String>(json, headers, code);
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public HttpStatus getCode() {
        return code;
    }

    public void setCode(HttpStatus code) {
        this.code = code;
    }
}