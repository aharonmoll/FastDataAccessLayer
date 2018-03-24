package com.gigaspaces.fdal.controller;

import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.IdQuery;
import com.j_spaces.core.client.SQLQuery;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.service.remoting.IKinveyDataRemotingService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

import static org.springframework.http.ResponseEntity.ok;
/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
@RequestMapping("/user")
public class UserController extends AbstractController {

    private static final Logger LOGGER = Logger.getLogger(UserController.class.getName());

    private static final String GET_USER_URL_FORMAT = "/user/%s/_me";
    private static final String LOGIN_URL_FORMAT = "/user/%s";
    private static final String USER_WITH_ID_URL_FORMAT = "/user/%s/%s";

    @Autowired
    private IKinveyDataRemotingService kinveyDataRemotingService;

    @Autowired
    @Qualifier("chinaSpace")
    private GigaSpace chinaSpace;

    @Autowired
    @Qualifier("usSpace")
    private GigaSpace usSpace;

    @RequestMapping(value = "/{appKey}/_me", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> get(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(GET_USER_URL_FORMAT, appKey);

        SQLQuery<User> userQuery = new SQLQuery<>(User.TYPE, "_kmd.authtoken = ?");
        String userAuthToken = extractKinveyToken(authorization);
        userQuery.setParameters(userAuthToken);
        return getUser(userQuery, url, headers, userAuthToken);
    }

    @RequestMapping(value = "/{appKey}/{userId}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getById(@PathVariable("appKey") String appKey, @PathVariable("userId") String userId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(USER_WITH_ID_URL_FORMAT, appKey, userId);

        IdQuery<User> userQuery = new IdQuery<>(User.TYPE, userId);
        return getUser(userQuery, url, headers, extractKinveyToken(authorization));
    }

    private ResponseEntity<?> getUser(ISpaceQuery<User> userQuery, String url, HttpHeaders headers, String userAuthToken) {
        User user = chinaSpace.readIfExists(userQuery);
        if (user != null) {
            LOGGER.info(String.format("User [id = %s, username = %s] details have been read from the China space. Notifying Kinvey...", user.getId(), user.getUsername()));

            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, headers, user.getId(), User.class);
            usSpace.write(kinveyNotifyRequest);

            return ok(user.getProperties());
        } else {
            Response<User> response = kinveyDataRemotingService.loadOne(new Request(url, headers), userAuthToken, User.class);

            user = response.getEntity();
            LOGGER.warning(String.format("User [id = %s, username = %s] details have been read from Kinvey. Kinvey request: GET %s", user.getId(), user.getUsername(), url));

            return response.toRestResponse();
        }
    }

    @RequestMapping(value = "/{appKey}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> login(@PathVariable("appKey") String appKey, @RequestBody String userSecretJson, @RequestHeader HttpHeaders headers) {
        String url = String.format(LOGIN_URL_FORMAT, appKey);
        Request request = new Request(url, headers);

        Response<User> result = kinveyDataRemotingService.login(request, appKey, userSecretJson);

        User user = result.getEntity();
        LOGGER.info(String.format("User [id = %s, username = %s] logged in. Kinvey request: POST %s", user.getId(), user.getUsername(), url));

        return result.toRestResponse();
    }

    @RequestMapping(value = "/{appKey}/{userId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> update(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @PathVariable("userId") String userId, @RequestBody String userJson, @RequestHeader HttpHeaders headers) {
        String url = String.format(USER_WITH_ID_URL_FORMAT, appKey, userId);
        
        LOGGER.info(String.format("PUT request %s , body: %s", url, userJson));
        Request request = new Request(url, headers);

        Response<User> result = kinveyDataRemotingService.update(request, userId, userJson);

        User user = result.getEntity();
        LOGGER.info(String.format("User [id = %s, username = %s] details have been updated. Kinvey request: PUT %s", user.getId(), user.getUsername(), url));

        return result.toRestResponse();
    }
}
