package com.gigaspaces.fdal.replication;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.cluster.IReplicationFilter;
import com.j_spaces.core.cluster.IReplicationFilterEntry;
import com.j_spaces.core.cluster.ReplicationOperationType;
import com.j_spaces.core.cluster.ReplicationPolicy;
import com.gigaspaces.fdal.model.OperationalCountry;
import com.gigaspaces.fdal.model.document.Country;
import com.gigaspaces.fdal.model.document.PrivateData;
import com.gigaspaces.fdal.model.document.User;
import org.apache.commons.lang.ArrayUtils;

import java.util.logging.Logger;

import static com.gigaspaces.fdal.model.document.PrivateData.REPLICABLE_FIELD_NAME;
import static com.gigaspaces.fdal.model.document.User.LOGGED_IN_WITH_FIELD_NAME;

public class ReplicationFilter implements IReplicationFilter {

    private static final Logger LOG = Logger.getLogger(ReplicationFilter.class.getName());

    private static final String CHINA_GATEWAY = "gateway:CHINA";

    @Override
    public void init(IJSpace space, String paramUrl, ReplicationPolicy replicationPolicy) {
        LOG.info("Replication Filter has been created");
    }

    @Override
    public void process(int direction, IReplicationFilterEntry replicationEntry, String replicationTargetName) {
        if (direction == FILTER_DIRECTION_OUTPUT && replicationTargetName.equals(CHINA_GATEWAY) && !isReplicable(replicationEntry)) {
            replicationEntry.discard();
        }
    }

    private boolean isReplicable(IReplicationFilterEntry replicationEntry) {
        return isUserReplicable(replicationEntry) || isPrivateDataReplicable(replicationEntry) || isPublicDataReplicable(replicationEntry);
    }

    private boolean isUserReplicable(IReplicationFilterEntry replicationEntry) {
        if (!User.TYPE.equals(replicationEntry.getClassName())) {
            return false;
        }
        ReplicationOperationType operation = replicationEntry.getOperationType();
        switch (operation) {
            case TAKE: return true;
            case WRITE: return replicationEntry.getFieldsValues() != null && OperationalCountry.CHINA.getValue().equals(replicationEntry.getFieldValue(LOGGED_IN_WITH_FIELD_NAME));
            case UPDATE: return true;
            case CHANGE: return true;
            default:
                LOG.severe("Unsupported user " + operation + " replication operation has been rejected");
                return false;
        }
    }

    private boolean isPrivateDataReplicable(IReplicationFilterEntry replicationEntry) {
        return ArrayUtils.contains(replicationEntry.getSuperClassesNames(), PrivateData.TYPE) 
                && (replicationEntry.getFieldsValues() != null && Boolean.TRUE.equals(replicationEntry.getFieldValue(REPLICABLE_FIELD_NAME)) || replicationEntry.getOperationType() == ReplicationOperationType.TAKE);
    }

    private boolean isPublicDataReplicable(IReplicationFilterEntry replicationEntry) {
        return Country.TYPE.equals(replicationEntry.getClassName()); 
    }

    @Override
    public void close() {
        LOG.info("Replication Filter has been closed");
    }
}