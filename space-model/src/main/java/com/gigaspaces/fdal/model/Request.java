package com.gigaspaces.fdal.model;

import org.springframework.http.HttpHeaders;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 */
public class Request implements Serializable {

    private static final long serialVersionUID = 1L;

    private String url;
    private Map<String, ?> urlVars;
    private HttpHeaders headers;

    public Request() {
    }

    public Request(String url, HttpHeaders headers) {
        this(url, Collections.emptyMap(), headers);
    }
    
    public Request(String url, Map<String, ?> urlVars, HttpHeaders headers) {
        this.url = url;
        this.urlVars = urlVars;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, ?> getUrlVars() {
        return urlVars;
    }

    public void setUrlVars(Map<String, ?> urlVars) {
        this.urlVars = urlVars;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }
}
