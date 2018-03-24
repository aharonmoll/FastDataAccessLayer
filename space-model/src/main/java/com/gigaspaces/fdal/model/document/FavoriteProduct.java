package com.gigaspaces.fdal.model.document;

import java.util.Map;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class FavoriteProduct extends PrivateData {

    public static final String TYPE = "FavoriteProduct";
    public static final String RESOURCE_NAME = "favoriteProducts";

    public FavoriteProduct() {
        super(TYPE);
    }

    public FavoriteProduct(Map<String, Object> properties) {
        super(TYPE, properties);
    }
}
