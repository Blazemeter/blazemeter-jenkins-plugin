package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.api.urlmanager.BmUrlManager;
import hudson.plugins.blazemeter.entities.TestInfo;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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

    public static final String APP_KEY = "jnk100x987c06f4e10c4";

    public void uploadJmx(String testId, File file);

    public JSONObject uploadBinaryFile(String testId, File file);

    public TestInfo getTestRunStatus(String testId);

    public JSONObject startTest(String testId);

    public int getTestCount() throws JSONException, IOException, ServletException;

    public JSONObject stopTest(String testId);

    public JSONObject testReport(String reportId);

    public HashMap<String, String> getTestList() throws IOException, MessagingException;

    public JSONObject getUser();

    public JSONObject getTresholds(String sessionId);

    public JSONObject getTestInfo(String testId);

    public JSONObject updateTestInfo(String testId, JSONObject data);

    public JSONObject createYahooTest(JSONObject data);

    public JSONObject createTest(JSONObject data,String testName);

    public String retrieveJUNITXML(String sessionId);

    public StdErrLog getLogger();

    public void setLogger(StdErrLog logger);

    public String getApiKey();

    public String getBlazeMeterURL();

    public BmUrlManager getUrlManager();
}
