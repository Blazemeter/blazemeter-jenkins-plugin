package hudson.plugins.blazemeter.api;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * User: Vitali
 * Date: 4/2/12
 * Time: 14:05
 * <p/>
 * Updated
 * User: Doron
 * Date: 8/7/12
 *
 * Updated (proxy)
 * User: Marcel
 * Date: 9/23/13

 */

public interface BlazemeterApi {

    String APP_KEY = "jnk100x987c06f4e10c4";

    TestStatus getTestStatus(String id);

    int getTestMasterStatusCode(String id);

    String startTest(String testId,TestType testType) throws JSONException;

    int getTestCount() throws JSONException, IOException, ServletException;

    JSONObject stopTest(String testId);

    void terminateTest(String testId);

    JSONObject testReport(String reportId);

    LinkedHashMultimap<String, String> getTestsMultiMap() throws IOException, MessagingException;

    JSONObject getTestsJSON();

    JSONObject getUser();

    JSONObject getCIStatus(String sessionId) throws JSONException;

    JSONObject getTestConfig(String testId);

    boolean active(String testId);

    JSONObject postJsonConfig(String testId, JSONObject data);

    JSONObject createTest(JSONObject data);

    String retrieveJUNITXML(String sessionId);

    JSONObject retrieveJtlZip(String sessionId);

    JSONObject putTestInfo(String testId, JSONObject data);

    List<String> getListOfSessionIds(String masterId);

    void setBzmHttpWr(BzmHttpWrapper bzmhc);

    BzmHttpWrapper getBzmHttpWr();

    StdErrLog getLogger();

    void setLogger(StdErrLog logger);

    JSONObject generatePublicToken(String sessionId);

    String getApiKey();

    String getBlazeMeterURL();

    BmUrlManager getUrlManager();

    boolean ping() throws Exception;
}
