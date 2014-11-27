package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;

public class APIFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static APIFactory apiFactory = null;

    private String blazeMeterUrl= Constants.DEFAULT_BLAZEMETER_URL;
    private ApiVersion version =null;

    private APIFactory() {
    }


    public static APIFactory getApiFactory() {
        if (apiFactory == null) {
            apiFactory = new APIFactory();
        }
        return apiFactory;
    }

    public BlazemeterApi getAPI(String apiKey) {
        if(version==null){
            version=ApiVersion.v3;
        }
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

    public ApiVersion getVersion() {
        return version;
    }

    public void setVersion(ApiVersion version) {
        this.version = version;
    }

    public String getBlazeMeterUrl() {
        return blazeMeterUrl;
    }

    public void setBlazeMeterUrl(String blazeMeterUrl) {
        this.blazeMeterUrl = blazeMeterUrl;
    }
}
