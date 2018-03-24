package com.gigaspaces.fdal.controller;

import com.gigaspaces.document.DocumentProperties;

import com.gigaspaces.metadata.index.SpaceIndexType;
import com.gigaspaces.fdal.model.document.Country;
import com.gigaspaces.fdal.model.document.User;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.fdal.model.document.FavoriteDocument;
import com.gigaspaces.fdal.model.document.FavoriteFAQ;
import com.gigaspaces.fdal.model.document.FavoriteProduct;
import com.gigaspaces.fdal.model.document.PrivateData;
import org.openspaces.core.GigaSpaceTypeManager;
import com.gigaspaces.fdal.model.document.FavoriteRange;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j_spaces.core.admin.StatisticsAdmin;
import com.gigaspaces.fdal.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.net.URI;
import java.util.*;
import java.util.concurrent.Callable;

import javax.annotation.Resource;

import static org.junit.Assert.assertEquals;

/**
 * @author Svitlana_Pogrebnas
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class AbstractControllerTest {

    @Autowired
    protected WebApplicationContext wac;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected MockRestServiceServer mockRestServiceServer; // per partition

    protected static final String kinveyHost = "https://test.kinvey.com";
    protected static final String appKey = "kid_Zk-fAAEhwg";

    @Resource
    protected GigaSpace chinaSpace;

    @Resource
    protected GigaSpace usSpace;

    @Resource
    protected TestCluster usSpaceCluster;

    @Autowired
    private RestTemplate restTemplate;

    protected static final String KINVEY_AUTHTOKEN = "a2lkX1prLWZBQUVod2c6Zjk1M2ZjYTZiYWFmNGJkMWI0MTExZDNhOTA1ZmIyMzg=";

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        mockRestServiceServer = MockRestServiceServer.createServer(restTemplate);

        registerTypes(chinaSpace);
        registerTypes(usSpace);

        chinaSpace.clear(null);
        usSpace.clear(null);
    }

    private void registerTypes(GigaSpace gigaSpace) {
        SpaceTypeDescriptor userType = new SpaceTypeDescriptorBuilder("User")
            .idProperty("_id")
            .routingProperty("_id")
            .addPathIndex("_kmd.authtoken", SpaceIndexType.BASIC)
            .documentWrapperClass(User.class)
            .create();

        SpaceTypeDescriptor countryType = new SpaceTypeDescriptorBuilder("Country")
            .idProperty("_id")
            .routingProperty("_id")
            .documentWrapperClass(Country.class)
            .create();

        SpaceTypeDescriptor privateDataType = new SpaceTypeDescriptorBuilder("PrivateData")
            .documentWrapperClass(PrivateData.class)
            .create();
            
        SpaceTypeDescriptor favoriteRangeType = new SpaceTypeDescriptorBuilder("FavoriteRange", privateDataType)
            .idProperty("_id")
            .routingProperty("creator")
            .documentWrapperClass(FavoriteRange.class)
            .create();

        SpaceTypeDescriptor favoriteDocumentType = new SpaceTypeDescriptorBuilder("FavoriteDocument", privateDataType)
            .idProperty("_id")
            .routingProperty("creator")
            .documentWrapperClass(FavoriteDocument.class)
            .create();

        SpaceTypeDescriptor favoriteProductType = new SpaceTypeDescriptorBuilder("FavoriteProduct", privateDataType)
            .idProperty("_id")
            .routingProperty("creator")
            .documentWrapperClass(FavoriteProduct.class)
            .create();

        SpaceTypeDescriptor favoriteFaqType = new SpaceTypeDescriptorBuilder("FavoriteFAQ", privateDataType)
            .idProperty("_id")
            .routingProperty("creator")
            .documentWrapperClass(FavoriteFAQ.class)
            .create();

        GigaSpaceTypeManager typeManager = gigaSpace.getTypeManager();
        typeManager.registerTypeDescriptor(userType);
        typeManager.registerTypeDescriptor(countryType);
        typeManager.registerTypeDescriptor(favoriteRangeType);
        typeManager.registerTypeDescriptor(favoriteDocumentType);
        typeManager.registerTypeDescriptor(favoriteProductType);
        typeManager.registerTypeDescriptor(favoriteFaqType);
    }

    protected void mockKinvey401Response(String uri, HttpMethod method) throws Exception {
        mockKinveyResponse(uri, method, HttpStatus.UNAUTHORIZED);
    }
    protected void mockKinvey204Response(String uri, HttpMethod method) throws Exception {
        mockKinveyResponse(uri, method, HttpStatus.NO_CONTENT);
    }
    private void mockKinveyResponse(String uri, HttpMethod method, HttpStatus httpStatus) throws Exception {
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(new URI(uri)))
        .andExpect(MockRestRequestMatchers.method(method))
        .andRespond(MockRestResponseCreators.withStatus(httpStatus));
    }
    protected void mockKinvey200Response(String jsonResponse, String uri, HttpMethod method) throws Exception {
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo(uri))
        .andExpect(MockRestRequestMatchers.method(method))
        .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(jsonResponse));
    }
    protected HttpHeaders createHeaders(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, authorization);
        headers.set("X-Kinvey-API-Version", "3");
        return headers;
    }

    protected User login() {
        User user = createUser();
        usSpace.write(user);
        return user;
    }

    protected User createUser() {
        User expectedUser = new User();
        expectedUser.setId("111111111");
        DocumentProperties kmd = new DocumentProperties();
        kmd.setProperty("authtoken", KINVEY_AUTHTOKEN);
        kmd.setProperty("ect", "Ect date");
        kmd.setProperty("lmt", "Lmt date");
        expectedUser.setKmd(kmd);
        expectedUser.setCountry("USA");
        expectedUser.setLoggedInWith(OperationalCountry.USA);
        expectedUser.setProfile("EMPLOYEE");
        expectedUser.setUsername("User111111111");
        return expectedUser;
    }

    protected List<Country> createCountries() {
        Country usa = new Country();
        usa.setId("1111111");
        usa.setCode("USA");
        usa.setFaqLocaleCode("USA");
        usa.setLanguage("English");
        usa.setLanguageAndroid("English");
        usa.setLanguageIOS("English");
        usa.setName("USA");

        Country china = new Country();
        china.setId("2222222");
        china.setCode("China");
        china.setFaqLocaleCode("Chinese");
        china.setLanguage("Chinese");
        china.setLanguageAndroid("Chinese");
        china.setLanguageIOS("Chinese");
        china.setName("China");
        return Arrays.asList(usa, china);
    }

    protected List<FavoriteRange> createFavoriteRanges(User user) {
        return Arrays.asList(createFavoriteRange(user, 1), createFavoriteRange(user, 2));
    }

    protected FavoriteRange createFavoriteRange(User user, int id) {
        Map<String, Object> properties = new DocumentProperties();
        properties.put("name", "FR" + id);
        properties.put("pictureId", "picture" + id);
        properties.put("rangeId", id);
        properties.put("description", "Foo" + id);
        
        FavoriteRange fr1 = new FavoriteRange(properties);
        fr1.setId(String.valueOf(id));
        fr1.setCountry(user.getCountry());
        fr1.setAcl(Collections.singletonMap("creator", user.getId()));
        fr1.setReplicable(false);
        return fr1;
    }

    protected void waitForEmptyReplicationBacklog() {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Assert.assertTrue("replication backlog is not 0", repeat(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                long l = ((StatisticsAdmin) usSpace.getSpace().getAdmin()).getHolder().getReplicationStatistics().getOutgoingReplication().getRedoLogSize();
                assertEquals("Backlog is not empty", 0, l);
                return null;
            }
        }, 10 * 1000));
    }

    protected boolean repeat(Callable<Void> iRepetitiveRunnable, long repeateInterval) {
        return repeat(iRepetitiveRunnable, repeateInterval, 4);
    }

    protected boolean repeat(Callable<Void> task, long repeatInterval, int timesToRepeat) {
        int leftToRepeat = timesToRepeat;
        while (true) {
            try {
                task.call();
                break;
            } catch (Throwable e) {
                try {
                    Thread.sleep(repeatInterval);
                    leftToRepeat--;
                    if (leftToRepeat == 0)
                        break;
                } catch (InterruptedException e1) {
                    leftToRepeat--;
                    if (leftToRepeat == 0)
                        break;
                }
            }
        }
        return leftToRepeat > 0;
    }
}
