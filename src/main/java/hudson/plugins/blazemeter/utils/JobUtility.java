/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter.utils;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Result;
import hudson.plugins.blazemeter.*;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiV3Impl;
import hudson.plugins.blazemeter.entities.CIStatus;
import hudson.plugins.blazemeter.entities.TestStatus;
import hudson.plugins.blazemeter.testresult.TestResult;
import hudson.remoting.VirtualChannel;
import hudson.util.FormValidation;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.AbstractLogger;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static hudson.plugins.blazemeter.utils.Constants.ENCRYPT_CHARS_NUM;


public class JobUtility {
    private static StdErrLog logger = new StdErrLog(Constants.BZM_JEN);
    private final static int DELAY = 10000;

    private JobUtility() {
    }

    public static void waitForFinish(Api api, String testId, AbstractLogger bzmLog,
                                     String masterId) throws InterruptedException {
        Date start = null;
        long lastPrint = 0;
        while (true) {
            Thread.sleep(15000);
            TestStatus testStatus = api.getTestStatus(masterId);

            if (!testStatus.equals(TestStatus.Running)) {
                bzmLog.info("BlazeMeter TestStatus for masterId " +
                        masterId
                        + " is not 'Running': finishing build.... ");
                bzmLog.info("Timestamp: " + Calendar.getInstance().getTime());
                break;
            }

            if (start == null)
                start = Calendar.getInstance().getTime();
            long now = Calendar.getInstance().getTime().getTime();
            long diffInSec = (now - start.getTime()) / 1000;
            if (now - lastPrint > 60000) {
                bzmLog.info("BlazeMeter test# " + testId + ", masterId # " + masterId + " running from " + start + " - for " + diffInSec + " seconds");
                lastPrint = now;
            }

            if (Thread.interrupted()) {
                bzmLog.info(LogEntries.JOB_WAS_STOPPED_BY_USER);
                throw new InterruptedException(LogEntries.JOB_WAS_STOPPED_BY_USER);
            }
        }
    }

    public static String getReportUrl(Api api, String masterId,StdErrLog bzmLog) throws Exception {
        JSONObject jo = null;
        String publicToken = "";
        String reportUrl = null;
        StringBuilder letnry=new StringBuilder();
        letnry.append("Problems with generating public-token for report URL: ");
        try {
            jo = api.generatePublicToken(masterId);
            if (jo.get(JsonConsts.ERROR).equals(JSONObject.NULL)) {
                JSONObject result = jo.getJSONObject(JsonConsts.RESULT);
                publicToken = result.getString("publicToken");
                reportUrl = api.getBlazeMeterURL() + "/app/?public-token=" + publicToken + "#masters/" + masterId + "/summary";
            } else {
                bzmLog.warn(letnry.toString() + jo.get(JsonConsts.ERROR).toString());
                reportUrl = api.getBlazeMeterURL() + "/app/#masters/" + masterId + "/summary";
            }
        } catch (Exception e) {
            bzmLog.warn(letnry.toString() + jo.get(JsonConsts.ERROR).toString());
            reportUrl = api.getBlazeMeterURL() + "/app/#masters/" + masterId + "/summary";
        } finally {
            return reportUrl;
        }
    }

    public static String getSessionId(JSONObject json, StdErrLog bzmLog) throws JSONException {
        String session = "";
        try {
            JSONObject startJO = (JSONObject) json.get(JsonConsts.RESULT);
            session = ((JSONArray) startJO.get("sessionsId")).get(0).toString();
        } catch (Exception e) {
            bzmLog.info("Failed to get session_id. ", e);
        }
        return session;
    }

