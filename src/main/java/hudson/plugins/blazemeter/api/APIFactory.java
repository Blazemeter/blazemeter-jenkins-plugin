package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;

public class APIFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static APIFactory apiFactory = null;

    private String blazeMeterUrl= Constants.DEFAULT_BLAZEMETER_URL;
    private BlazemeterApiV2Impl apiV2=null;
    private BlazemeterApiV3Impl apiV3=null;
    private BlazemeterApi api = null;
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
        switch (version) {
            case v2:
                if (api == null || api instanceof BlazemeterApiV3Impl) {
                    if(apiV2==null){
                     apiV2 = new BlazemeterApiV2Impl(apiKey);
                    }
                    apiV2.setBlazeMeterURL(this.blazeMeterUrl);
                    api = apiV2;
                }
                break;
            case v3:
                if (api == null || api instanceof BlazemeterApiV2Impl) {
                    if(apiV3==null){
                        apiV3 = new BlazemeterApiV3Impl(apiKey);
                    }
                    apiV3.setBlazeMeterURL(this.blazeMeterUrl);
                    api = apiV3;
                }
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
