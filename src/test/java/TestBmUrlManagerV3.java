import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.api.urlmanager.UrlManagerFactory;
import hudson.plugins.blazemeter.utils.Constants;

/**
 * Created by dzmitrykashlach on 9/01/15.
 */
public class TestBmUrlManagerV3 {
    private BmUrlManager bmUrlManager=
            UrlManagerFactory.getURLFactory().getURLManager(UrlManagerFactory.ApiVersion.v3,
            Constants.DEFAULT_BLAZEMETER_URL);

}
