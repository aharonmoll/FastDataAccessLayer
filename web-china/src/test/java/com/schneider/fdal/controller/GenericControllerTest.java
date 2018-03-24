package com.gigaspaces.fdal.controller;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Svitlana_Pogrebna
 *
 */
@ContextConfiguration("/test-context.xml")
public class GenericControllerTest extends AbstractControllerTest {

    private static final String PROFILES_URL = "/appdata/" + appKey + "/profiles";
    private static final String PROFILES_QUERY_URL = PROFILES_URL + "?query={\"countryId\":\"USA\",\"title\":\"EMPLOYEE\"}&skip=1&limit=1";
    private static final String USERS_URL = "/user/" + appKey + "/_logout";

    @Autowired
    private GenericController genericController;

    @Test
    public void getProfiles() throws Exception {
        HttpHeaders headers = createHeaders("Kinvey " + KINVEY_AUTHTOKEN);

        String response = "{\"test\":\"Test1\"}";
        mockKinvey200Response(response, kinveyHost + PROFILES_URL, HttpMethod.GET);

        mockMvc.perform(get(PROFILES_URL).headers(headers))
            .andExpect(status().isOk()).andExpect(jsonPath("$[*].test").value("Test1"));
    }

    @Test
    public void getProfilesByQuery() throws Exception {
        HttpHeaders headers = createHeaders("Kinvey " + KINVEY_AUTHTOKEN);

        String response = "{\"test\":\"Test2\"}";
        
        URI uri = UriComponentsBuilder.fromUriString(PROFILES_QUERY_URL).build(false).toUri();
        mockKinvey200Response(response, kinveyHost + uri.toString(), HttpMethod.GET);

        ResultActions result = mockMvc.perform(get(uri).headers(headers));
        result.andExpect(status().isOk()).andExpect(jsonPath("$[*].test").value("Test2"));
    }

    @Test
    public void createProfile() throws Exception {
        testProfile(HttpMethod.POST);
    }

    @Test
    public void updateProfile() throws Exception {
        testProfile(HttpMethod.PUT);
    }

    @Test
    public void deleteProfile() throws Exception {
        testProfile(HttpMethod.DELETE);
    }

    @Test
    public void logout() throws Exception {
        HttpHeaders headers = createHeaders("Kinvey " + KINVEY_AUTHTOKEN);

        mockKinvey204Response(kinveyHost + USERS_URL, HttpMethod.POST);

        mockMvc.perform(post(USERS_URL).headers(headers))
            .andExpect(status().isNoContent());
    }

    private void testProfile(HttpMethod method) throws Exception {
        HttpHeaders headers = createHeaders("Kinvey " + KINVEY_AUTHTOKEN);

        String response = "{\"test\":\"Test1\"}";

        String urlPath;
        MockHttpServletRequestBuilder builder;
        boolean delete = false;
            switch(method) {
            case POST:
                urlPath = PROFILES_URL;
                builder = post(urlPath).headers(headers).content(response);
                break;
            case PUT:
                urlPath = PROFILES_URL + "/1";
                builder = put(urlPath).headers(headers).content(response);
                break;
            case DELETE:
                delete = true;
                urlPath = PROFILES_URL + "/1";
                builder = delete(urlPath).headers(headers);
                break;
             default:
                 throw new UnsupportedOperationException("method " + method + " is unsupported");
            }

        if (delete) {
            mockKinvey204Response(kinveyHost + urlPath, method);
            mockMvc.perform(builder).andExpect(status().isNoContent());
        } else {
            mockKinvey200Response(response, kinveyHost + urlPath, method);
            mockMvc.perform(builder).andExpect(status().isOk()).andExpect(jsonPath("$[*].test").value("Test1"));
        }
    }
}
