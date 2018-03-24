package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.kinvey.KinveyRestClient;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.*;
import com.gigaspaces.fdal.service.KinveyDataService;
import com.gigaspaces.fdal.service.remoting.ISessionDataBroadcastingService;
import com.gigaspaces.fdal.utils.SessionDataManager;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Logger;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
@RequestMapping("/appdata")
public class FavoriteDataController extends AbstractController {

    @Autowired
    private ISessionDataBroadcastingService sessionDataBroadcastingService;

    @Autowired
    private SessionDataManager sessionDataManager;
    
    @Autowired
    private KinveyDataService kinveyDataService;

    @Autowired
    private KinveyRestClient client;

    @Autowired
    private GigaSpace usSpace;

    private static final Logger LOGGER = Logger.getLogger(FavoriteDataController.class.getName());

    private static final String URL_WITH_ID_PATH = "%s/{%s}";
    private static final String ID_KEY = "id";
    
    private static final String RANGES_URL_FORMAT = "/appdata/%s/" + FavoriteRange.RESOURCE_NAME;
    private static final String PRODUCTS_URL_FORMAT = "/appdata/%s/" + FavoriteProduct.RESOURCE_NAME;
    private static final String DOCUMENTS_URL_FORMAT = "/appdata/%s/" + FavoriteDocument.RESOURCE_NAME;
    private static final String FAQ_URL_FORMAT = "/appdata/%s/" + FavoriteFAQ.RESOURCE_NAME;

