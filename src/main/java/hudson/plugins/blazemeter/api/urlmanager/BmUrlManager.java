package hudson.plugins.blazemeter.api.urlmanager;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public interface BmUrlManager {

    public String getServerUrl();

    public String testStatus(String appKey, String userKey, String testId);

    public String getTests(String appKey, String userKey);

    public String scriptUpload(String appKey, String userKey, String testId, String fileName);

    public String fileUpload(String appKey, String userKey, String testId, String fileName);
    public String testStart(String appKey, String userKey, String testId);

    public String testStop(String appKey, String userKey, String testId);


    public String testReport(String appKey, String userKey, String reportId);

    public String getUser(String appKey, String userKey);

}

