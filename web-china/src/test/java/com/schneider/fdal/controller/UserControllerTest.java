package com.gigaspaces.fdal.controller;

import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.document.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
/**
 * @author Svitlana_Pogrebna
 *
 */
@ContextConfiguration("/test-context.xml")
public class UserControllerTest extends AbstractControllerTest {

    private String userPath = "/user/kid_Zk-fAAEhwg";

    @Autowired
    private UserController userController;

    @Test
    public void login_succeed() throws Exception {
        User expectedUser = createUser();

        String jsonResponse = objectMapper.writeValueAsString(expectedUser.getProperties());
        mockKinvey200Response(jsonResponse, kinveyHost + userPath, HttpMethod.POST);

        ResultActions result = mockMvc.perform(post(userPath).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Basic a2lkX1prLWZBQUVod2c6Zjk1M2ZjYTZiYWFmNGJkMWI0MTExZDNhOTA1ZmIyMzg=")).content("{\"username\":\"User111111111\",\"password\":\"password\"}"));
        validateUserResponse(expectedUser, result);

        String userId = expectedUser.getId();
        User userTemplate = new User();
        userTemplate.setId(userId);
        User actualUser = usSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        waitForEmptyReplicationBacklog();

        actualUser = chinaSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);
    }
    
    @Test
    public void login_failed401() throws Exception {
        User expectedUser = createUser();

        mockKinvey401Response(kinveyHost + userPath, HttpMethod.POST);

        mockMvc.perform(post(userPath).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Basic a2lkX1prLWZBQUVod2c6Zjk1M2ZjYTZiYWFmNGJkMWI0MTExZDNhOTA1ZmIyMzg=")).content("{\"username\":\"User111111111\",\"password\":\"password\"}"));

        String userId = expectedUser.getId();
        User userTemplate = new User();
        userTemplate.setId(userId);
        assertNull(usSpace.readIfExists(userTemplate));
        assertNull(chinaSpace.readIfExists(userTemplate));
    }

    @Test
    public void updateUser_succeed() throws Exception {
        login();
        User expectedUser = updatedUser();

        String jsonResponse = objectMapper.writeValueAsString(expectedUser.getProperties());
        String id = expectedUser.getId();
        String urlPath = userPath + "/" + id;
        mockKinvey200Response(jsonResponse, kinveyHost + urlPath, HttpMethod.PUT);

        HttpHeaders headers = createHeaders("Kinvey " + expectedUser.getAuthtoken());
        ResultActions result = mockMvc.perform(put(urlPath).accept(MediaType.APPLICATION_JSON).headers(headers).content(jsonResponse));
        validateUserResponse(expectedUser, result);

        User userTemplate = new User();
        userTemplate.setId(id);
        User actualUser = usSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        waitForEmptyReplicationBacklog();

        actualUser = chinaSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);
    }
    
    @Test
    public void updateUser_failed401() throws Exception {
        User expectedUser = login();

        String jsonResponse = objectMapper.writeValueAsString(updatedUser().getProperties());
        String urlPath = userPath + "/" + expectedUser.getId();
        mockKinvey401Response(kinveyHost + urlPath, HttpMethod.PUT);

        HttpHeaders headers = createHeaders("Kinvey " + expectedUser.getAuthtoken());
        mockMvc.perform(put(urlPath).accept(MediaType.APPLICATION_JSON).headers(headers).content(jsonResponse)).andExpect(status().isUnauthorized());

        User userTemplate = new User();
        userTemplate.setId(expectedUser.getId());
        User actualUser = usSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        waitForEmptyReplicationBacklog();
        
        actualUser = chinaSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);
    }
    
    @Test
    public void getUserFromSpace_succeed() throws Exception {
        User user = login();
        getUserFromSpace_succeed(user, userPath + "/_me");
    }
    
    @Test
    public void getUserByIdFromSpace_succeed() throws Exception {
        User user = login();
        getUserFromSpace_succeed(user, userPath + "/" + user.getId());
    }

    private void getUserFromSpace_succeed(User expectedUser, String urlPath) throws Exception {
        HttpHeaders headers = createHeaders("Kinvey " + expectedUser.getAuthtoken());
        ResultActions result = mockMvc.perform(get(urlPath).headers(headers));
        validateUserResponse(expectedUser, result);

        String userId = expectedUser.getId();
        User userTemplate = new User();
        userTemplate.setId(userId);
        User actualUser = chinaSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        KinveyAsyncGetRequest template = new KinveyAsyncGetRequest();
        template.setUserId(userId);
        KinveyAsyncGetRequest asyncRequest = usSpace.read(template);
        assertNotNull(asyncRequest);
        assertEquals(urlPath, asyncRequest.getUrl());
        assertEquals(User.class, asyncRequest.getEntityType());
        assertEquals(headers, asyncRequest.getHeaders());
        assertEquals(Boolean.FALSE, asyncRequest.getSaveToSpace());
        assertEquals(userId, asyncRequest.getUserId());
    }

    @Test
    public void getUserFromKinvey_succeed() throws Exception {
        User expectedUser = createUser();
        String urlPath = userPath + "/_me";
        getUserFromKinvey_succeed(expectedUser, urlPath);
    }

    @Test
    public void getUserByIdFromKinvey_succeed() throws Exception {
        User expectedUser = createUser();
        String urlPath = userPath + "/" + expectedUser.getId();
        getUserFromKinvey_succeed(expectedUser, urlPath);
    }

    private void getUserFromKinvey_succeed(User expectedUser, String urlPath) throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(expectedUser.getProperties());
        mockKinvey200Response(jsonResponse, kinveyHost + urlPath, HttpMethod.GET);
        
        HttpHeaders headers = createHeaders("Kinvey " + expectedUser.getAuthtoken());
        ResultActions result = mockMvc.perform(get(urlPath).headers(headers));
        validateUserResponse(expectedUser, result);

        String userId = expectedUser.getId();
        User userTemplate = new User();
        userTemplate.setId(userId);
        User actualUser = usSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        waitForEmptyReplicationBacklog();

        actualUser = chinaSpace.readIfExists(userTemplate);
        assertNotNull(actualUser);
        validateUser(expectedUser, actualUser);

        KinveyAsyncGetRequest asyncRequest = usSpace.read(new KinveyAsyncGetRequest());
        assertNull(asyncRequest);
    }

    private User updatedUser() {
        User user = createUser();
        user.setCountry("USA");
        user.setEmail("Updated email");
        user.setProfile("Updated profile");
        user.setUsername("Updated username");
        DocumentProperties messaging = new DocumentProperties();
        DocumentProperties pushToken = new DocumentProperties();
        pushToken.setProperty("arn", "Arn");
        pushToken.setProperty("platform", "Platform");
        pushToken.setProperty("token", "Token");
        messaging.setProperty("pushTokens", new DocumentProperties[] {pushToken});
        user.setMessaging(messaging);
        DocumentProperties kmd = new DocumentProperties();
        kmd.setProperty("ect", "Updated ect date");
        kmd.setProperty("lmt", "Updated lmt date");
        kmd.setProperty("authtoken", "52c6d75c-f19b-43d4-8d69-63a16e9151bf.3Mqup8yQpCR1tfFPfRYZ+msPnfy8HYppfcqYfOXqlTk=");
        user.setKmd(kmd);
        return user;
    }
    
    private void validateUserResponse(User expectedUser, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
        .andExpect(jsonPath("$._id").value(expectedUser.getId()))
        .andExpect(jsonPath("$.country").value(expectedUser.getCountry()))
        .andExpect(jsonPath("$.loggedInWith").value(expectedUser.getLoggedInWith().getValue()))
        .andExpect(jsonPath("$.profile").value(expectedUser.getProfile()))
        .andExpect(jsonPath("$.username").value(expectedUser.getUsername()))
        .andExpect(jsonPath("$._kmd.authtoken").value(expectedUser.getAuthtoken()))
        .andExpect(jsonPath("$._kmd.ect").value(expectedUser.getKmd().get("ect")))
        .andExpect(jsonPath("$._kmd.lmt").value(expectedUser.getKmd().get("lmt")));
    }
    
    private void validateUser(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCountry(), actual.getCountry());
        assertEquals(expected.getLoggedInWith(), actual.getLoggedInWith());
        assertEquals(expected.getProfile(), actual.getProfile());
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getAuthtoken(), actual.getAuthtoken());
    }
}