    /* Favorite ranges */
    @RequestMapping(value = "/{appKey}/" + FavoriteRange.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getRanges(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(RANGES_URL_FORMAT, appKey);
        return get(url, authorization, headers, FavoriteRange.class, FavoriteRange.TYPE);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteRange.RESOURCE_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> createRange(@PathVariable("appKey") String appKey, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(RANGES_URL_FORMAT, appKey);
        return create(url, json, headers, FavoriteRange.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteRange.RESOURCE_NAME + "/{rangeId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> updateRange(@PathVariable("appKey") String appKey, @PathVariable("rangeId") String rangeId, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(RANGES_URL_FORMAT, appKey);
        return update(url, rangeId, json, headers, FavoriteRange.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteRange.RESOURCE_NAME + "/{rangeId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> deleteRange(@PathVariable("appKey") String appKey, @PathVariable("rangeId") String rangeId, @RequestHeader HttpHeaders headers) {
        String url = String.format(RANGES_URL_FORMAT, appKey);
        return delete(url, rangeId, headers, new FavoriteRange());
    }

    /* Favorite products */
    @RequestMapping(value = "/{appKey}/" + FavoriteProduct.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getProducts(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(PRODUCTS_URL_FORMAT, appKey);
        return get(url, authorization, headers, FavoriteProduct.class, FavoriteProduct.TYPE);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteProduct.RESOURCE_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> createProduct(@PathVariable("appKey") String appKey, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(PRODUCTS_URL_FORMAT, appKey);
        return create(url, json, headers, FavoriteProduct.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteProduct.RESOURCE_NAME + "/{productId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> updateProduct(@PathVariable("appKey") String appKey, @PathVariable("productId") String productId, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(PRODUCTS_URL_FORMAT, appKey);
        return update(url, productId, json, headers, FavoriteProduct.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteProduct.RESOURCE_NAME + "/{productId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> deleteProduct(@PathVariable("appKey") String appKey, @PathVariable("productId") String productId, @RequestHeader HttpHeaders headers) {
        String url = String.format(PRODUCTS_URL_FORMAT, appKey);
        return delete(url, productId, headers, new FavoriteProduct());
    }
    
    /* Favorite documents */
    @RequestMapping(value = "/{appKey}/" + FavoriteDocument.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getDocuments(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(DOCUMENTS_URL_FORMAT, appKey);
        return get(url, authorization, headers, FavoriteDocument.class, FavoriteDocument.TYPE);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteDocument.RESOURCE_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> createDocument(@PathVariable("appKey") String appKey, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(DOCUMENTS_URL_FORMAT, appKey);
        return create(url, json, headers, FavoriteDocument.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteDocument.RESOURCE_NAME + "/{documentId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> updateDocument(@PathVariable("appKey") String appKey, @PathVariable("documentId") String documentId, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(DOCUMENTS_URL_FORMAT, appKey);
        return update(url, documentId, json, headers, FavoriteDocument.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteDocument.RESOURCE_NAME + "/{documentId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> deleteDocument(@PathVariable("appKey") String appKey, @PathVariable("documentId") String documentId, @RequestHeader HttpHeaders headers) {
        String url = String.format(DOCUMENTS_URL_FORMAT, appKey);
        return delete(url, documentId, headers, new FavoriteDocument());
    }

    /* Favorite FAQs */
    @RequestMapping(value = "/{appKey}/" + FavoriteFAQ.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> getFAQs(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        String url = String.format(FAQ_URL_FORMAT, appKey);
        return get(url, authorization, headers, FavoriteFAQ.class, FavoriteFAQ.TYPE);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteFAQ.RESOURCE_NAME, method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> createFAQ(@PathVariable("appKey") String appKey, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(FAQ_URL_FORMAT, appKey);
        return create(url, json, headers, FavoriteFAQ.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteFAQ.RESOURCE_NAME + "/{faqId}", method = RequestMethod.PUT, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> updateFAQ(@PathVariable("appKey") String appKey, @PathVariable("faqId") String faqId, @RequestBody String json, @RequestHeader HttpHeaders headers) {
        String url = String.format(FAQ_URL_FORMAT, appKey);
        return update(url, faqId, json, headers, FavoriteFAQ.class);
    }

    @RequestMapping(value = "/{appKey}/" + FavoriteFAQ.RESOURCE_NAME + "/{faqId}", method = RequestMethod.DELETE, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> deleteFAQ(@PathVariable("appKey") String appKey, @PathVariable("faqId") String faqId, @RequestHeader HttpHeaders headers) {
        String url = String.format(FAQ_URL_FORMAT, appKey);
        return delete(url, faqId, headers, new FavoriteFAQ());
    }

    private ResponseEntity<?> get(String url, String authorization, HttpHeaders headers, Class<? extends PrivateData> clazz, String type) {
        PrivateData[] data = sessionDataBroadcastingService.loadByUserCountry(clazz, type, extractKinveyToken(authorization), url, headers);

        if (data != null && data.length > 0) {
            LOGGER.info(data.length + " " + clazz.getSimpleName() + " objects have been read from the US space. Notifying Kinvey...");
            return ok(Arrays.stream(data).map(PrivateData :: toJsonProperties).toArray());
        } else {
            Response<PrivateData[]> response = kinveyDataService.load(url, headers, clazz);
            data = response.getEntity();
            sessionDataManager.write(usSpace, data, false);

            LOGGER.warning(String.format("%d %s objects have been saved to the US space. Kinvey request: GET %s", data.length, clazz.getSimpleName(), url));
            return response.toRestResponse();
        }
    }

    private <T extends PrivateData> ResponseEntity<String> create(String url, String json, HttpHeaders headers, Class<T> entityType) {
        Response<T> response = kinveyDataService.create(new Request(url, headers), json, entityType);
        LOGGER.info(String.format("%s entity has been created. Kinvey request: POST %s.", entityType.getSimpleName(), url));

        sessionDataManager.write(usSpace, response.getEntity(), false);

        return response.toRestResponse();
    }

    private <T extends PrivateData> ResponseEntity<String> update(String url, String id, String json, HttpHeaders headers, Class<T> entityType) {
        String urlPath = String.format(URL_WITH_ID_PATH, url, ID_KEY);
        Response<T> response = kinveyDataService.update(new Request(urlPath, Collections.singletonMap(ID_KEY, id), headers), json, entityType);
        LOGGER.info(String.format("%s entity with id %s has been updated. Kinvey request: PUT %s.", entityType.getSimpleName(), id, urlPath));

        sessionDataManager.write(usSpace, response.getEntity(), false);

        return response.toRestResponse();
    }
    
    private <T extends PrivateData> ResponseEntity<String> delete(String url, String id, HttpHeaders headers, T entityTemplate) {
        String urlPath = String.format(URL_WITH_ID_PATH, url, ID_KEY);
        Response<?> response = kinveyDataService.delete(new Request(urlPath, Collections.singletonMap(ID_KEY, id), headers));
        
        entityTemplate.setId(id);
        usSpace.clear(entityTemplate);

        LOGGER.info(String.format("%s entity with id %s has been deleted. Kinvey request: DELETE %s.", entityTemplate.getClass().getSimpleName(), id, urlPath));

        return response.toRestResponse();
    }
}
