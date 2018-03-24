package com.gigaspaces.fdal.model.document;

import com.gigaspaces.document.DocumentProperties;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.fdal.model.OperationalCountry;

import java.util.Map;

public class User extends SpaceDocument {

    private static final long serialVersionUID = 1L;

    public static final String ID_FIELD_NAME = "_id";
    public static final String COUNTRY_FIELD_NAME = "country";
    public static final String PROFILE_FIELD_NAME = "profile";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String USERNAME_FIELD_NAME = "username";
    public static final String KMD_FIELD_NAME = "_kmd";
    public static final String AUTHTOKEN_FIELD_NAME = "authtoken";
    public static final String ACL_FIELD_NAME = "_acl";
    public static final String MESSAGING_FIELD_NAME = "_messaging";
    public static final String LOGGED_IN_WITH_FIELD_NAME = "loggedInWith";

    public static final String TYPE = "User";

    public User() {
        super(TYPE);
    }

    public User(Map<String, Object> properties) {
        super(TYPE, properties);
    }
    
    public String getId() {
        return getProperty(ID_FIELD_NAME);
    }

    public void setId(String id) {
        setProperty(ID_FIELD_NAME, id);
    }

    public String getUsername() {
        return getProperty(USERNAME_FIELD_NAME);
    }

    public void setUsername(String username) {
        setProperty(USERNAME_FIELD_NAME, username);
    }

    public Map<String, Object> getKmd() {
        return getProperty(KMD_FIELD_NAME);
    }

    public void setKmd(Map<String, Object> kmd) {
        setProperty(KMD_FIELD_NAME, kmd);
    }
    
    public String getAuthtoken() {
        Map<String, Object> kmd = getKmd();
        return kmd == null ? null : (String)kmd.get(AUTHTOKEN_FIELD_NAME);
    }

    public void setAuthtoken(String authtoken) {
        Map<String, Object> kmd = getKmd();
        if (kmd == null) {
            kmd = new DocumentProperties();
            setKmd(kmd);
        }
        kmd.put(AUTHTOKEN_FIELD_NAME, authtoken);
    }

    public String getProfile() {
        return getProperty(PROFILE_FIELD_NAME);
    }

    public void setProfile(String profile) {
        setProperty(PROFILE_FIELD_NAME, profile);
    }

    public String getEmail() {
        return getProperty(EMAIL_FIELD_NAME);
    }

    public void setEmail(String email) {
        setProperty(EMAIL_FIELD_NAME, email);
    }

    public String getCountry() {
        return getProperty(COUNTRY_FIELD_NAME);
    }

    public void setCountry(String country) {
        setProperty(COUNTRY_FIELD_NAME, country);
    }

    public OperationalCountry getLoggedInWith() {
        return OperationalCountry.fromValue(getProperty(LOGGED_IN_WITH_FIELD_NAME));
    }

    public void setLoggedInWith(OperationalCountry loggedInWith) {
        this.setProperty(LOGGED_IN_WITH_FIELD_NAME, loggedInWith.getValue());
    }

    public Map<String, Object> getAcl() {
        return getProperty(ACL_FIELD_NAME);
    }

    public void setAcl(Map<String, Object> acl) {
        setProperty(ACL_FIELD_NAME, acl);
    }

    public Map<String, Object> getMessaging() {
        return getProperty(MESSAGING_FIELD_NAME);
    }

    public void setMessaging(Map<String, Object> messaging) {
        setProperty(MESSAGING_FIELD_NAME, messaging);
    }
}
