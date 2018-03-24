package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.Country;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.service.KinveyDataService;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
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
    private KinveyDataService kinveyDataService;

    @Autowired
    private GigaSpace usSpace;

    @RequestMapping(value = "/{appKey}/" + Country.RESOURCE_NAME, method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> get(@PathVariable("appKey") String appKey, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestHeader HttpHeaders headers) {
        User user = checkUnauthorized(usSpace, authorization);

        String url = String.format(COUNTRY_URL_FORMAT, appKey);

        Country[] countries = usSpace.readMultiple(new Country());
        if (countries != null && countries.length > 0) {
            LOGGER.info(countries.length + " country objects have been read from the US space. Notifying Kinvey...");

            KinveyAsyncGetRequest kinveyNotifyRequest = new KinveyAsyncGetRequest(url, headers, user.getId(), Country.class);
            usSpace.write(kinveyNotifyRequest);

            return ok(Arrays.stream(countries).map(Country :: getProperties).toArray());
        } else {
            Response<Country[]> countryResponse = kinveyDataService.load(url, headers, Country.class);
            countries = countryResponse.getEntity();

            if (countries != null && countries.length > 0) {
                usSpace.writeMultiple(countries);
            }

            LOGGER.warning(String.format("%d country objects have been read from Kinvey and saved to the US space. Kinvey request: GET %s", countries.length, url));

            return countryResponse.toRestResponse();
        }
    }
}
