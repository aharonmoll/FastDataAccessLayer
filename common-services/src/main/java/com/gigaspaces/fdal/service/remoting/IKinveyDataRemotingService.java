package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.fdal.model.document.User;

import com.gigaspaces.fdal.model.document.PrivateData;
import com.gigaspaces.fdal.model.Response;
import org.openspaces.remoting.Routing;
import com.gigaspaces.fdal.model.Request;

import java.io.Serializable;

/**
 * @author Svitlana_Pogrebna
 *
 */
public interface IKinveyDataRemotingService {

    Response<User> login(Request request, String appKey, String userSecretJson);

    Response<User> update(Request request, @Routing String userId, String userJson);

    <T extends Serializable> Response<T[]> load(Request request, Class<? extends T> entityType);

    <T extends Serializable> Response<T> loadOne(Request request, String userAuthToken, Class<? extends T> entityType);

    <T extends PrivateData> Response<T[]> load(Request request, @Routing String userId, Class<? extends T> entityType);

    <T extends PrivateData> Response<T> create(Request request, @Routing String userId, String json, Class<? extends T> entityType);

    <T extends PrivateData> Response<T> update(Request request, @Routing String userId, String json, Class<? extends T> entityType);

    <T extends PrivateData> Response<Void> delete(Request request, @Routing String userId, T entityTemplate);
    
    Response<String> load(Request request);

    Response<String> create(Request request, String json);

    Response<String> update(Request request, String json);

    Response<String> delete(Request request);
}
