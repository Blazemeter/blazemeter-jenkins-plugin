package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.api.ApiVersion;
import hudson.plugins.blazemeter.utils.Constants;

public class APIFactory {

    private APIFactory() {
    }

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
