package com.gigaspaces.fdal.utils;

import com.gigaspaces.fdal.model.document.User;

import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.document.*;
import org.openspaces.core.GigaSpace;
import org.springframework.http.HttpHeaders;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class SessionDataManager {

    public KinveyAsyncGetRequest[] createAsyncRequests(String userId, String userAuthToken, String appKey, Boolean replicate) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, AuthorizationType.KINVEY.getPrefix() + userAuthToken);

        String urlPath = "/appdata/" + appKey + "/";
        KinveyAsyncGetRequest[] asyncRequests = {
            new KinveyAsyncGetRequest(urlPath + FavoriteRange.RESOURCE_NAME, headers, userId, FavoriteRange.class, true, replicate),
            new KinveyAsyncGetRequest(urlPath + FavoriteDocument.RESOURCE_NAME, headers, userId, FavoriteDocument.class, true, replicate),
            new KinveyAsyncGetRequest(urlPath + FavoriteProduct.RESOURCE_NAME, headers, userId, FavoriteProduct.class, true, replicate),
            new KinveyAsyncGetRequest(urlPath + FavoriteFAQ.RESOURCE_NAME, headers, userId, FavoriteFAQ.class, true, replicate)
        };
        return asyncRequests;
    }

    public void write(GigaSpace spaceProxy, PrivateData[] data, Boolean replicable) {
        if (data != null && data.length > 0) {
            for (PrivateData o : data) {
                o.setReplicable(replicable);
                o.setCreator(o.getAcl().get(PrivateData.CREATOR_FIELD_NAME));
            }
            spaceProxy.writeMultiple(data);
        }
    }

    public void write(GigaSpace spaceProxy, PrivateData data, Boolean replicable) {
        if (data != null) {
            data.setReplicable(replicable);
            data.setCreator(data.getAcl().get(PrivateData.CREATOR_FIELD_NAME));
            spaceProxy.write(data);
        }
    }

    public void remove(GigaSpace spaceProxy, String userId) {
        User user = new User();
        user.setId(userId);
        spaceProxy.clear(user);

        FavoriteRange range = new FavoriteRange();
        range.setCreator(userId);
        spaceProxy.clear(range);

        FavoriteDocument document = new FavoriteDocument();
        document.setCreator(userId);
        spaceProxy.clear(document);

        FavoriteProduct product = new FavoriteProduct();
        product.setCreator(userId);
        spaceProxy.clear(product);

        FavoriteFAQ faq = new FavoriteFAQ();
        faq.setCreator(userId);
        spaceProxy.clear(faq);
    }
}
