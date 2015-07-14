package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.ApiVersion;
import hudson.plugins.blazemeter.utils.Constants;

public class APIFactory {

    private APIFactory() {
    }

    /*TODO
    1. Remove blazeMeterUrl field & getter/setter;
    2. Pass version/url as arguments to getAPI();
    */

    public static BlazemeterApi getAPI(String apiKey,ApiVersion version, String blazeMeterUrl) {
        BlazemeterApi api=null;
        switch (version) {
            case autoDetect:
                api=new BlazemeterApiV3Impl(apiKey,blazeMeterUrl);
                ((BlazemeterApiV3Impl)api).setBlazeMeterURL(blazeMeterUrl);
                break;
            case v2:
                api=new BlazemeterApiV2Impl(apiKey,blazeMeterUrl);
                ((BlazemeterApiV2Impl)api).setBlazeMeterURL(blazeMeterUrl);
                break;
            case v3:
                api=new BlazemeterApiV3Impl(apiKey,blazeMeterUrl);
                ((BlazemeterApiV3Impl)api).setBlazeMeterURL(blazeMeterUrl);
                break;
        }
        return api;
    }

}
