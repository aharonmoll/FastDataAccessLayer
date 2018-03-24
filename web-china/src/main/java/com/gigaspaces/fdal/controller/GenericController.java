package com.gigaspaces.fdal.controller;

import org.springframework.web.bind.annotation.RequestBody;

import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.service.remoting.IKinveyDataRemotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
public class GenericController extends AbstractController {

    @Autowired
    private IKinveyDataRemotingService kinveyDataRemotingService;

    @RequestMapping(value="/**", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> get(HttpServletRequest httpRequest, @RequestParam LinkedHashMap<String,String> allRequestParams, @RequestHeader HttpHeaders headers) {
       Request request = toRequest(httpRequest, allRequestParams, headers);
       return kinveyDataRemotingService.load(request).toRestResponse();
    }

    @RequestMapping(value="/**", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> create(HttpServletRequest httpRequest, @RequestBody(required = false) String jsonBody, @RequestHeader HttpHeaders headers) {
        Request request = toRequest(httpRequest, headers);
        return kinveyDataRemotingService.create(request, jsonBody).toRestResponse();
    }

    @RequestMapping(value="/**", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> update(HttpServletRequest httpRequest, @RequestBody(required = false) String jsonBody, @RequestHeader HttpHeaders headers) {
        Request request = toRequest(httpRequest, headers);
        return kinveyDataRemotingService.update(request, jsonBody).toRestResponse();
    }

    @RequestMapping(value="/**", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> delete(HttpServletRequest httpRequest, @RequestHeader HttpHeaders headers) {
        Request request = toRequest(httpRequest, headers);
        return kinveyDataRemotingService.delete(request).toRestResponse();
    }
}
