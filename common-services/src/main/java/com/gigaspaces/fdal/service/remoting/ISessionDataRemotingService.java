package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.fdal.model.document.User;

import org.springframework.http.HttpHeaders;
import org.openspaces.remoting.Routing;

/**
 * @author Svitlana_Pogrebna
 *
 */
public interface ISessionDataRemotingService {

    User loadUser(@Routing String userId, String url, HttpHeaders headers);

    void createUser(@Routing String userId, User user, String appKey);
}
