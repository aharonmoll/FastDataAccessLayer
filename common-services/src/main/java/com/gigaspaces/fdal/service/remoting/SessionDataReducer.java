package com.gigaspaces.fdal.service.remoting;

import org.openspaces.remoting.SpaceRemotingInvocation;

import org.openspaces.remoting.SpaceRemotingResult;
import org.openspaces.remoting.RemoteResultReducer;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class SessionDataReducer<T extends Serializable> implements RemoteResultReducer<T, T> {
    
    private static final Logger LOGGER = Logger.getLogger(SessionDataReducer.class.getName());

    @Override
    public T reduce(SpaceRemotingResult<T>[] results, SpaceRemotingInvocation sri) throws Exception {
        for (SpaceRemotingResult<T> result : results) {
            Throwable exception = result.getException();
            if (exception != null) {
                LOGGER.warning("Exception occuried during loading session data from space: " + exception);
            } else {
                T entities = result.getResult();
                if (entities != null) {
                    return entities;
                }
            }
        }
        return null;
    }
}
