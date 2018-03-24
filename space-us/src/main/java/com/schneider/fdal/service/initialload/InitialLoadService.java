package com.gigaspaces.fdal.service.initialload;

import com.gigaspaces.fdal.model.document.Country;

import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.service.KinveyDataService;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoContext;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.core.space.mode.PostBackup;
import org.openspaces.core.space.mode.PostPrimary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class InitialLoadService {

    private static final Logger LOGGER = Logger.getLogger(InitialLoadService.class.getName());

    @GigaSpaceContext(name = "usSpace")
    private GigaSpace usSpace;

    @ClusterInfoContext
    private ClusterInfo clusterInfo;

    @Autowired
    private KinveyDataService kinveyDataService;

    @Value("${authorization.token}")
    private String authorizationToken;

    @Value("${application.key}")
    private String appKey;

    private AtomicBoolean onFirstDeploy = new AtomicBoolean(true);

    @PostBackup
    public void postBackupLoad() {
        onFirstDeploy.set(false);
    }

    @PostPrimary
    public void postPrimaryLoad() {
        if (onFirstDeploy.compareAndSet(true, false)) {
            LOGGER.info("Pre-loading data to the US space...");
            loadCountries();
        }
    }

    private void loadCountries() {
        try {
            Response<Country[]> countryResponse = kinveyDataService.load(Country.RESOURCE_NAME, appKey, authorizationToken, Country.class);
            Country[] allCountries = countryResponse.getEntity();
            if (allCountries != null && allCountries.length > 0) {
                Object[] countries = clusterInfo.getNumberOfInstances() > 1 ? Arrays.stream(allCountries)
                        .filter(c -> isRoutedToCurrentPartiton(c.getId())).toArray() : allCountries;
                if (countries.length > 0) {
                    usSpace.writeMultiple(countries);
                    LOGGER.info(String.format("%d country objects have been pre-loaded to the partition %d of the US space from Kinvey", countries.length, clusterInfo.getInstanceId() - 1));
                    return;
                }
            }
            LOGGER.warning(String.format("No country objects have been found to be pre-loaded to the US space from Kinvey"));
        } catch (HttpStatusCodeException exception) {
            LOGGER.severe(String.format("No country objects have been pre-loaded to the US space from Kinvey due to exception: " + exception));
        }
    }

    private boolean isRoutedToCurrentPartiton(String routingValue) {
        return (safeABS(routingValue.hashCode()) % clusterInfo.getNumberOfInstances()) == (clusterInfo.getInstanceId() - 1);
    }

    private int safeABS(int value) {
        return value == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(value);
    }
}
