package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.Request;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.Country;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.service.remoting.IKinveyDataRemotingService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.logging.Logger;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RestController
@RequestMapping("/appdata")
public class CountryController extends AbstractController {

    private static final String COUNTRY_URL_FORMAT = "/appdata/%s/countries";

    private static final Logger LOGGER = Logger.getLogger(CountryController.class.getName());

    @Autowired
    private IKinveyDataRemotingService kinveyDataRemotingService;

    @Autowired
    @Qualifier("chinaSpace")
    private GigaSpace chinaSpace;

    @Autowired
    @Qualifier("usSpace")
    private GigaSpace usSpace;

    @RequestMapping(value = "/{appKey}/" + Country.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> get(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        User user = checkUnauthorized(chinaSpace, authorization);

        Country[] countries = chinaSpace.readMultiple(new Country());

        String url = String.format(COUNTRY_URL_FORMAT, appKey);
        if (countries != null && countries.length > 0) {
            LOGGER.info(countries.length + " country objects have been read from the China space. Notifying Kinvey...");

            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, headers, user.getId(), Country.class);
            usSpace.write(kinveyNotifyRequest);

            return ok(Arrays.stream(countries).map(Country :: getProperties).toArray());
        } else {
            Response<Country[]> countriesResponse = kinveyDataRemotingService.load(new Request(url, headers), Country.class);

            LOGGER.warning(String.format("%d country objects have been read from Kinvey. Kinvey request: GET %s", countriesResponse.getEntity().length, url));

            return countriesResponse.toRestResponse();
        }
    }
}
