package com.gigaspaces.fdal.utils;

/**
 * @author Svitlana_Pogrebna
 *
 */
public enum AuthorizationType {

    BASIC("Basic "),
    KINVEY("Kinvey ");
    
    private final String prefix;
    
    private AuthorizationType(String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return prefix;
    }
}
