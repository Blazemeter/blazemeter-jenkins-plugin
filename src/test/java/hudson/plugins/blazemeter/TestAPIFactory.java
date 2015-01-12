package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.api.BlazemeterApiV2Impl;
import hudson.plugins.blazemeter.api.BlazemeterApiV3Impl;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Created by dzmitrykashlach on 12/01/15.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestAPIFactory {
    private BlazemeterApi blazemeterApi = null;
    private String userKey1 = "1234567890";
    private String userKey2 = "0987654321";

    @Test
    public void testDefaultVersion() {
        APIFactory apiFactory = APIFactory.getApiFactory();
        blazemeterApi = apiFactory.getAPI(userKey1);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);
    }

    @Test
    public void testV3() {
        APIFactory apiFactory = APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v3);
        blazemeterApi = apiFactory.getAPI(userKey1);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey1));
        blazemeterApi = apiFactory.getAPI(userKey2);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey2));
        blazemeterApi = apiFactory.getAPI(userKey1);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV3Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey1));

    }

    @Test
    public void testV2() {
        APIFactory apiFactory = APIFactory.getApiFactory();
        apiFactory.setVersion(APIFactory.ApiVersion.v2);
        blazemeterApi = apiFactory.getAPI(userKey1);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV2Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey1));
        blazemeterApi = apiFactory.getAPI(userKey2);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV2Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey2));
        blazemeterApi = apiFactory.getAPI(userKey1);
        Assert.assertTrue(blazemeterApi instanceof BlazemeterApiV2Impl);
        Assert.assertTrue(blazemeterApi.getApiKey().equals(userKey1));

    }
}
