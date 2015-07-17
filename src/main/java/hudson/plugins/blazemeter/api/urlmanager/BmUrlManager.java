package hudson.plugins.blazemeter.api.urlmanager;

import hudson.plugins.blazemeter.utils.BzmServiceManager;

/**
 * Created by dzmitrykashlach on 10/11/14.
 */
public interface BmUrlManager {

    String CLIENT_IDENTIFICATION = "_clientId=CI_JENKINS&_clientVersion="
            + BzmServiceManager.getVersion()+"&​";

    String getServerUrl();

    void setServerUrl(String serverUrl);

    String testMasterStatus(String appKey, String userKey, String testId);

    String getTests(String appKey, String userKey);

    String scriptUpload(String appKey, String userKey, String testId, String fileName);

    String fileUpload(String appKey, String userKey, String testId, String fileName);

    String testStart(String appKey, String userKey, String testId);

    String collectionStart(String appKey, String userKey, String collectionId);

    String testStop(String appKey, String userKey, String testId);

    String testTerminate(String appKey, String userKey, String testId);

    String testReport(String appKey, String userKey, String reportId);

    String getUser(String appKey, String userKey);

    String getTresholds(String appKey, String userKey, String sessionId);

    String getTestConfig(String appKey, String userKey, String testId);

    String postJsonConfig(String appKey, String userKey, String testId);

    String createTest(String appKey, String userKey);

    String retrieveJUNITXML(String appKey, String userKey, String sessionId);

    String retrieveJTLZIP(String appKey, String userKey, String sessionId);

    String generatePublicToken(String appKey, String userKey, String sessionId);

    String listOfSessionIds(String appKey, String userKey, String masterId);

}

