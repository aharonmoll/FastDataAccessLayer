package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.document.User;

import com.gigaspaces.query.IdQuery;
import com.gigaspaces.fdal.model.document.*;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Svitlana_Pogrebna
 *
 */
@ContextConfiguration("/test-context.xml")
public class FavoriteDataControllerTest extends AbstractControllerTest {

    private static final String favoriteRangesPath = "/appdata/" + appKey + "/favoriteRanges";
    private static final String favoriteDocumentsPath = "/appdata/" + appKey + "/favoriteDocuments";
    private static final String favoriteProductsPath = "/appdata/" + appKey + "/favoriteProducts";
    private static final String favoriteFAQsPath = "/appdata/" + appKey + "/favoriteFaqs";

    @Autowired
    private FavoriteDataController favoriteDataController;

    @Test
    public void getFavoriteRangesFromKinvey_succeed() throws Exception {
        User user = login();
        List<FavoriteRange> favoriteRanges = createFavoriteRanges(user);
        ResultActions result = getFromKinvey_succeed(favoriteRanges, favoriteRangesPath);

        validateFavoriteRangesResponse(favoriteRanges, result);

        validateFavoriteRanges(favoriteRanges, usSpace.readMultiple(new FavoriteRange()));
        waitForEmptyReplicationBacklog();
        validateFavoriteRanges(favoriteRanges, chinaSpace.readMultiple(new FavoriteRange()));
    }

    @Test
    public void getFavoriteProductsFromKinvey_succeed() throws Exception {
        User user = login();
        List<FavoriteProduct> favoriteProducts = createFavoriteProducts(user);
        ResultActions result = getFromKinvey_succeed(favoriteProducts, favoriteProductsPath);

        validateFavoriteProductsResponse(favoriteProducts, result);

        validateFavoriteProducts(favoriteProducts, usSpace.readMultiple(new FavoriteProduct()));
        waitForEmptyReplicationBacklog();
        validateFavoriteProducts(favoriteProducts, chinaSpace.readMultiple(new FavoriteProduct()));
    }

