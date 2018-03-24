package com.gigaspaces.fdal.model;

import com.gigaspaces.fdal.model.document.PrivateData;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.springframework.http.HttpHeaders;

/**
 * @author Svitlana_Pogrebna
 *
 */
@SpaceClass
public class KinveyAsyncGetRequest extends Request {

    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private Class<?> entityType;
    private Boolean saveToSpace;
    private Boolean replicable;

    public KinveyAsyncGetRequest() {
    }

    public KinveyAsyncGetRequest(String url, HttpHeaders httpHeaders, String userId, Class<?> entityType) {
        this(url, httpHeaders, userId, entityType, false, true);
    }

    public KinveyAsyncGetRequest(String url, HttpHeaders httpHeaders, String userId, Class<?> entityType, Boolean saveToSpace, Boolean replicable) {
        super(url, httpHeaders);
        this.userId = userId;
        this.entityType = entityType;
        this.saveToSpace = saveToSpace;
        this.replicable = replicable;
    }

    @SpaceId(autoGenerate = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @SpaceRouting
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(Class<?> entityType) {
        this.entityType = entityType;
    }

    public Boolean getSaveToSpace() {
        return saveToSpace;
    }

    public void setSaveToSpace(Boolean saveToSpace) {
        this.saveToSpace = saveToSpace;
    }

    public Boolean isReplicable() {
        return replicable;
    }

    public void setReplicable(Boolean replicable) {
        this.replicable = replicable;
    }

    public boolean isSessionData() {
        return PrivateData.class.isAssignableFrom(entityType);
    }
}
