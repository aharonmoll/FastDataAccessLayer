package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.document.Country;

import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.model.document.FavoriteRange;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.service.async.KinveyDataAsyncService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.SimplePollingContainerConfigurer;
import org.openspaces.events.polling.SimplePollingEventListenerContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Svitlana_Pogrebna
 *
 */
@ContextConfiguration("/test-context.xml")
public class TokenExpiryTest extends AbstractControllerTest {

    private static final String APP_KEY = "kid_Zk-fAAEhwg";
    private static final String COUNTRIES_PATH = "/appdata/" + APP_KEY + "/countries";
    private static final String FR_PATH = "/appdata/" + APP_KEY + "/favoriteRanges";

    private KinveyDataAsyncService kinveyDataAsyncService;
    private SimplePollingEventListenerContainer pollingEventListenerContainer;

    @Override
    @Before
    public void setup() {
        super.setup();
        for (ApplicationContext ac : usSpaceCluster.getMembersContexts()) {
            kinveyDataAsyncService = ac.getBean(KinveyDataAsyncService.class); // any instance fits
        }
        this.pollingEventListenerContainer = new SimplePollingContainerConfigurer(usSpace)
        .template(new KinveyAsyncGetRequest())
        .eventListenerAnnotation(new Object() {
            @SpaceDataEvent
            public void eventHappened(KinveyAsyncGetRequest data) {
                kinveyDataAsyncService.eventProcess(data);
            }})
        .receiveTimeout(0)
        .pollingContainer();
        pollingEventListenerContainer.start();
    }

    @Override
    @After
    public void destroy() {
        super.destroy();
        pollingEventListenerContainer.stop();
    }

    @Test
    public void getCountries_tokenExpried() throws Exception {
        tokenExpired(COUNTRIES_PATH, Country.class);
    }

    @Test
    public void getFavoriteRanges_tokenExpried() throws Exception {
        tokenExpired(FR_PATH, FavoriteRange.class);
    }

    private void tokenExpired(String expiredUrlPath, Class<?> expiredEntityType) throws Exception {
        User user = login();

        usSpace.writeMultiple(createCountries().toArray());
        usSpace.writeMultiple(createFavoriteRanges(user).toArray());

        waitForEmptyReplicationBacklog();

        mockKinvey401Response(kinveyHost + expiredUrlPath, HttpMethod.GET);

        String authtoken = user.getAuthtoken();
        HttpHeaders headers = createHeaders("Kinvey " + authtoken);

        usSpace.write(new KinveyAsyncGetRequest(expiredUrlPath, headers, user.getId(), expiredEntityType));

        // wait for async request being processed
        Thread.sleep(3000l);

        assertNull(usSpace.readById(User.class, user.getId()));
        assertTrue(usSpace.readMultiple(new Country()).length > 0);
        assertTrue(usSpace.readMultiple(new FavoriteRange()).length == 0);

        waitForEmptyReplicationBacklog();

        assertNull(chinaSpace.readById(User.class, user.getId()));
        assertTrue(chinaSpace.readMultiple(new Country()).length > 0);
        assertTrue(chinaSpace.readMultiple(new FavoriteRange()).length == 0);
    }
}
