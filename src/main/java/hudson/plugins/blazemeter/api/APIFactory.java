package hudson.plugins.blazemeter.api;

/**
 * Created by dzmitrykashlach on 11/11/14.
 */
public class APIFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static APIFactory apiFactory = null;

    private BlazemeterApiV2Impl apiV2=null;

    private APIFactory() {
    }


    public static APIFactory getApiFactory() {
        if (apiFactory == null) {
            apiFactory = new APIFactory();
        }
        return apiFactory;
    }

    public BlazemeterApi getAPI(ApiVersion version) {
        BlazemeterApi api = null;
        switch (version) {
            case v2:
            if(apiV2==null){
                api = new BlazemeterApiV2Impl();
            }
                break;
            case v3:
                break;
        }
        return api;
    }
}
