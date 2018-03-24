package com.gigaspaces.fdal.controller;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.service.KinveyDataService;
import com.gigaspaces.fdal.service.remoting.ISessionDataBroadcastingService;
import com.gigaspaces.fdal.service.remoting.ISessionDataRemotingService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.logging.Logger;

import static com.gigaspaces.fdal.model.document.User.ACL_FIELD_NAME;

import static com.gigaspaces.fdal.model.document.User.*;
import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    @Autowired
    private ISessionDataRemotingService sessionDataRemotingService;
    
    @Autowired
    private ISessionDataBroadcastingService sessionDataBroadcastingService;

    @Autowired
    private KinveyDataService kinveyDataService;

    @Autowired
    private GigaSpace usSpace;

    private static final String LOGIN_URL_FORMAT = "/user/%s";
    private static final String PUT_USER_URL_FORMAT = "/user/%s/{%s}";
    private static final String GET_USER_URL_FORMAT = "/user/%s/_me";
    private static final String GET_BY_ID_USER_URL_FORMAT = "/user/%s/%s";
    
    private static final String ID_KEY = "id";

    @RequestMapping(value = "/{appKey}/_me", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> get(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(GET_USER_URL_FORMAT, appKey);
        User user = sessionDataBroadcastingService.loadUser(extractKinveyToken(authorization), url, headers);
        return toUserResponse(user, url, headers);
    }

    @RequestMapping(value = "/{appKey}/{userId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getById(@PathVariable("appKey") String appKey, @PathVariable("userId") String userId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(GET_BY_ID_USER_URL_FORMAT, appKey, userId);
        User user = sessionDataRemotingService.loadUser(userId, url, headers);
        return toUserResponse(user, url, headers);
    }

    private ResponseEntity<?> toUserResponse(User user, String url, HttpHeaders headers) {
        if (user != null) {
            LOGGER.info(String.format("User [id = %s, username = %s] details have been read from the US space. Notifying Kinvey...", user.getId(), user.getUsername()));
            return ok(user.getProperties());
        } else {
            Response<User> result = kinveyDataService.loadOne(url, headers, User.class);
            user = result.getEntity();
            usSpace.write(user);
            
            LOGGER.warning(String.format("User [id = %s, username = %s] details have been read from Kinvey and saved to the US space.", user.getId(), user.getUsername()));
            return result.toRestResponse();
        }
    }

    @RequestMapping(value = "/{appKey}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> login(@PathVariable("appKey") String appKey, @RequestBody String userSecretJson, @RequestHeader HttpHeaders headers) throws Exception {
        String url = String.format(LOGIN_URL_FORMAT, appKey);
        Request request = new Request(url, headers);

        Response<User> result = kinveyDataService.create(request, userSecretJson, User.class);

        User user = result.getEntity();
        String userId = user.getId();
        sessionDataRemotingService.createUser(userId, user, appKey);

        LOGGER.info(String.format("User [id = %s, username = %s] logged in. Kinvey request: POST %s", userId, user.getUsername(), url));

        return result.toRestResponse();
    }

    @RequestMapping(value = "/{appKey}/{userId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> update(@PathVariable("appKey") String appKey, @PathVariable("userId") String userId, @RequestBody String userJson, @RequestHeader HttpHeaders headers) throws Exception {
        String url = String.format(PUT_USER_URL_FORMAT, appKey, ID_KEY);
        Request request = new Request(url, Collections.singletonMap(ID_KEY, userId), headers);

        Response<User> result = kinveyDataService.update(request, userJson, User.class);
        User userResult = result.getEntity();

        IdQuery<User> query = new IdQuery<User>(User.TYPE, userId);
        ChangeSet update = new ChangeSet()
            .set(USERNAME_FIELD_NAME, userResult.getUsername())
            .set(COUNTRY_FIELD_NAME, userResult.getCountry())
            .set(PROFILE_FIELD_NAME, userResult.getProfile())
            .set(EMAIL_FIELD_NAME, userResult.getEmail())
            .set(ACL_FIELD_NAME, new DocumentProperties(userResult.getAcl()))
            .set(KMD_FIELD_NAME, new DocumentProperties(userResult.getKmd()))
            .set(MESSAGING_FIELD_NAME, new DocumentProperties(userResult.getMessaging()));

        usSpace.change(query, update);

        LOGGER.info(String.format("User [id = %s, username = %s] details have been updated. Kinvey request: PUT %s.", userResult.getId(), userResult.getUsername(), url));

        return result.toRestResponse();
    }
}
