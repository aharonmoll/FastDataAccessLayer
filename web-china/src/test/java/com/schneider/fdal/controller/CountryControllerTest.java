package com.gigaspaces.fdal.controller;

import com.gigaspaces.fdal.model.document.Country;

import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.fdal.model.KinveyAsyncGetRequest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
/**
 * @author Svitlana_Pogrebna
 *
 */
@ContextConfiguration("/test-context.xml")
public class CountryControllerTest extends AbstractControllerTest {

    private static final String countriesPath = "/appdata/" + appKey + "/countries";

    @Autowired
    private CountryController countryController;

    @Test
    public void getCountriesFromKinvey_succeed() throws Exception {
        login();
        getCountriesFromKinvey();
    }

    @Test
    public void getCountriesFromSpace_succeed() throws Exception {
        User user = login();

        List<Country> countries = getCountriesFromKinvey();

        HttpHeaders headers = createHeaders("Kinvey " + KINVEY_AUTHTOKEN);
        ResultActions result = mockMvc.perform(get(countriesPath).headers(headers));
        validateCountriesResponse(countries, result);

        KinveyAsyncGetRequest asyncRequest = usSpace.read(new KinveyAsyncGetRequest());
        assertNotNull(asyncRequest);
        assertEquals(countriesPath, asyncRequest.getUrl());
        assertEquals(Country.class, asyncRequest.getEntityType());
        assertEquals(headers, asyncRequest.getHeaders());
        assertEquals(Boolean.FALSE, asyncRequest.getSaveToSpace());
        assertEquals(user.getId(), asyncRequest.getUserId());
    }

    private List<Country> getCountriesFromKinvey() throws Exception {
        List<Country> countries = createCountries();

        String jsonResponse = objectMapper.writeValueAsString(countries.stream().map(Country::getProperties).toArray());
        mockKinvey200Response(jsonResponse, kinveyHost + countriesPath, HttpMethod.GET);

        ResultActions result = mockMvc.perform(get(countriesPath).accept(MediaType.APPLICATION_JSON).headers(createHeaders("Kinvey " + KINVEY_AUTHTOKEN)));
        validateCountriesResponse(countries, result);

        validateCountries(countries, usSpace.readMultiple(new Country()));
        waitForEmptyReplicationBacklog();
        validateCountries(countries, chinaSpace.readMultiple(new Country()));
        return countries;
    }

    private void validateCountriesResponse(List<Country> countries, ResultActions result) throws Exception {
        result.andExpect(status().isOk())
       .andExpect(jsonPath("$[*]._id").value(containsInAnyOrder(countries.stream().map(Country::getId).toArray())))
       .andExpect(jsonPath("$[*].name").value(containsInAnyOrder(countries.stream().map(Country::getName).toArray())))
       .andExpect(jsonPath("$[*].faqLocaleCode").value(containsInAnyOrder(countries.stream().map(Country::getFaqLocaleCode).toArray())))
       .andExpect(jsonPath("$[*].code").value(containsInAnyOrder(countries.stream().map(Country::getCode).toArray())))
       .andExpect(jsonPath("$[*].language").value(containsInAnyOrder(countries.stream().map(Country::getLanguage).toArray())))
       .andExpect(jsonPath("$[*].languageAndroid").value(containsInAnyOrder(countries.stream().map(Country::getLanguageAndroid).toArray())))
       .andExpect(jsonPath("$[*].languageIOS").value(containsInAnyOrder(countries.stream().map(Country::getLanguageIOS).toArray())));
    }

    private void validateCountries(List<Country> expected, Country[] actual) throws Exception {
        assertNotNull(actual);
        Comparator<Country> comparator = (c1, c2) -> c1.getId().compareTo(c2.getId());
        Collections.sort(expected, comparator);
        Arrays.sort(actual, comparator);
        assertEquals(expected.size(), actual.length);
        for(int i = 0; i < expected.size(); i++) {
            validateCountry(expected.get(i), actual[i]);
        }
    }

    private void validateCountry(Country expected, Country actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getCode(), actual.getCode());
        assertEquals(expected.getFaqLocaleCode(), actual.getFaqLocaleCode());
        assertEquals(expected.getLanguage(), actual.getLanguage());
        assertEquals(expected.getLanguageAndroid(), actual.getLanguageAndroid());
        assertEquals(expected.getLanguageIOS(), actual.getLanguageIOS());
    }
}
