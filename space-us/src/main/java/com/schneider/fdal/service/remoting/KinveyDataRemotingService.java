package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.document.DocumentProperties;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.fdal.kinvey.KinveyRestClient;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.PrivateData;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.service.KinveyDataService;
import com.gigaspaces.fdal.utils.SessionDataManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.core.space.mode.PostPrimary;
import org.openspaces.remoting.RemotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;
import java.util.Collections;

import static com.gigaspaces.fdal.model.document.User.*;
/**
 * @author Svitlana_Pogrebna
 *
 */
@RemotingService
public class KinveyDataRemotingService implements IKinveyDataRemotingService {

    @Autowired
    private KinveyRestClient client;

    @Autowired
    private KinveyDataService kinveyDataService;

    @Autowired
    private SessionDataManager sessionDataManager;

    @GigaSpaceContext(name = "usSpace")
    private GigaSpace usSpaceNonClustered;
    private GigaSpace usSpaceClustered;

    @PostPrimary
    public void postPrimary() {
        usSpaceClustered = usSpaceNonClustered.getClustered();
    }

    /**
     * For tests usage only
     */
    public void setUsSpaceClustered(GigaSpace usSpaceClustered) {
        this.usSpaceClustered = usSpaceClustered.getClustered();
    }

    @Override
    public Response<User> login(Request request, String appKey, String userSecretJson) {
        Response<User> result = kinveyDataService.create(request, userSecretJson, User.class);

        User user = result.getEntity();
        usSpaceClustered.write(user);

        KinveyAsyncGetRequest[] asyncRequests = sessionDataManager.createAsyncRequests(user.getId(), user.getAuthtoken(), appKey, true);
        usSpaceClustered.writeMultiple(asyncRequests);

        return result;
    }

    @Override
    public <T extends Serializable> Response<T[]> load(Request request, Class<? extends T> entityType) {
        Response<T[]> response = kinveyDataService.load(request.getUrl(), request.getHeaders(), entityType);
        T[] result = response.getEntity();
        if (result != null && result.length > 0) {
            usSpaceClustered.writeMultiple(result);
        }
        return response;
    }

    @Override
    public <T extends Serializable> Response<T> loadOne(Request request, String authToken, Class<? extends T> entityType) {
        Response<T> response = kinveyDataService.loadOne(request.getUrl(), request.getHeaders(), entityType);
        T result = response.getEntity();
        if (result != null) {
            usSpaceClustered.write(result);
        }
        return response;
    }

    @Override
    public <T extends PrivateData> Response<T[]> load(Request request, String userId, Class<? extends T> entityType) {
        Response<T[]> response = kinveyDataService.load(request.getUrl(), request.getHeaders(), entityType);
        sessionDataManager.write(usSpaceNonClustered, response.getEntity(), true);
        return response;
    }

    @Override
    public Response<User> update(Request request, String userId, String userJson) {
        Response<User> result = kinveyDataService.update(request, userJson, User.class);

        User resultEntity = result.getEntity();

        IdQuery<User> query = new IdQuery<User>(User.TYPE, userId);
        ChangeSet update = new ChangeSet()
            .set(USERNAME_FIELD_NAME, resultEntity.getUsername())
            .set(COUNTRY_FIELD_NAME, resultEntity.getCountry())
            .set(PROFILE_FIELD_NAME, resultEntity.getProfile())
            .set(EMAIL_FIELD_NAME, resultEntity.getEmail())
            .set(ACL_FIELD_NAME, new DocumentProperties(resultEntity.getAcl()))
            .set(KMD_FIELD_NAME, new DocumentProperties(resultEntity.getKmd()))
            .set(MESSAGING_FIELD_NAME, new DocumentProperties(resultEntity.getMessaging()));

        usSpaceNonClustered.change(query, update);
        return result;
    }

    @Override
    public <T extends PrivateData> Response<T> create(Request request, String userId, String json, Class<? extends T> entityType) {
        Response<T> response = kinveyDataService.create(request, json, entityType);
        sessionDataManager.write(usSpaceNonClustered, response.getEntity(), true);
        return response;
    }

    @Override
    public <T extends PrivateData> Response<T> update(Request request, String userId, String json, Class<? extends T> entityType) {
        Response<T> response = kinveyDataService.update(request, json, entityType);
        sessionDataManager.write(usSpaceNonClustered, response.getEntity(), true);
        return response;
    }

    @Override
    public <T extends PrivateData> Response<Void> delete(Request request, String userId, T entityTemplate) {
        Response<Void> response = kinveyDataService.delete(request);
        usSpaceNonClustered.clear(entityTemplate);
        return response;
    }

    @Override
    public Response<String> load(Request request) {
        ResponseEntity<String> result = client.get(request.getUrl(), request.getUrlVars(), request.getHeaders());
        return new Response<String>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @Override
    public Response<String> create(Request request, String json) {
        ResponseEntity<String> result = client.post(request.getUrl(), json, request.getHeaders());
        return new Response<String>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @Override
    public Response<String> update(Request request, String json) {
        ResponseEntity<String> result = client.put(request.getUrl(), Collections.emptyMap(), json, request.getHeaders());
        return new Response<String>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }

    @Override
    public Response<String> delete(Request request) {
        ResponseEntity<String> result = client.delete(request.getUrl(), Collections.emptyMap(), request.getHeaders());
        return new Response<String>(result.getBody(), result.getHeaders(), result.getStatusCode());
    }
}
