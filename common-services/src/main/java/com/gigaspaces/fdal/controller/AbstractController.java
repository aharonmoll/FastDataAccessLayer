package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.document.User;

import org.openspaces.core.GigaSpace;
import com.j_spaces.core.client.SQLQuery;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.utils.AuthorizationType;
import org.springframework.http.HttpHeaders;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Svitlana_Pogrebna
 *
 */
public abstract class AbstractController {

    private static final String AUTH_ERR_MSG = HttpHeaders.AUTHORIZATION + " header should include Basic or Kinvey authorization type";
    
    protected String extractBasicToken(String authorizationValue) {
        if (!authorizationValue.contains(AuthorizationType.BASIC.getPrefix())) {
            throw new IllegalArgumentException(AUTH_ERR_MSG);
        }
        return authorizationValue.replace(AuthorizationType.BASIC.getPrefix(), "");
    }

    protected String extractKinveyToken(String authorizationValue) {
        if (!authorizationValue.contains(AuthorizationType.KINVEY.getPrefix())) {
            throw new IllegalArgumentException(AUTH_ERR_MSG);
        }
        return authorizationValue.replace(AuthorizationType.KINVEY.getPrefix(), "");
    }

    protected Request toRequest(HttpServletRequest request, HttpHeaders headers) {
        return new Request(request.getRequestURI(), headers);
    }
    
    protected Request toRequest(HttpServletRequest request, Map<String, String> params, HttpHeaders headers) {
        return new Request(extractUrl(request, params), params, headers);
    }
    
    protected String extractUrl(HttpServletRequest request, Map<String, String> params) {
        String requestURL = request.getRequestURI();
        String queryString = request.getQueryString();
        StringBuilder urlBuilder = new StringBuilder(requestURL);
        if (queryString != null) {
            urlBuilder.append('?');
            for (String paramName : params.keySet()) {
                urlBuilder.append(paramName).append("={").append(paramName).append("}&");
            }
            return urlBuilder.substring(0, urlBuilder.length() - 1);
        }
        return urlBuilder.toString();
    }
    
    protected User checkUnauthorized(GigaSpace space, String authorization) {
        SQLQuery<User> userQuery = new SQLQuery<User>(User.TYPE, "_kmd.authtoken = ?");
        userQuery.setParameters(extractKinveyToken(authorization));
        userQuery.setProjections("_id", "country");
        User user = space.readIfExists(userQuery);
        if (user == null) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        return user;
    }
}
