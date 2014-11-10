package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.entities.TestInfo;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

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
    PrintStream logger = new PrintStream(System.out);

    public static final String APP_KEY = "jnk100x987c06f4e10c4";

    public void uploadJmx(String userKey, String testId, File file);

    public JSONObject uploadBinaryFile(String userKey, String testId, File file);

    public TestInfo getTestRunStatus(String userKey, String testId);

    public JSONObject startTest(String userKey, String testId);

    public int getTestCount(String userKey) throws JSONException, IOException, ServletException;

    public JSONObject stopTest(String userKey, String testId);

    public JSONObject aggregateReport(String userKey, String reportId);

    public HashMap<String, String> getTestList(String userKey) throws IOException, MessagingException;
}
