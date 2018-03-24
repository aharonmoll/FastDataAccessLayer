package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.fdal.model.document.User;

import com.j_spaces.core.client.SQLQuery;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.document.PrivateData;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;
import org.springframework.http.HttpHeaders;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RemotingService
public class SessionDataBroadcastingService implements ISessionDataBroadcastingService {

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    @Override
    public <T extends PrivateData> T[] loadByUserCountry(Class<T> clazz, String type, String userAuthToken, String url, HttpHeaders httpHeaders) {
        User user = gigaSpace.readIfExists(createUserIdCountryQuery(userAuthToken));
        if (user == null) {
            return null;
        }
        SQLQuery<T> dataQuery = new SQLQuery<>(type, "creator = ? AND country = ?");
        String userId = user.getId();
        dataQuery.setParameters(userId, user.getCountry());
        T[] data = gigaSpace.readMultiple(dataQuery);
        if (data != null && data.length > 0) {
            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, httpHeaders, userId, clazz, false, false);
            gigaSpace.write(kinveyNotifyRequest);
        }
        return data;
    }

    @Override
    public User loadUser(String userAuthToken, String url, HttpHeaders httpHeaders) {
        User user = gigaSpace.readIfExists(createUserQuery(userAuthToken));
        if (user != null) {
            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, httpHeaders, user.getId(), User.class);
            gigaSpace.write(kinveyNotifyRequest);
        }
        return user;
    }

    private SQLQuery<User> createUserQuery(String userAuthToken) {
        SQLQuery<User> userQuery = new SQLQuery<User>(User.TYPE, "_kmd.authtoken = ?");
        userQuery.setParameters(userAuthToken);
        return userQuery;
    }
    
    private SQLQuery<User> createUserIdCountryQuery(String userAuthToken) {
        SQLQuery<User> userQuery = createUserQuery(userAuthToken);
        userQuery.setProjections("_id", "country");
        return userQuery;
    }
}
