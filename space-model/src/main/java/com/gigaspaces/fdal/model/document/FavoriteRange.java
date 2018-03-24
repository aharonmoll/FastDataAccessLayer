package com.gigaspaces.fdal.model.document;

import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class FavoriteRange extends PrivateData {

    public static final String TYPE = "FavoriteRange";
    public static final String RESOURCE_NAME = "favoriteRanges";

    public FavoriteRange() {
        super(TYPE);
    }

    public FavoriteRange(Map<String, Object> properties) {
        super(TYPE, properties);
    }
}
