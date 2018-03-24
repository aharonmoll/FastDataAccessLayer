package com.gigaspaces.fdal.service.remoting;

import com.gigaspaces.fdal.model.document.PrivateData;
import com.gigaspaces.fdal.model.document.User;
import org.springframework.http.HttpHeaders;

/**
 * @author Svitlana_Pogrebna
 *
 */
public interface ISessionDataBroadcastingService {

    User loadUser(String userAuthToken, String url, HttpHeaders httpHeaders);

    <T extends PrivateData> T[] loadByUserCountry(Class<T> clazz, String type, String userAuthToken, String url, HttpHeaders httpHeaders);
}
