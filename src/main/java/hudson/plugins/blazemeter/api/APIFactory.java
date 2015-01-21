package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;

public class APIFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static APIFactory apiFactory = null;

    private String blazeMeterUrl= Constants.DEFAULT_BLAZEMETER_URL;

    private APIFactory() {
    }

    public static APIFactory getApiFactory() {
        if (apiFactory == null) {
            apiFactory = new APIFactory();
        }
        return apiFactory;
    }

    /*TODO
    1. Remove blazeMeterUrl field & getter/setter;
    2. Pass version/url as arguments to getAPI();
    */

    public BlazemeterApi getAPI(String apiKey,ApiVersion version) {
        BlazemeterApi api=null;
        switch (version) {
            case v2:
                api=new BlazemeterApiV2Impl(apiKey);
                ((BlazemeterApiV2Impl)api).setBlazeMeterURL(this.blazeMeterUrl);
                break;
            case v3:
                api=new BlazemeterApiV3Impl(apiKey);
                ((BlazemeterApiV3Impl)api).setBlazeMeterURL(this.blazeMeterUrl);
                break;
        }
        return api;
    }


    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }
}
