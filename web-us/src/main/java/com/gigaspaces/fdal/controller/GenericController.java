package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.kinvey.KinveyRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
public class GenericController extends AbstractController {

    @Autowired
    private KinveyRestClient client;

    @RequestMapping(value = "/**", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> get(HttpServletRequest httpRequest, @RequestParam LinkedHashMap<String, String> allRequestParams, @RequestHeader HttpHeaders headers) {
        String url = extractUrl(httpRequest, allRequestParams);
        return client.get(url, allRequestParams, headers);
    }

    @RequestMapping(value = "/**", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> create(HttpServletRequest httpRequest, @RequestBody(required = false) String jsonBody, @RequestHeader HttpHeaders headers) {
        String url = httpRequest.getRequestURI();
        return client.post(url, jsonBody, headers);
    }

    @RequestMapping(value = "/**", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> update(HttpServletRequest httpRequest, @RequestBody(required = false) String jsonBody, @RequestHeader HttpHeaders headers) {
        String url = httpRequest.getRequestURI();
        return client.put(url, Collections.emptyMap(), jsonBody, headers);
    }

    @RequestMapping(value = "/**", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> delete(HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        String url = httpRequest.getRequestURI();
        return client.delete(url, Collections.emptyMap(), headers);
    }
}
