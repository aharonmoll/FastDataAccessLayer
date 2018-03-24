package com.gigaspaces.fdal.model.document;

import java.util.Map;

import com.gigaspaces.document.SpaceDocument;

/**
 * {"_id":"Saint-Vincent and the Grenadines",
 * "name":"Saint-Vincent and the Grenadines", 
 * "code":"VC", 
 * "languageIOS":"EN",
 * "languageAndroid":"EN", 
 * "faqLocaleCode":"SE_GLOBAL", 
 * "language":"en_GB"}
 * 
 * @author Svitlana_Pogrebna
 *
 */

public class Country extends SpaceDocument {

    public static final String TYPE = "Country";
    public static final String RESOURCE_NAME = "countries";

    public static final String ID_FIELD_NAME = "_id";
    public static final String NAME_FIELD_NAME = "name";
    public static final String CODE_FIELD_NAME = "code";
    public static final String LANGUAGE_IOS_FIELD_NAME = "languageIOS";
    public static final String LANGUAGE_ANDROID_FIELD_NAME = "languageAndroid";
    public static final String LANGUAGE_FIELD_NAME = "language";
    public static final String FAQ_LOCALE_CODE_FIELD_NAME = "faqLocaleCode";

    public Country() {
        super(TYPE);
    }

    public Country(Map<String, Object> properties) {
        super(TYPE, properties);
    }

    public String getId() {
        return getProperty(ID_FIELD_NAME);
    }

    public void setId(String id) {
        setProperty(ID_FIELD_NAME, id);
    }

    public String getName() {
        return getProperty(NAME_FIELD_NAME);
    }

    public void setName(String name) {
        setProperty(NAME_FIELD_NAME, name);
    }

    public String getCode() {
        return getProperty(CODE_FIELD_NAME);
    }

    public void setCode(String code) {
        setProperty(CODE_FIELD_NAME, code);
    }

    public String getLanguage() {
        return getProperty(LANGUAGE_FIELD_NAME);
    }

    public void setLanguage(String language) {
        this.setProperty(LANGUAGE_FIELD_NAME, language);
    }

    public String getLanguageIOS() {
        return getProperty(LANGUAGE_IOS_FIELD_NAME);
    }

    public void setLanguageIOS(String languageIOS) {
        setProperty(LANGUAGE_IOS_FIELD_NAME, languageIOS);
    }

    public String getFaqLocaleCode() {
        return getProperty(FAQ_LOCALE_CODE_FIELD_NAME);
    }

    public void setFaqLocaleCode(String faqLocalCode) {
        setProperty(FAQ_LOCALE_CODE_FIELD_NAME, faqLocalCode);
    }

    public String getLanguageAndroid() {
        return getProperty(LANGUAGE_ANDROID_FIELD_NAME);
    }

    public void setLanguageAndroid(String languageAndroid) {
        this.setProperty(LANGUAGE_ANDROID_FIELD_NAME, languageAndroid);
    }
}