    public static void saveReport(String reportName,
                                  String report,
                                  FilePath filePath,
                                  StdErrLog bzmLog,
                                  StdErrLog consLog) {
        FilePath junit = null;
        StringBuilder letnry=new StringBuilder();
        letnry.append("ERROR: Failed to save XML report to filepath=");

        try {
            junit = new FilePath(filePath, reportName);
            if (!junit.exists()) {
                junit.touch(System.currentTimeMillis());
            }
            junit.write(report, System.getProperty("file.encoding"));
        } catch (FileNotFoundException e) {
            bzmLog.info(letnry.toString()+ junit.getParent() + "/" + junit.getName() + " : " + e.getMessage());
            consLog.info(letnry.toString()+ junit.getParent() + "/" + junit.getName() + " : " + e.getMessage());
        } catch (IOException e) {
            bzmLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
            consLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
        } catch (InterruptedException e) {
            bzmLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
            consLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
        }
    }

    public static CIStatus validateCIStatus(Api api, String session, StdErrLog bzmLog, StdErrLog consLog) {
        CIStatus ciStatus = CIStatus.success;
        JSONObject jo;
        JSONArray failures = new JSONArray();
        JSONArray errors = new JSONArray();
        StringBuilder lentry = new StringBuilder();
        lentry.append("No thresholds on server: setting 'success' for CIStatus ");
        try {
            jo = api.getCIStatus(session);
            bzmLog.info("Test status object = " + jo.toString());
            consLog.info("Test status object = " + jo.toString());
            failures = jo.getJSONArray(JsonConsts.FAILURES);
            errors = jo.getJSONArray(JsonConsts.ERRORS);
        } catch (JSONException je) {
            bzmLog.warn(lentry.toString());
            consLog.warn(lentry.toString());
        } catch (Exception e) {
            bzmLog.warn(lentry.toString());
            consLog.warn(lentry.toString());
        } finally {
            lentry.setLength(0);
            lentry.append(" while test status validation...");
            if (errors.length() > 0) {
                bzmLog.info("Having errors " + lentry.toString());
                consLog.info("Having errors " + lentry.toString());
                bzmLog.info("Errors: " + errors.toString());
                consLog.info("Errors: " + errors.toString());
                ciStatus = errorsFailed(errors) ? CIStatus.failures : CIStatus.errors;
                bzmLog.info("Setting CIStatus=" + ciStatus.name());
                consLog.info("Setting CIStatus=" + ciStatus.name());
            }
            if (failures.length() > 0) {
                bzmLog.info("Having failures " + lentry.toString());
                consLog.info("Having failures " + lentry.toString());
                bzmLog.info("Failures: " + failures.toString());
                consLog.info("Failures: " + failures.toString());
                ciStatus = CIStatus.failures;
                bzmLog.info("Setting CIStatus=" + CIStatus.failures.name());
                consLog.info("Setting CIStatus=" + CIStatus.failures.name());
                return ciStatus;
            }
            if (ciStatus.equals(CIStatus.success)) {
                bzmLog.info("No errors/failures while validating CIStatus: setting " + CIStatus.success.name());
                consLog.info("No errors/failures while validating CIStatus: setting " + CIStatus.success.name());
            }
        }
        return ciStatus;
    }

