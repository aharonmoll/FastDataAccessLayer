package com.gigaspaces.fdal.model;

import com.fasterxml.jackson.annotation.JsonValue;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Svitlana_Pogrebna
 *
 */
public enum OperationalCountry {
    CHINA("China"), USA("USA");

    private final String value;

    private OperationalCountry(String value) {
        this.value = value;
    }

    @JsonCreator
    public static OperationalCountry fromValue(String value) {
        for (OperationalCountry country : values()) {
            if (country.getValue().equals(value)) {
                return country;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
