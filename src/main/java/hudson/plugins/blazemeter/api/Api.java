package hudson.plugins.blazemeter.api;

import com.google.common.collect.LinkedHashMultimap;
import hudson.plugins.blazemeter.api.urlmanager.UrlManager;
import hudson.plugins.blazemeter.entities.TestStatus;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
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

public interface Api {

    String APP_KEY = "jnk100x987c06f4e10c4";

    TestStatus getTestStatus(String id);

    int getTestMasterStatusCode(String id);

    String startTest(String testId, TestType testType) throws JSONException;

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

    String retrieveJUNITXML(String sessionId);

    JSONObject retrieveJtlZip(String sessionId);

    List<String> getListOfSessionIds(String masterId);

    void setBzmHttpWr(HttpUtil bzmhc);

    HttpUtil getBzmHttpWr();

    StdErrLog getLogger();

    void setLogger(StdErrLog logger);

    JSONObject generatePublicToken(String sessionId);

    String getApiKey();

    String getBlazeMeterURL();

    UrlManager getUrlManager();

    boolean ping() throws Exception;

    boolean notes(String note,String masterId)throws Exception;

    boolean properties(JSONArray properties, String sessionId) throws Exception;
}
