package com.gigaspaces.fdal.model.document;

import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class FavoriteFAQ extends PrivateData {

    public static final String TYPE = "FavoriteFAQ";
    public static final String RESOURCE_NAME = "favoriteFaqs";

    public FavoriteFAQ() {
        super(TYPE);
    }

    public FavoriteFAQ(Map<String, Object> properties) {
        super(TYPE, properties);
    }
}