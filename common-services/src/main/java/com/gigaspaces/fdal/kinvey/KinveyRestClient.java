package com.gigaspaces.fdal.kinvey;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import javax.annotation.PostConstruct;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class KinveyRestClient {

    @Value("${protocol:https}")
    private String protocol;

    @Value("${kinvey.hostname:se-baas.kinvey.com}")
    private String kinveyHostname;

    @Value("${version.header.key:X-Kinvey-API-Version}")
    private String kinveyVersionHeaderKey;

    @Value("${version:3}")
    private String kinveyVersion;

    private String kinveyUrl;

    @PostConstruct
    public void init() {
        kinveyUrl = protocol + "://" + kinveyHostname;
    }

    private RestTemplate template;

    public ResponseEntity<String> get(String url, HttpHeaders headers) {
        checkAndUpdateHttpHeaders(headers);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
 
        return template.exchange(kinveyUrl + url, HttpMethod.GET, requestEntity, String.class);
    }

    public ResponseEntity<String> get(String url, Map<String, ?> urlVars, HttpHeaders headers) {
        checkAndUpdateHttpHeaders(headers);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        return template.exchange(kinveyUrl + url, HttpMethod.GET, requestEntity, String.class, urlVars);
    }

    public ResponseEntity<String> post(String url, String jsonBody, HttpHeaders headers) {
        checkAndUpdateHttpHeaders(headers);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        return template.postForEntity(kinveyUrl + url, requestEntity, String.class);
    }

    public ResponseEntity<String> put(String url, Map<String, ?> urlVars, String jsonBody, HttpHeaders headers) {
        checkAndUpdateHttpHeaders(headers);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        return template.exchange(kinveyUrl + url, HttpMethod.PUT, requestEntity, String.class, urlVars);
    }

    public ResponseEntity<String> delete(String url, Map<String, ?> urlVars, HttpHeaders headers) {
        checkAndUpdateHttpHeaders(headers);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        return template.exchange(kinveyUrl + url, HttpMethod.DELETE, requestEntity, String.class, urlVars);
    }

    private void checkAndUpdateHttpHeaders(HttpHeaders headers) {
        if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "No Authorization header is provided");
        }
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(kinveyVersionHeaderKey, kinveyVersion);
    }

    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.template = restTemplate;
    }
}
