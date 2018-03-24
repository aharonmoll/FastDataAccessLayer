package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.utils.SessionDataManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RemotingService
public class SessionDataRemotingService implements ISessionDataRemotingService {

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    @Autowired
    private SessionDataManager sessionDataManager;

    @Override
    public void createUser(String userId, User user, String appKey) {
        gigaSpace.write(user);

        KinveyAsyncGetRequest[] asyncRequests = sessionDataManager.createAsyncRequests(userId, user.getAuthtoken(), appKey, false);
        gigaSpace.writeMultiple(asyncRequests);
    }

    @Override
    public User loadUser(String userId, String url, HttpHeaders httpHeaders) {
        User userTemplate = new User();
        userTemplate.setId(userId);
        User user = gigaSpace.readIfExists(userTemplate);
        if (user != null) {
            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, httpHeaders, user.getId(), User.class);
            gigaSpace.write(kinveyNotifyRequest);
        }
        return user;
    }
}