    @Test
    public void getFavoriteFAQsFromKinvey_succeed() throws Exception {
        User user = login();
        FavoriteFAQ favoriteFAQ = createFavoriteFaq(user);
        ResultActions result = getFromKinvey_succeed(Collections.singletonList(favoriteFAQ), favoriteFAQsPath);

        validateFavoriteFAQResponse(favoriteFAQ, result);

        FavoriteFAQ template = new FavoriteFAQ();
        template.setId(favoriteFAQ.getId());
        validateFavoriteFAQ(favoriteFAQ, usSpace.read(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteFAQ(favoriteFAQ, chinaSpace.read(template));
    }

    @Test
    public void getFavoriteDocumentsFromKinvey_succeed() throws Exception {
        User user = login();
        FavoriteDocument favoriteDocument = createFavoriteDocument(user);
        ResultActions result = getFromKinvey_succeed(Collections.singletonList(favoriteDocument), favoriteDocumentsPath);

        validateFavoriteDocumentResponse(favoriteDocument, result);

        FavoriteDocument template = new FavoriteDocument();
        template.setId(favoriteDocument.getId());
        validateFavoriteDocument(favoriteDocument, usSpace.read(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteDocument(favoriteDocument, chinaSpace.read(template));
    }

    private ResultActions getFromKinvey_succeed(List<? extends PrivateData> data, String urlPath) throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(data.stream().map(PrivateData :: toJsonProperties).toArray());
        mockKinvey200Response(jsonResponse, kinveyHost + urlPath, HttpMethod.GET);

        ResultActions result = mockMvc.perform(get(urlPath).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Kinvey " + KINVEY_AUTHTOKEN)));

        return result;
    }

    @Test
    public void createFavoriteRange() throws Exception {
        User user = login();

        FavoriteRange favoriteRange = createFavoriteRange(user, 1);

        ResultActions result = createPrivateData(favoriteRange, favoriteRangesPath);
        validateFavoriteRangeResponse(favoriteRange, result);

        FavoriteRange template = new FavoriteRange();
        template.setId(favoriteRange.getId());
        validateFavoriteRange(favoriteRange, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteRange(favoriteRange, chinaSpace.readIfExists(template));
    }

    @Test
    public void createFavoriteDocument() throws Exception {
        User user = login();

        FavoriteDocument favoriteDocument = createFavoriteDocument(user);

        ResultActions result = createPrivateData(favoriteDocument, favoriteDocumentsPath);
        validateFavoriteDocumentResponse(favoriteDocument, result);

        FavoriteDocument template = new FavoriteDocument();
        template.setId(favoriteDocument.getId());
        validateFavoriteDocument(favoriteDocument, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteDocument(favoriteDocument, chinaSpace.readIfExists(template));
    }

    @Test
    public void createFavoriteProduct() throws Exception {
        User user = login();

        FavoriteProduct favoriteProduct = createFavoriteProduct(user, 1);

        ResultActions result = createPrivateData(favoriteProduct, favoriteProductsPath);
        validateFavoriteProductResponse(favoriteProduct, result);

        FavoriteProduct template = new FavoriteProduct();
        template.setId(favoriteProduct.getId());
        validateFavoriteProduct(favoriteProduct, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteProduct(favoriteProduct, chinaSpace.readIfExists(template));
    }

    @Test
    public void createFavoriteFAQ() throws Exception {
        User user = login();

        FavoriteFAQ favoriteFAQ = createFavoriteFaq(user);

        ResultActions result = createPrivateData(favoriteFAQ, favoriteFAQsPath);
        validateFavoriteFAQResponse(favoriteFAQ, result);

        FavoriteFAQ template = new FavoriteFAQ();
        template.setId(favoriteFAQ.getId());
        validateFavoriteFAQ(favoriteFAQ, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteFAQ(favoriteFAQ, chinaSpace.readIfExists(template));
    }

    private ResultActions createPrivateData(PrivateData data, String urlPath) throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(data.toJsonProperties());
        mockKinvey200Response(jsonResponse, kinveyHost + urlPath, HttpMethod.POST);

        String id = data.getId();
        data.setId(null);

        String body = objectMapper.writeValueAsString(data.toJsonProperties());
        data.setId(id);

        ResultActions result = mockMvc.perform(post(urlPath).content(body).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Kinvey " + KINVEY_AUTHTOKEN)));

        return result;
    }

    @Test
    public void updateFavoriteRange() throws Exception {
        User user = login();

        FavoriteRange favoriteRange = createFavoriteRange(user, 1);

        ResultActions result = updatePrivateData(favoriteRange, favoriteRangesPath);
        validateFavoriteRangeResponse(favoriteRange, result);

        FavoriteRange template = new FavoriteRange();
        template.setId(favoriteRange.getId());
        validateFavoriteRange(favoriteRange, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteRange(favoriteRange, chinaSpace.readIfExists(template));
    }

    @Test
    public void updateFavoriteDocument() throws Exception {
        User user = login();

        FavoriteDocument favoriteDocument = createFavoriteDocument(user);

        ResultActions result = updatePrivateData(favoriteDocument, favoriteDocumentsPath);
        validateFavoriteDocumentResponse(favoriteDocument, result);

        FavoriteDocument template = new FavoriteDocument();
        template.setId(favoriteDocument.getId());
        validateFavoriteDocument(favoriteDocument, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteDocument(favoriteDocument, chinaSpace.readIfExists(template));
    }

    @Test
    public void updateFavoriteFAQ() throws Exception {
        User user = login();

        FavoriteFAQ favoriteFAQ = createFavoriteFaq(user);

        ResultActions result = updatePrivateData(favoriteFAQ, favoriteFAQsPath);
        validateFavoriteFAQResponse(favoriteFAQ, result);

        FavoriteFAQ template = new FavoriteFAQ();
        template.setId(favoriteFAQ.getId());
        validateFavoriteFAQ(favoriteFAQ, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteFAQ(favoriteFAQ, chinaSpace.readIfExists(template));
    }

    @Test
    public void updateFavoriteProduct() throws Exception {
        User user = login();

        FavoriteProduct favoriteProduct = createFavoriteProduct(user, 1);

        ResultActions result = updatePrivateData(favoriteProduct, favoriteProductsPath);
        validateFavoriteProductResponse(favoriteProduct, result);

        FavoriteProduct template = new FavoriteProduct();
        template.setId(favoriteProduct.getId());
        validateFavoriteProduct(favoriteProduct, usSpace.readIfExists(template));
        waitForEmptyReplicationBacklog();
        validateFavoriteProduct(favoriteProduct, chinaSpace.readIfExists(template));
    }

    private ResultActions updatePrivateData(PrivateData data, String urlPath) throws Exception {
        usSpace.write(data);
        waitForEmptyReplicationBacklog();

        data.setCountry("New country");
        String jsonResponse = objectMapper.writeValueAsString(data.toJsonProperties());
        String id = data.getId();
        String url = urlPath + "/" + id;
        mockKinvey200Response(jsonResponse, kinveyHost + url, HttpMethod.PUT);

        ResultActions result = mockMvc.perform(put(url).content(jsonResponse).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Kinvey " + KINVEY_AUTHTOKEN)));

        return result;
    }

    @Test
    public void deleteFavoriteRange() throws Exception {
        User user = login();

        FavoriteRange favoriteRange = createFavoriteRange(user, 1);
        deleteFavoriteData(favoriteRange, FavoriteRange.TYPE, favoriteRangesPath);
    }

    @Test
    public void deleteFavoriteDocument() throws Exception {
        User user = login();

        FavoriteDocument favoriteDocument = createFavoriteDocument(user);
        deleteFavoriteData(favoriteDocument, FavoriteDocument.TYPE, favoriteDocumentsPath);
    }

    @Test
    public void deleteFavoriteProduct() throws Exception {
        User user = login();

        FavoriteProduct favoriteProduct = createFavoriteProduct(user, 1);
        deleteFavoriteData(favoriteProduct, FavoriteProduct.TYPE, favoriteProductsPath);
    }

    @Test
    public void deleteFavoriteFAQ() throws Exception {
        User user = login();

        FavoriteFAQ favoriteFAQ = createFavoriteFaq(user);
        deleteFavoriteData(favoriteFAQ, FavoriteFAQ.TYPE, favoriteFAQsPath);
    }

    private void deleteFavoriteData(PrivateData data, String type, String urlPath) throws Exception {
        usSpace.write(data);
        waitForEmptyReplicationBacklog();

        String id = data.getId();
        String url = urlPath + "/" + id;
        mockKinvey204Response(kinveyHost + url, HttpMethod.DELETE);

        mockMvc.perform(delete(url).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Kinvey " + KINVEY_AUTHTOKEN))).andExpect(status().isNoContent());
        
        IdQuery<? extends PrivateData> query = new IdQuery<PrivateData>(type, id);
        assertNull(usSpace.readIfExists(query));
        waitForEmptyReplicationBacklog();
        assertNull(chinaSpace.readIfExists(query));
    }

    private void validateFavoriteRangesResponse(List<FavoriteRange> favoriteRanges, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
        .andExpect(jsonPath("$[*]._id").value(containsInAnyOrder(favoriteRanges.stream().map(FavoriteRange::getId).toArray())))
        .andExpect(jsonPath("$[*].name").value(containsInAnyOrder(favoriteRanges.stream().map(fr -> fr.getProperty("name")).toArray())))
        .andExpect(jsonPath("$[*].country").value(containsInAnyOrder(favoriteRanges.stream().map(FavoriteRange::getCountry).toArray())))
        .andExpect(jsonPath("$[*]._acl.creator").value(containsInAnyOrder(favoriteRanges.stream().map(fr -> fr.getCreator()).toArray())))
        .andExpect(jsonPath("$[*].rangeId").value(containsInAnyOrder(favoriteRanges.stream().map(fr -> fr.getProperty("rangeId")).toArray())))
        .andExpect(jsonPath("$[*].description").value(containsInAnyOrder(favoriteRanges.stream().map(fr -> fr.getProperty("description")).toArray())))
        .andExpect(jsonPath("$[*].pictureId").value(containsInAnyOrder(favoriteRanges.stream().map(fr -> fr.getProperty("pictureId")).toArray())))
        .andExpect(jsonPath("$[*].creator").doesNotExist())
        .andExpect(jsonPath("$[*].replicable").doesNotExist());
    }

    private void validateFavoriteRangeResponse(FavoriteRange favoriteRange, ResultActions result) throws Exception {
        validatePrivateData(favoriteRange, result);

        result.andExpect(jsonPath("$.name").value(favoriteRange.<String>getProperty("name")))
        .andExpect(jsonPath("$.rangeId").value(favoriteRange.<Integer>getProperty("rangeId")))
        .andExpect(jsonPath("$.description").value(favoriteRange.<String>getProperty("description")))
        .andExpect(jsonPath("$.pictureId").value(favoriteRange.<String>getProperty("pictureId")));
    }

    private void validatePrivateData(PrivateData privateData, ResultActions result)  throws Exception {
        result.andExpect(status().isOk())
        .andExpect(jsonPath("$._id").value(privateData.getId()))
        .andExpect(jsonPath("$.country").value(privateData.getCountry()))
        .andExpect(jsonPath("$._acl.creator").value(privateData.getCreator()))
        .andExpect(jsonPath("$.replicable").doesNotExist());
    }

    private void validatePrivateDataAsArray(PrivateData privateData, ResultActions result)  throws Exception {
        result.andExpect(status().isOk())
        .andExpect(jsonPath("$[*]._id").value(privateData.getId()))
        .andExpect(jsonPath("$[*].country").value(privateData.getCountry()))
        .andExpect(jsonPath("$[*]._acl.creator").value(privateData.getCreator()))
        .andExpect(jsonPath("$[*].creator").doesNotExist())
        .andExpect(jsonPath("$[*].replicable").doesNotExist());
    }

    private void validateFavoriteDocumentResponse(FavoriteDocument favoriteDocument, ResultActions result) throws Exception {
        validatePrivateDataAsArray(favoriteDocument, result);
        result.andExpect(jsonPath("$[*].author").value(favoriteDocument.<String>getProperty("author")))
        .andExpect(jsonPath("$[*].attributes[0].name").value(favoriteDocument.<Map<String, Object>[]>getProperty("attributes")[0].get("name")))
        .andExpect(jsonPath("$[*].attributes[0].value").value(favoriteDocument.<Map<String, Object>[]>getProperty("attributes")[0].get("value")))
        .andExpect(jsonPath("$[*].audience.id").value(favoriteDocument.<Map<String, Object>>getProperty("audience").get("id")))
        .andExpect(jsonPath("$[*].audience.translation").value(favoriteDocument.<Map<String, Object>>getProperty("audience").get("translation")))
        .andExpect(jsonPath("$[*].creationDate").value(favoriteDocument.<String>getProperty("creationDate")))
        .andExpect(jsonPath("$[*].desc").value(favoriteDocument.<String>getProperty("desc")))
        .andExpect(jsonPath("$[*].description").value(favoriteDocument.<String>getProperty("description")))
        .andExpect(jsonPath("$[*].docId").value(favoriteDocument.<Integer>getProperty("docId")))
        .andExpect(jsonPath("$[*].docLastModificationDate").value(favoriteDocument.<String>getProperty("docLastModificationDate")))
        .andExpect(jsonPath("$[*].docOwner").value(favoriteDocument.<String>getProperty("docOwner")))
        .andExpect(jsonPath("$[*].docReference").value(favoriteDocument.<String>getProperty("docReference")))
        .andExpect(jsonPath("$[*].docTitle").value(favoriteDocument.<String>getProperty("docTitle")))
        .andExpect(jsonPath("$[*].documentDate").value(favoriteDocument.<String>getProperty("documentDate")))
        .andExpect(jsonPath("$[*].documentType.englishLabel").value(favoriteDocument.<Map<String, Object>>getProperty("documentType").get("englishLabel")))
        .andExpect(jsonPath("$[*].documentType.id").value(favoriteDocument.<Map<String, Object>>getProperty("documentType").get("id")))
        .andExpect(jsonPath("$[*].documentType.name").value(favoriteDocument.<Map<String, Object>>getProperty("documentType").get("name")))
        .andExpect(jsonPath("$[*].documentType.translation").value(favoriteDocument.<Map<String, Object>>getProperty("documentType").get("translation")))
        .andExpect(jsonPath("$[*].files[0].extension").value(favoriteDocument.<Map<String, Object>[]>getProperty("files")[0].get("extension")))
        .andExpect(jsonPath("$[*].files[0].filename").value(favoriteDocument.<Map<String, Object>[]>getProperty("files")[0].get("filename")))
        .andExpect(jsonPath("$[*].files[0].id").value(favoriteDocument.<Map<String, Object>[]>getProperty("files")[0].get("id")))
        .andExpect(jsonPath("$[*].files[0].size").value(favoriteDocument.<Map<String, Object>[]>getProperty("files")[0].get("size")))
        .andExpect(jsonPath("$[*].flipFlopGenerated").value(favoriteDocument.<Boolean>getProperty("flipFlopGenerated")))
        .andExpect(jsonPath("$[*].isFavorite").value(favoriteDocument.<Boolean>getProperty("isFavorite")))
        .andExpect(jsonPath("$[*].keywords").value(favoriteDocument.<String>getProperty("keywords")))
        .andExpect(jsonPath("$[*].lastModificationDate").value(favoriteDocument.<String>getProperty("lastModificationDate")))
        .andExpect(jsonPath("$[*].locales[0].isoCountry").value(favoriteDocument.<Map<String, Object>[]>getProperty("locales")[0].get("isoCountry")))
        .andExpect(jsonPath("$[*].locales[0].isoLanguage").value(favoriteDocument.<Map<String, Object>[]>getProperty("locales")[0].get("isoLanguage")))
        .andExpect(jsonPath("$[*].numberOfPage").value(favoriteDocument.<Integer>getProperty("numberOfPage")))
        .andExpect(jsonPath("$[*].partNumber").value(favoriteDocument.<Integer>getProperty("partNumber")))
        .andExpect(jsonPath("$[*].publicationDate").value(favoriteDocument.<String>getProperty("publicationDate")))
        .andExpect(jsonPath("$[*].reference").value(favoriteDocument.<String>getProperty("reference")))
        .andExpect(jsonPath("$[*].revision").value(favoriteDocument.<String>getProperty("revision")))
        .andExpect(jsonPath("$[*].title").value(favoriteDocument.<String>getProperty("title")))
        .andExpect(jsonPath("$[*].version").value(favoriteDocument.<String>getProperty("version")));
    }

    private void validateFavoriteFAQResponse(FavoriteFAQ favoriteFAQ, ResultActions result) throws Exception {
        validatePrivateDataAsArray(favoriteFAQ, result);
        result.andExpect(jsonPath("$[*].countryId").value(favoriteFAQ.<String>getProperty("countryId")))
        .andExpect(jsonPath("$[*].excerpt").value(favoriteFAQ.<String>getProperty("excerpt")))
        .andExpect(jsonPath("$[*].faqCategoryList[0].numberOfContents").value(favoriteFAQ.<Map<String, Object>[]>getProperty("faqCategoryList")[0].get("numberOfContents")))
        .andExpect(jsonPath("$[*].faqCategoryList[0].referenceKey").value(favoriteFAQ.<Map<String, Object>[]>getProperty("faqCategoryList")[0].get("referenceKey")))
        .andExpect(jsonPath("$[*].faqCategoryList[0].title").value(favoriteFAQ.<Map<String, Object>[]>getProperty("faqCategoryList")[0].get("title")))
        .andExpect(jsonPath("$[*].faqId").value(favoriteFAQ.<Integer>getProperty("faqId")))
        .andExpect(jsonPath("$[*].order").value(favoriteFAQ.<Integer>getProperty("order")));
    }

    private void validateFavoriteProductResponse(FavoriteProduct favoriteProduct, ResultActions result) throws Exception {
        validatePrivateData(favoriteProduct, result);
        result.andExpect(jsonPath("$.commercialReference").value(favoriteProduct.<String>getProperty("commercialReference")));
    }

    private void validateFavoriteProductsResponse(List<FavoriteProduct> favoriteProducts, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
        .andExpect(jsonPath("$[*]._id").value(containsInAnyOrder(favoriteProducts.stream().map(FavoriteProduct::getId).toArray())))
        .andExpect(jsonPath("$[*].country").value(containsInAnyOrder(favoriteProducts.stream().map(FavoriteProduct::getCountry).toArray())))
        .andExpect(jsonPath("$[*]._acl.creator").value(containsInAnyOrder(favoriteProducts.stream().map(fr -> fr.getCreator()).toArray())))
        .andExpect(jsonPath("$[*].commercialReference").value(containsInAnyOrder(favoriteProducts.stream().map(fr -> fr.getProperty("commercialReference")).toArray())))
        .andExpect(jsonPath("$[*].creator").doesNotExist())
        .andExpect(jsonPath("$[*].replicable").doesNotExist());
    }

    private void validateFavoriteRanges(List<FavoriteRange> expected, FavoriteRange[] actual) {
        sortPrivateData(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            validateFavoriteRange(expected.get(i), actual[i]);
        }
    }

    private void validateFavoriteProducts(List<FavoriteProduct> expected, FavoriteProduct[] actual) {
        sortPrivateData(expected, actual);
        for (int i = 0; i < expected.size(); i++) {
            validateFavoriteProduct(expected.get(i), actual[i]);
        }
    }

    private void validateFavoriteRange(FavoriteRange expected, FavoriteRange actual) {
        validatePrivateData(expected, actual);
        assertEquals(expected.<String>getProperty("name"), actual.<String>getProperty("name"));
        assertEquals(expected.<String>getProperty("pictureId"), actual.<String>getProperty("pictureId"));
        assertEquals(expected.<Integer>getProperty("rangeId"), actual.<Integer>getProperty("rangeId"));
        assertEquals(expected.<String>getProperty("description"), actual.<String>getProperty("description"));
    }

    private void validateFavoriteProduct(FavoriteProduct expected, FavoriteProduct actual) {
        validatePrivateData(expected, actual);
        assertEquals(expected.<String>getProperty("commercialReference"), actual.<String>getProperty("commercialReference"));
    }

    private void validateFavoriteFAQ(FavoriteFAQ expected, FavoriteFAQ actual) {
        validatePrivateData(expected, actual);
        assertEquals(expected.<String>getProperty("countryId"), actual.<String>getProperty("countryId"));
        assertEquals(expected.<String>getProperty("excerpt"), actual.<String>getProperty("excerpt"));
        assertEquals(expected.<Integer>getProperty("faqId"), actual.<Integer>getProperty("faqId"));
        assertEquals(expected.<String>getProperty("order"), actual.<String>getProperty("order"));
        assertEquals(expected.<String>getProperty("title"), actual.<String>getProperty("title"));
        List<Map<String, Object>> actualFaqCategories = actual.getProperty("faqCategoryList");
        assertEquals(1, actualFaqCategories.size());
        Map<String, Object> expectedCategory = expected.<Map<String, Object>[]>getProperty("faqCategoryList")[0];
        Map<String, Object> actualCategory = actualFaqCategories.get(0);
        assertEquals(expectedCategory.get("numberOfContents"), actualCategory.get("numberOfContents"));
        assertEquals(expectedCategory.get("referenceKey"), actualCategory.get("referenceKey"));
        assertEquals(expectedCategory.get("title"), actualCategory.get("title"));
    }

    private void validatePrivateData(PrivateData expected, PrivateData actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCountry(), actual.getCountry());
        assertEquals(expected.getCreator(), actual.getCreator());
        assertEquals(expected.isReplicable(), actual.isReplicable());
    }

    private void validateFavoriteDocument(FavoriteDocument expected, FavoriteDocument actual) {
        validatePrivateData(expected, actual);
        List<Map<String, Object>> actualAttributes = actual.getProperty("attributes");
        assertEquals(1, actualAttributes.size());
        Map<String, Object> expectedAttr = expected.<Map<String, Object>[]>getProperty("attributes")[0];
        Map<String, Object> actualAttr = actualAttributes.get(0);
        assertEquals(expectedAttr.get("name"), actualAttr.get("name"));
        assertEquals(expectedAttr.get("value"), actualAttr.get("value"));
        assertEquals(expected.<String>getProperty("author"), actual.<String>getProperty("author"));
        Map<String, Object> expectedAudience = expected.getProperty("audience");
        Map<String, Object> actualAudience = actual.getProperty("audience");
        assertEquals(expectedAudience.get("id"), actualAudience.get("id"));
        assertEquals(expectedAudience.get("translation"), actualAudience.get("translation"));
        assertEquals(expected.<String>getProperty("creationDate"), actual.<String>getProperty("creationDate"));
        assertEquals(expected.<String>getProperty("desc"), actual.<String>getProperty("desc"));
        assertEquals(expected.<String>getProperty("description"), actual.<String>getProperty("description"));
        assertEquals(expected.<String>getProperty("docId"), actual.<String>getProperty("docId"));
        assertEquals(expected.<String>getProperty("docLastModificationDate"), actual.<String>getProperty("docLastModificationDate"));
        assertEquals(expected.<String>getProperty("docOwner"), actual.<String>getProperty("docOwner"));
        assertEquals(expected.<String>getProperty("docReference"), actual.<String>getProperty("docReference"));
        assertEquals(expected.<String>getProperty("docTitle"), actual.<String>getProperty("docTitle"));
        assertEquals(expected.<String>getProperty("documentDate"), actual.<String>getProperty("documentDate"));
        Map<String, Object> expectedDocType = expected.getProperty("documentType");
        Map<String, Object> actualDocType = actual.getProperty("documentType");
        assertEquals(expectedDocType.get("englishLabel"), actualDocType.get("englishLabel"));
        assertEquals(expectedDocType.get("id"), actualDocType.get("id"));
        assertEquals(expectedDocType.get("name"), actualDocType.get("name"));
        assertEquals(expectedDocType.get("translation"), actualDocType.get("translation"));
        List<Map<String, Object>> actualFiles = actual.getProperty("files");
        assertEquals(1, actualFiles.size());
        Map<String, Object> expectedFile = expected.<Map<String, Object>[]>getProperty("files")[0];
        Map<String, Object> actualFile = actualFiles.get(0);
        assertEquals(expectedFile.get("extension"), actualFile.get("extension"));
        assertEquals(expectedFile.get("filename"), actualFile.get("filename"));
        assertEquals(expectedFile.get("id"), actualFile.get("id"));
        assertEquals(expectedFile.get("size"), actualFile.get("size"));
        assertEquals(expected.<Boolean>getProperty("flipFlopGenerated"), actual.<Boolean>getProperty("flipFlopGenerated"));
        assertEquals(expected.<Boolean>getProperty("isFavorite"), actual.<Boolean>getProperty("isFavorite"));
        assertEquals(expected.<String>getProperty("keywords"), actual.<String>getProperty("keywords"));
        assertEquals(expected.<String>getProperty("lastModificationDate"), actual.<String>getProperty("lastModificationDate"));
        List<Map<String, Object>> actualLocales = actual.getProperty("locales");
        assertEquals(1, actualLocales.size());
        Map<String, Object> expectedLocale = expected.<Map<String, Object>[]>getProperty("locales")[0];
        Map<String, Object> actualLocale = actualLocales.get(0);
        assertEquals(expectedLocale.get("isoCountry"), actualLocale.get("isoCountry"));
        assertEquals(expectedLocale.get("isoLanguage"), actualLocale.get("isoLanguage"));
        assertEquals(expected.<Integer>getProperty("numberOfPage"), actual.<Integer>getProperty("numberOfPage"));
        assertEquals(expected.<String>getProperty("partNumber"), actual.<String>getProperty("partNumber"));
        assertEquals(expected.<String>getProperty("publicationDate"), actual.<String>getProperty("publicationDate"));
        assertEquals(expected.<String>getProperty("reference"), actual.<String>getProperty("reference"));
        assertEquals(expected.<String>getProperty("revision"), actual.<String>getProperty("revision"));
        assertEquals(expected.<String>getProperty("title"), actual.<String>getProperty("title"));
        assertEquals(expected.<String>getProperty("version"), actual.<String>getProperty("version"));
    }

    private void sortPrivateData(List<? extends PrivateData> expected, PrivateData[] actual) {
        assertNotNull(actual);
        Comparator<PrivateData> comparator = (c1, c2) -> c1.getId().compareTo(c2.getId());
        Collections.sort(expected, comparator);
        Arrays.sort(actual, comparator);
        assertEquals(expected.size(), actual.length);
    }

    private FavoriteDocument createFavoriteDocument(User user) {
        Map<String, Object> properties = new HashMap<>();
        Map<String, Object> documentAttr = new HashMap<>();
        documentAttr.put("name", "doc1");
        documentAttr.put("value", "doc1values");
        properties.put("attributes", new Map[]{documentAttr});
        properties.put("description", "Foo1");
        Map<String, Object> audience = new HashMap<>();
        audience.put("id", 11111);
        audience.put("translation", "Test translation1");
        properties.put("audience", audience);
        properties.put("author", "Test author1");
        properties.put("creationDate", "CreationDate1");
        properties.put("desc", "desc1");
        properties.put("docId", 222222);
        properties.put("docLastModificationDate", ZonedDateTime.now().toString());
        properties.put("docOwner", "doc owner1");
        properties.put("docReference", "doc reference1");
        properties.put("docTitle", "doc title1");
        properties.put("documentDate", ZonedDateTime.now().toString());
        Map<String, Object> documentType = new HashMap<>();
        documentType.put("englishLabel", "English label 1");
        documentType.put("id", 1111);
        documentType.put("name", "Name1");
        documentType.put("translation", "Translation1");
        properties.put("documentType", documentType);
        Map<String, Object> file = new HashMap<>();
        file.put("extension", ".foo");
        file.put("filename", "Test filename1");
        file.put("size", 123);
        file.put("id", 44444);
        properties.put("files", new Map[] {file});
        properties.put("flipFlopGenerated", false);
        properties.put("isFavorite", true);
        properties.put("keywords", "Keywords1");
        properties.put("lastModificationDate", ZonedDateTime.now().toString());
        Map<String, Object> locale = new HashMap<>();
        locale.put("isoCountry", "isoCountry1");
        locale.put("isoLanguage", "isoLanguage1");
        properties.put("locales", new Map[] {locale});
        properties.put("numberOfPage", 156);
        properties.put("partNumber", "ssssss");
        properties.put("publicationDate", "publicationDate1");
        properties.put("reference", "reference1");
        properties.put("revision", "Revision1");
        properties.put("title", "Title1");
        properties.put("version", "Version1");

        FavoriteDocument fd1 = new FavoriteDocument(properties);
        fd1.setId("111111111");
        fd1.setCountry("USA");
        fd1.setAcl(Collections.singletonMap("creator", user.getId()));
        fd1.setReplicable(true);
        return fd1;
    }

    private FavoriteFAQ createFavoriteFaq(User user) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("countryId", "USA");
        properties.put("excerpt", "excerpt1");
        
        Map<String, Object> faqCategory = new HashMap<>();
        faqCategory.put("numberOfContents", 23);
        faqCategory.put("referenceKey", "RefKey1");
        faqCategory.put("title", "Title1");
        properties.put("faqCategoryList", new Map[] {faqCategory});
        properties.put("faqId", 13245665);
        properties.put("order", 2);

        FavoriteFAQ faq1 = new FavoriteFAQ(properties);
        faq1.setId("111111111");
        faq1.setCountry("USA");
        faq1.setAcl(Collections.singletonMap("creator", user.getId()));
        faq1.setReplicable(true);
        return faq1;
    }

    private FavoriteProduct createFavoriteProduct(User user, int id) {
        FavoriteProduct fp1 = new FavoriteProduct(Collections.singletonMap("commercialReference", "commercialReference" + id));
        fp1.setId(String.valueOf(id));
        fp1.setCountry("USA");
        fp1.setAcl(Collections.singletonMap("creator", user.getId()));
        fp1.setReplicable(true);
        return fp1;
    }

    private List<FavoriteProduct> createFavoriteProducts(User user) {
        return Arrays.asList(createFavoriteProduct(user, 1), createFavoriteProduct(user, 2), createFavoriteProduct(user, 3));
    }
}