    public static boolean errorsFailed(JSONArray errors) {
        int l = errors.length();
        for (int i = 0; i < l; i++) {
            try {
                if (errors.getJSONObject(i).getInt(JsonConsts.CODE) == 0 | errors.getJSONObject(i).getInt(JsonConsts.CODE) == 70404) {
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException je) {
                return false;
            }
        }
        return false;
    }

    public static String selectUserKeyOnId(BlazeMeterPerformanceBuilderDescriptor descriptor,
                                           String id) {
        String userKey = null;
        List<BlazemeterCredentialImpl> credentialList = descriptor.getCredentials("Global");
        if (credentialList.size() == 1) {
            userKey = credentialList.get(0).getApiKey();
        } else {
            for (BlazemeterCredentialImpl c : credentialList) {
                if (c.getId().equals(id)) {
                    userKey = c.getApiKey();
                    break;
                }
            }
        }
        return userKey;
    }

    public static HashMap<String,String> jtlUrls(Api api, String masterId,StdErrLog bzmLog,StdErrLog consLog){
        HashMap<String,String> jtlUrls=new HashMap<String, String>();
        List<String> sessionsIds = api.getListOfSessionIds(masterId);
        for (String s : sessionsIds) {
            StringBuilder dataUrl=new StringBuilder();
            JSONObject jo = api.retrieveJtlZip(s);
            try {
                JSONArray data = jo.getJSONObject(JsonConsts.RESULT).getJSONArray(JsonConsts.DATA);
                for (int i = 0; i < data.length(); i++) {
                    String title = data.getJSONObject(i).getString("title");
                    if (title.equals("Zip")) {
                        dataUrl.append(data.getJSONObject(i).getString(JsonConsts.DATA_URL));
                        jtlUrls.put(s,dataUrl.toString());
                        bzmLog.info("SessionId="+s+", jtlUrl="+dataUrl.toString());
                        consLog.info("SessionId="+s+", jtlUrl="+dataUrl.toString());
                        dataUrl.setLength(0);
                        break;
                    }
                }
            } catch (JSONException e) {
                bzmLog.info("Failed to get url for JTL report, sessionId="+s,e);
                consLog.info("Failed to get url for JTL report, sessionId="+s,e);
            }

        }
        return jtlUrls;
    }

    public static void downloadJtlReport(String sessionId, String jtlUrl, FilePath filePath, StdErrLog bzmLog,StdErrLog consLog) {
        URL url = null;
        try {
            url = new URL(jtlUrl);
            int i = 1;
            boolean jtl = false;
            while (!jtl && i < 4) {
                try {
                    bzmLog.info("Downloading JTLZIP for sessionId = " + sessionId + " attemp # " + i);
                    consLog.info("Downloading JTLZIP for sessionId = " + sessionId + " attemp # " + i);
                    int conTo = (int) (10000 * Math.pow(3, i - 1));
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(conTo);
                    connection.setReadTimeout(30000);
                    InputStream input = connection.getInputStream();
                    filePath.unzipFrom(input);
                    jtl = true;
                } catch (MalformedURLException e) {
                    bzmLog.warn("It seems like test was terminated on server side...");
                    consLog.warn("It seems like test was terminated on server side...");
                    bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts");
                    consLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts");
                } catch (Exception e) {
                    bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts" + e);
                    consLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts" + e);
                } finally {
                    i++;
                }
            }

            FilePath sample_jtl = new FilePath(filePath, "sample.jtl");
            FilePath bm_kpis_jtl = new FilePath(filePath, Constants.BM_KPIS);
            if (sample_jtl.exists()) {
                sample_jtl.renameTo(bm_kpis_jtl);
            }
        } catch (MalformedURLException e) {
            bzmLog.warn("It seems like test was terminated on server side...");
            consLog.warn("It seems like test was terminated on server side...");
            bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts " + e);
            consLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts " + e);
        } catch (IOException e) {
            bzmLog.warn("Unable to get JTLZIP from " + url, e);
            consLog.warn("Unable to get JTLZIP from " + url, e);
        } catch (InterruptedException e) {
            bzmLog.warn("Unable to get JTLZIP from " + url, e);
            consLog.warn("Unable to get JTLZIP from " + url, e);
        }
    }

    public static void downloadJtlReports(HashMap<String,String> jtlUrls, FilePath filePath,
                                          StdErrLog bzmBuildLog,StdErrLog consLog) {
        Set<String> sessionsIds=jtlUrls.keySet();
        for (String s : sessionsIds) {
            FilePath jtl = new FilePath(filePath, s + Constants.BM_ARTEFACTS);
            downloadJtlReport(s, jtlUrls.get(s),jtl, bzmBuildLog,consLog);
        }
    }


    public static void retrieveJUNITXMLreport(Api api, String masterId,
                                              FilePath junitPath, StdErrLog bzmLog,StdErrLog consLog) {
        String junitReport = "";
        bzmLog.info("Requesting JUNIT report from server, masterId=" + masterId);
        consLog.info("Requesting JUNIT report from server, masterId=" + masterId);
        try {
            junitReport = api.retrieveJUNITXML(masterId);
            String junitName = masterId + "-" + Constants.BM_TRESHOLDS;
            bzmLog.info("Received Junit report from server.... masterId=" + masterId);
            consLog.info("Received Junit report from server.... masterId=" + masterId);
            bzmLog.info("Saving it to " + junitPath + " with name=" + junitName);
            consLog.info("Saving it to " + junitPath + " with name=" + junitName);
            saveReport(junitName, junitReport, junitPath, bzmLog, consLog);
        } catch (Exception e) {
            bzmLog.warn("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
            consLog.warn("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
        }
    }

    public static Result postProcess(
            FilePath workspace,
            String buildId,
            Api api,
            String masterId,
            EnvVars envVars,
            boolean isJunit,
            String junitPathStr,
            boolean isJtl,
            String jtlPathStr,
            StdErrLog bzmLog,
            StdErrLog consLog) throws InterruptedException {
        Thread.sleep(10000); // Wait for the report to generate.
        //get thresholds from server and check if test is success
        Result result;
        CIStatus ciStatus = JobUtility.validateCIStatus(api, masterId, bzmLog,consLog);
        if (ciStatus.equals(CIStatus.errors)) {
            result = Result.UNSTABLE;
            return result;
        }
        result = ciStatus.equals(CIStatus.failures) ? Result.FAILURE : Result.SUCCESS;
        FilePath dfp = new FilePath(workspace, buildId);
        if (isJunit) {
            FilePath junitPath = null;
            try {
                junitPath = Utils.resolvePath(dfp, junitPathStr, envVars);
            } catch (Exception e) {
                bzmLog.warn("Failed to resolve jtlPath: " + e.getMessage());
                consLog.warn("Failed to resolve jtlPath: " + e.getMessage());
                bzmLog.warn("JTL report will be saved to workspace");
                consLog.warn("JTL report will be saved to workspace");
                junitPath = dfp;
            }
            retrieveJUNITXMLreport(api, masterId, junitPath, bzmLog,consLog);
        } else {
            bzmLog.info("JUNIT report won't be requested: check-box is unchecked.");
            consLog.info("JUNIT report won't be requested: check-box is unchecked.");
        }
        Thread.sleep(30000);
        FilePath jtlPath = null;
        HashMap<String,String> jtlUrls=JobUtility.jtlUrls(api,masterId,bzmLog,consLog);
        if (isJtl) {
            if (StringUtil.isBlank(jtlPathStr)) {
                jtlPath = dfp;
            } else {
                try {
                    jtlPath = Utils.resolvePath(dfp, jtlPathStr, envVars);
                    bzmLog.info("Will use the following path for JTL: " +  jtlPath.getParent().getName() + "/" + jtlPath.getName());
                    consLog.info("Will use the following path for JTL: " +  jtlPath.getParent().getName() + "/" + jtlPath.getName());
                } catch (Exception e) {
                    bzmLog.warn("Failed to resolve jtlPath: " + e.getMessage());
                    consLog.warn("Failed to resolve jtlPath: " + e.getMessage());
                    bzmLog.warn("JTL report will be saved to workspace");
                    consLog.warn("JTL report will be saved to workspace");
                    jtlPath = dfp;
                }
                bzmLog.info("Will use the following path for JTL: " +
                        jtlPath.getParent().getName() + "/" + jtlPath.getName());
                consLog.info("Will use the following path for JTL: " +
                        jtlPath.getParent().getName() + "/" + jtlPath.getName());
            }
            JobUtility.downloadJtlReports(jtlUrls,jtlPath,bzmLog,consLog);
        } else {
            bzmLog.info("JTL report won't be requested: check-box is unchecked.");
            consLog.info("JTL report won't be requested: check-box is unchecked.");
        }


        //get testGetArchive information
        JSONObject testReport = requestAggregateReport(api,masterId,bzmLog,consLog);


        if (testReport == null || testReport.equals("null")) {
            bzmLog.info("Aggregate report is not available after 4 attempts.");
            consLog.info("Aggregate report is not available after 4 attempts.");
            return result;
        }
        TestResult testResult = null;
        try {
            testResult = new TestResult(testReport);
            bzmLog.info(testResult.toString());
            consLog.info(testResult.toString());
        } catch (IOException ioe) {
            bzmLog.info("Failed to get test result. Try to check server for it");
            consLog.info("Failed to get test result. Try to check server for it");
            bzmLog.info("ERROR: Failed to generate TestResult: " + ioe);
            consLog.info("ERROR: Failed to generate TestResult: " + ioe);
        } catch (JSONException je) {
            bzmLog.info("Failed to get test result. Try to check server for it");
            consLog.info("Failed to get test result. Try to check server for it");
            bzmLog.info("ERROR: Failed to generate TestResult: " + je);
            consLog.info("ERROR: Failed to generate TestResult: " + je);
        } finally {
            return result;
        }

    }


    public static JSONObject requestAggregateReport(Api api, String masterId,StdErrLog bzmLog,StdErrLog consLog) {
        JSONObject testReport = null;
        int retries = 1;
        try {
            while (retries < 5 && testReport == null) {
                bzmLog.info("Trying to get aggregate test report from server, attempt# " + retries);
                consLog.info("Trying to get aggregate test report from server, attempt# " + retries);
                testReport = api.testReport(masterId);
                if (testReport != null) {
                    return testReport;
                }
                Thread.sleep(5000);
                retries++;
            }
        } catch (Exception e) {
            bzmLog.info("Failed to get test report from server.");
            consLog.info("Failed to get test report from server.");
        }
        return testReport;
    }

    public static boolean notes(Api api, String masterId, String notes, StdErrLog jenBuildLog) {
        boolean note = false;
        int n = 1;
        while (!note && n < 6) {
            try {
                Thread.sleep(DELAY);
                int statusCode = api.getTestMasterStatusCode(masterId);
                if (statusCode > 20) {
                    note = api.notes(notes, masterId);
                }
            } catch (Exception e) {
                jenBuildLog.warn("Failed to PATCH notes to test report on server: masterId=" + masterId + " " + e.getMessage());
            } finally {
                n++;
            }

        }
        return note;
    }

    public static JSONArray prepareSessionProperties(String sesssionProperties, EnvVars vars, StdErrLog jenBuildLog) throws JSONException {
        List<String> propList = Arrays.asList(sesssionProperties.split(","));
        JSONArray props = new JSONArray();
        StrSubstitutor strSubstr = new StrSubstitutor(vars);
        jenBuildLog.info("Preparing jmeter properties for the test...");
        for (String s : propList) {
            try {
                JSONObject prop = new JSONObject();
                List<String> pr = Arrays.asList(s.split("="));
                if (pr.size() > 1) {
                    prop.put("key", strSubstr.replace(pr.get(0)).trim());
                    prop.put("value", strSubstr.replace(pr.get(1)).trim());
                }
                props.put(prop);
            } catch (Exception e) {
                jenBuildLog.warn("Failed to prepare jmeter property " + s + " for the test: " + e.getMessage());
            }
        }
        jenBuildLog.info("Prepared JSONArray of jmeter properties: " + props.toString());
        return props;
    }


    public static boolean stopTestSession(Api api, String masterId, StdErrLog jenBuildLog) {
        boolean terminate = false;
        try {
            int statusCode = api.getTestMasterStatusCode(masterId);
            if (statusCode < 100 & statusCode != 0) {
                api.terminateTest(masterId);
                terminate = true;
            }
            if (statusCode >= 100 | statusCode == -1 | statusCode == 0) {
                api.stopTest(masterId);
                terminate = false;
            }
        } catch (Exception e) {
            jenBuildLog.warn("Error while trying to stop test with testId=" + masterId + ", " + e.getMessage());
        } finally {
            return terminate;
        }
    }

    public static String version() {
        Properties props = new Properties();
        try {
            props.load(JobUtility.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty(Constants.VERSION, "N/A");
        }
        return props.getProperty(Constants.VERSION);
    }

    public static String emptyBodyJson() {
        String eb = "";
        try {
            InputStream is = JobUtility.class.getResourceAsStream("emptyBody.json");
            eb = IOUtils.toString(is);
        } catch (IOException ex) {
        } finally {
            return eb;
        }
    }

    public static FormValidation validateUserKey(String userKey, String blazeMeterUrl) {
        if (userKey.isEmpty()) {
            logger.warn(Constants.API_KEY_EMPTY);
            return FormValidation.errorWithMarkup(Constants.API_KEY_EMPTY);
        }
        String encryptedKey = userKey.substring(0, ENCRYPT_CHARS_NUM) + "...";
        try {
            logger.info("Validating API key started: API key=" + encryptedKey);
            Api bzm = new ApiV3Impl(userKey, blazeMeterUrl);
            logger.info("Getting user details from server: serverUrl=" + blazeMeterUrl);
            JSONObject u = bzm.getUser();
            net.sf.json.JSONObject user = null;
            if (u != null) {
                logger.warn("Received user information:");
                logger.warn(u.toString());
                user = net.sf.json.JSONObject.fromObject(u.toString());
                if (user.has(JsonConsts.ERROR) && !user.get(JsonConsts.ERROR).equals(null)) {
                    logger.warn("API key is not valid: error=" + user.get(JsonConsts.ERROR).toString());
                    logger.warn("User profile: " + user.toString());
                    return FormValidation.errorWithMarkup("API key is not valid: error=" + user.get(JsonConsts.ERROR).toString());
                } else {
                    logger.warn(Constants.API_KEY_VALID + user.getString(JsonConsts.MAIL));
                    return FormValidation.ok(Constants.API_KEY_VALID + user.getString(JsonConsts.MAIL));
                }
            }
        } catch (ClassCastException e) {
            logger.warn("API key is not valid: unexpected exception=" + e.getMessage().toString());
            logger.warn(e);
        } catch (Exception e) {
            logger.warn("API key is not valid: unexpected exception=" + e.getMessage().toString());
            logger.warn(e);
            return FormValidation.errorWithMarkup("API key is not valid: unexpected exception=" + e.getMessage().toString());
        }
        logger.warn("API key is not valid: userKey=" + encryptedKey + " blazemeterUrl=" + blazeMeterUrl);
        logger.warn(" Please, check proxy settings, serverUrl and userKey.");
        return FormValidation.error("API key is not valid: API key=" + encryptedKey + " blazemeterUrl=" + blazeMeterUrl +
                ". Please, check proxy settings, serverUrl and userKey.");
    }

    public static String getUserEmail(String userKey, String blazemeterUrl, VirtualChannel c) {
        Api bzm = new ApiV3Impl(userKey, blazemeterUrl);
        try {
            net.sf.json.JSONObject user = net.sf.json.JSONObject.fromObject(bzm.getUser().toString());
            if (user.has(JsonConsts.MAIL)) {
                return user.getString(JsonConsts.MAIL);
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static String getUserEmail(String userKey, String blazemeterUrl) {
        return getUserEmail(userKey, blazemeterUrl, null);
    }


    public static void properties(Api api, JSONArray properties, String masterId, StdErrLog jenBuildLog) {
        List<String> sessionsIds = api.getListOfSessionIds(masterId);
        jenBuildLog.info("Trying to submit jmeter properties: got " + sessionsIds.size() + " sessions");
        for (String s : sessionsIds) {
            jenBuildLog.info("Submitting jmeter properties to sessionId=" + s);
            int n = 1;
            boolean submit = false;
            while (!submit && n < 6) {
                try {
                    submit = api.properties(properties, s);
                    if (!submit) {
                        jenBuildLog.warn("Failed to submit jmeter properties to sessionId=" + s + " retry # " + n);
                        Thread.sleep(DELAY);
                    }
                } catch (Exception e) {
                    jenBuildLog.warn("Failed to submit jmeter properties to sessionId=" + s, e);
                } finally {
                    n++;
                }
            }
        }
    }
}
