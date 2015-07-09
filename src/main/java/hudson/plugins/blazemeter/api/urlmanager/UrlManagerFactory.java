package hudson.plugins.blazemeter.api.urlmanager;


/**
 * Created by dzmitrykashlach on 11/11/14.
 */
public class UrlManagerFactory {
    public enum ApiVersion {
        v3, v2
    }

    private UrlManagerFactory() {
    }


    public static BmUrlManager getURLManager(ApiVersion version,String blazeMeterUrl) {
        BmUrlManager urlManager = null;
        switch (version) {
            case v2:
                urlManager=new BmUrlManagerV2Impl(blazeMeterUrl);
                break;
            case v3:
                urlManager = new BmUrlManagerV3Impl(blazeMeterUrl);
                break;
        }
        return urlManager;
    }
}
