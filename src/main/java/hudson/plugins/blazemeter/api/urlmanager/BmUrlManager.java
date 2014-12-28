package hudson.plugins.blazemeter.api.urlmanager;

import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.Utils;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public interface BmUrlManager {

    String CLIENT_IDENTIFICATION = "_clientId=CI_JENKINS&_clientVersion="
            + Utils.getVersion()+"&​";

    public String getServerUrl();

    public void setServerUrl(String serverUrl);

    public String testStatus(String appKey, String userKey, String testId);

    public String getTests(String appKey, String userKey);

    public String scriptUpload(String appKey, String userKey, String testId, String fileName);

    public String fileUpload(String appKey, String userKey, String testId, String fileName);

    public String testStart(String appKey, String userKey, String testId);

    public String testStop(String appKey, String userKey, String testId);

    public String testReport(String appKey, String userKey, String reportId);

    public String getUser(String appKey, String userKey);

    public String getTresholds(String appKey, String userKey, String sessionId);

    public String getTestInfo(String appKey, String userKey, String testId);

    public String createYahooTest(String appKey, String userKey);

    public String createTest(String appKey, String userKey);

    public String retrieveJUNITXML(String appKey, String userKey, String sessionId);

}

