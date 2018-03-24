package com.gigaspaces.fdal.model.document;

import com.gigaspaces.document.SpaceDocument;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 *
 */
public abstract class PrivateData extends SpaceDocument {

    public static final String TYPE = "PrivateData";

    public PrivateData() {
        super(TYPE);
    }

    public PrivateData(String type) {
        super(type);
    }

    public PrivateData(String type, Map<String, Object> properties) {
        super(type, properties);
    }

    public static final String REPLICABLE_FIELD_NAME = "replicable";
    private static final String ID_FIELD_NAME = "_id";
    private static final String ACL_FIELD_NAME = "_acl";
    private static final String COUNTRY_FIELD_NAME = "country";
    public static final String CREATOR_FIELD_NAME = "creator";
    
    public String getId() {
        return getProperty(ID_FIELD_NAME);
    }

    public void setId(String id) {
        setProperty(ID_FIELD_NAME, id);
    }

    public Map<String, Object> getAcl() {
        return getProperty(ACL_FIELD_NAME);
    }

    public void setAcl(Map<String, Object> acl) {
        setProperty(ACL_FIELD_NAME, acl);
        setCreator(acl.get(CREATOR_FIELD_NAME));
    }
    
    public String getCreator() {
        return getProperty(CREATOR_FIELD_NAME);
    }

    public void setCreator(Object creator) {
        setProperty(CREATOR_FIELD_NAME, creator);
    }

    public Boolean isReplicable() {
        return getProperty(REPLICABLE_FIELD_NAME);
    }

    public void setReplicable(Boolean isReplicable) {
        setProperty(REPLICABLE_FIELD_NAME, isReplicable);
    }

    public String getCountry() {
        return getProperty(COUNTRY_FIELD_NAME);
    }

    public void setCountry(String country) {
        setProperty(COUNTRY_FIELD_NAME, country);
    }
    
    public Map<String, Object> toJsonProperties() {
        Map<String, Object> jsonView = new HashMap<>(getProperties());
        jsonView.remove(REPLICABLE_FIELD_NAME);
        jsonView.remove(CREATOR_FIELD_NAME);
        return jsonView;
    }
}
