package hudson.plugins.blazemeter.api.urlmanager;


/**
 * Created by dzmitrykashlach on 11/11/14.
 */
public class URLFactory {
    public enum ApiVersion {
        v3, v2
    }

    public static URLFactory urlFactory = null;

    private BmUrlManagerV2Impl bmUrlV2=null;
    private BmUrlManagerV3Impl bmUrlV3=null;

    private URLFactory() {
    }


    public static URLFactory getURLFactory() {
        if (urlFactory == null) {
            urlFactory = new URLFactory();
        }
        return urlFactory;
    }

    public BmUrlManager getURLManager(ApiVersion version,String blazeMeterUrl) {
        BmUrlManager urlManager = null;
        switch (version) {
            case v2:
                if(bmUrlV2==null){
                    bmUrlV2 = new BmUrlManagerV2Impl(blazeMeterUrl);
                }
                urlManager=bmUrlV2;
                break;
            case v3:
                if(bmUrlV3==null){
                    bmUrlV3 = new BmUrlManagerV3Impl(blazeMeterUrl);
                }
                urlManager=bmUrlV3;
                break;
        }
        return urlManager;
    }
}
