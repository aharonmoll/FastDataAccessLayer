package com.gigaspaces.fdal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.fdal.kinvey.KinveyRestClient;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.utils.AuthorizationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class KinveyDataService {

    @Autowired
    private KinveyRestClient client;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String URL_FORMAT = "/appdata/%s/";

    public <T> Response<T[]> load(String resourceName, String appKey, String authorizationToken, Class<? extends T> clazz) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, AuthorizationType.KINVEY.getPrefix() + authorizationToken);
        
        return load(String.format(URL_FORMAT, appKey) + resourceName, headers, clazz);
    }

    public <T> Response<T[]> load(String url, HttpHeaders headers, Class<? extends T> clazz) {
        ResponseEntity<String> result = client.get(url, headers);
        return toArrayResponse(result, url, clazz);
    }

    public <T> Response<T> loadOne(String url, HttpHeaders headers, Class<? extends T> clazz) {
        ResponseEntity<String> result = client.get(url, headers);
        return toResponse(result, url, clazz);
    }

    public <T> Response<T> create(Request request, String entityJson, Class<? extends T> entityType) {
        return createOrUpdate(request, entityJson, entityType, true);
    }

    public <T> Response<T> update(Request request, String entityJson, Class<? extends T> entityType) {
        return createOrUpdate(request, entityJson, entityType, false);
    }

    public Response<Void> delete(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("'request' parameter must not be null");
        }
        String url = request.getUrl();
        ResponseEntity<String> result = client.delete(url, request.getUrlVars(), request.getHeaders());

        checkResponseCode(url, HttpMethod.DELETE, result);
        return new Response<Void>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    private <T> Response<T> createOrUpdate(Request request, String entityJson, Class<? extends T> entityType, boolean isPost) {
        if (request == null) {
            throw new IllegalArgumentException("'request' parameter must not be null");
        }

        String url = request.getUrl();
        ResponseEntity<String> result = isPost ? client.post(url, entityJson, request.getHeaders()) : client.put(url, request.getUrlVars(), entityJson, request.getHeaders());

        checkResponseCode(url, isPost ? HttpMethod.POST : HttpMethod.PUT, result);

        return toResponse(result, url, entityType);
    }

    private <T> Response<T> toResponse(ResponseEntity<String> result, String url, Class<? extends T> responseType) {
        String response = result.getBody();
        try {
            T entity;
            if (SpaceDocument.class.isAssignableFrom(responseType)) {
                Map<String, Object> properties = objectMapper.readValue(response, Map.class);
                entity = (T) createSpaceDocument((Class<? extends SpaceDocument>) responseType, properties);
            } else {
                entity = objectMapper.readValue(response, responseType);
            }
            return new Response<T>(response, entity, result.getHeaders());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse an object response: " + response + ", that was returned by Kinvey " + url + " endpoint", e);
        }
    }

    private <T extends SpaceDocument> T createSpaceDocument(Class<T> type, Map<String, Object> properties) {
        try {
            return type.getConstructor(Map.class).newInstance(properties);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create an instance of " + type + " extended document type", e);
        }
    }
    
    private <T> Response<T[]> toArrayResponse(ResponseEntity<String> result, String url, Class<? extends T> responseType) {
        String response = result.getBody();
        try {
            T[] entityArray;
            if (SpaceDocument.class.isAssignableFrom(responseType)) {
                Map<String, Object>[] propertiesArray = objectMapper.readValue(response, Map[].class);
                entityArray = (T[]) Array.newInstance(responseType, propertiesArray.length);
                for (int i = 0; i < propertiesArray.length; i++) {
                    entityArray[i] = (T) createSpaceDocument((Class<? extends SpaceDocument>) responseType, propertiesArray[i]);
                }
            } else {
                Class<T[]> arrayClass = (Class<T[]>) Array.newInstance(responseType, 0).getClass();
                entityArray = objectMapper.readValue(response, arrayClass);
            }
            return new Response<T[]>(response, entityArray, result.getHeaders());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse an array response: " + response + ", that was returned by Kinvey " + url + " endpoint", e);
        }
    }

    private static void checkResponseCode(String url, HttpMethod method, ResponseEntity<String> jsonResponse) {
        HttpStatus statusCode = jsonResponse.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            throw new UnsupportedOperationException("Unsupported HTTP response code: " + statusCode + ", body: " + jsonResponse.getBody() + " for " + method + url + " request");
        }
    }
}
