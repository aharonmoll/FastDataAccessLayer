package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.service.KinveyDataService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

/**
 * @author Denys_Novikov
 *
 */
@RestController
@RequestMapping("/healthcheck")
public class HealthCheckController extends AbstractController {


    private static final Logger LOGGER = Logger.getLogger(HealthCheckController.class.getName());

    @Autowired
    private KinveyDataService kinveyDataService;

    @Autowired
    private GigaSpace usSpace;

    @RequestMapping(value = "/{appKey}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> get(@PathVariable("appKey") String appKey, @RequestHeader HttpHeaders headers) {
        LOGGER.info("Incoming HealthCheck request");

        if (usSpace == null || kinveyDataService == null) {
            return new Response("Some services failed to autowire", headers, HttpStatus.INTERNAL_SERVER_ERROR).toRestResponse();
        }
        return new Response(null, headers, HttpStatus.OK).toRestResponse();

    }
}
