package hudson.plugins.blazemeter.api;

public class APIFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static APIFactory apiFactory = null;

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
                    api = apiV2;
                }
                break;
            case v3:
                if (api == null || api instanceof BlazemeterApiV2Impl) {
                    if(apiV3==null){
                        apiV3 = new BlazemeterApiV3Impl(apiKey);
                    }
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
}
