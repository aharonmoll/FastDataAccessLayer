package com.gigaspaces.fdal.model.document;

import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class FavoriteDocument extends PrivateData {

    public static final String TYPE = "FavoriteDocument";
    public static final String RESOURCE_NAME = "favoriteDocuments";

    public FavoriteDocument() {
        super(TYPE);
    }

    public FavoriteDocument(Map<String, Object> properties) {
        super(TYPE, properties);
    }
}
