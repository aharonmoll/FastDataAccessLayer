package com.gigaspaces.fdal.service.async;

import com.j_spaces.core.client.SQLQuery;
import com.gigaspaces.fdal.kinvey.KinveyRestClient;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import com.gigaspaces.fdal.model.Response;
import com.gigaspaces.fdal.model.document.PrivateData;
import com.gigaspaces.fdal.service.KinveyDataService;
import com.gigaspaces.fdal.utils.SessionDataManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoContext;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.events.EventDriven;
import org.openspaces.events.EventTemplate;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.polling.Polling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

@EventDriven
@Polling
public class KinveyDataAsyncService {

    @Autowired
    private KinveyDataService kinveyDataService;

    @Autowired
    private KinveyRestClient kinveyRestClient;

    @Autowired
    private SessionDataManager sessionDataManager;
    
    @GigaSpaceContext(name = "usSpace")
    private GigaSpace usSpace;

    @ClusterInfoContext
    private ClusterInfo clusterInfo;

    private Integer routing;

    private static final String LOG_MSG = "Async Kinvey GET %s request returned %d %s";

    private static final Logger LOGGER = Logger.getLogger(KinveyDataAsyncService.class.getName());

    @PostConstruct
    public void initRouting() {
        Integer instanceId = clusterInfo.getInstanceId();
        if (instanceId == null) {
            routing = 0;
            LOGGER.warning("Failed to get partition instance id. Starting polling container to handle async requests to Kinvey with the default routing value: " + routing + ". Check the primary partition count to be 1.");
        } else {
            routing = instanceId - 1;
            LOGGER.info("Starting " + this.getClass().getSimpleName() + " polling container collocated with partition " + routing);
        }
    }

    @EventTemplate
    public SQLQuery<KinveyAsyncGetRequest> template() {
        SQLQuery<KinveyAsyncGetRequest> query = new SQLQuery<>(KinveyAsyncGetRequest.class, "");
        query.setRouting(routing);
        return query;
    }

    @SpaceDataEvent
    public void eventProcess(KinveyAsyncGetRequest request) {
        HttpHeaders requestHeaders = request.getHeaders();
        try {
            String url = request.getUrl();
            HttpStatus httpStatus;
            if (Boolean.TRUE.equals(request.getSaveToSpace())) {
                Response<?> response = kinveyDataService.load(url, requestHeaders, request.getEntityType());
                if (request.isSessionData()) {
                    PrivateData[] data = (PrivateData[]) response.getEntity();
                    sessionDataManager.write(usSpace, data, request.isReplicable());
                }
                httpStatus = response.getCode();
            } else {
                ResponseEntity<String> response = kinveyRestClient.get(url, requestHeaders);
                httpStatus = response.getStatusCode();
            }
            LOGGER.info(String.format(LOG_MSG, url, httpStatus.value(), httpStatus.getReasonPhrase()));
        } catch (HttpClientErrorException clientException) {
            HttpStatus httpStatus = clientException.getStatusCode();
            if (HttpStatus.UNAUTHORIZED == httpStatus) {
                LOGGER.info(String.format(LOG_MSG, request.getUrl(), httpStatus.value(), httpStatus.getReasonPhrase()));
                sessionDataManager.remove(usSpace, request.getUserId());
            } else {
                LOGGER.warning(String.format(LOG_MSG, request.getUrl(), httpStatus.value(), httpStatus.getReasonPhrase()));
            }
        } catch (HttpServerErrorException serverException) {
            HttpStatus httpStatus = serverException.getStatusCode();
            LOGGER.warning(String.format(LOG_MSG, request.getUrl(), httpStatus.value(), httpStatus.getReasonPhrase()));
        }
    }
}
