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
                bzmLog.info("BlazeMeter TestStatus for masterId" +
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
                                  StdErrLog jenBuildLog) {
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
            jenBuildLog.info(letnry.toString()+ junit.getParent() + "/" + junit.getName() + " : " + e.getMessage());
        } catch (IOException e) {
            jenBuildLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
        } catch (InterruptedException e) {
            jenBuildLog.info(letnry.toString() + filePath.getParent() + "/" + filePath.getName() + " : " + e.getMessage());
        }
    }

    public static CIStatus validateCIStatus(Api api, String session, StdErrLog jenBuildLog) {
        CIStatus ciStatus = CIStatus.success;
        JSONObject jo;
        JSONArray failures = new JSONArray();
        JSONArray errors = new JSONArray();
        StringBuilder lentry=new StringBuilder();
        lentry.append("No thresholds on server: setting 'success' for CIStatus ");
        try {
            jo = api.getCIStatus(session);
            jenBuildLog.info("Test status object = " + jo.toString());
            failures = jo.getJSONArray(JsonConsts.FAILURES);
            errors = jo.getJSONArray(JsonConsts.ERRORS);
        } catch (JSONException je) {
            jenBuildLog.warn(lentry.toString());
        } catch (Exception e) {
            jenBuildLog.warn(lentry.toString());
        } finally {
            lentry.setLength(0);
            lentry.append(" while test status validation...");
            if (errors.length() > 0) {
                jenBuildLog.info("Having errors "+lentry.toString());
                jenBuildLog.info("Errors: " + errors.toString());
                ciStatus = CIStatus.errors;
                jenBuildLog.info("Setting CIStatus=" + CIStatus.errors.name());
                return ciStatus;
            }
            if (failures.length() > 0) {
                jenBuildLog.info("Having failures "+lentry.toString());
                jenBuildLog.info("Failures: " + failures.toString());
                ciStatus = CIStatus.failures;
                jenBuildLog.info("Setting CIStatus=" + CIStatus.failures.name());
                return ciStatus;
            }
            jenBuildLog.info("No errors/failures while validating CIStatus: setting " + CIStatus.success.name());
        }
        return ciStatus;
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

    public static HashMap<String,String> jtlUrls(Api api, String masterId,StdErrLog bzmLog){
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
                        bzmLog.warn("SessionId="+s+", jtlUrl="+dataUrl.toString());
                        dataUrl.setLength(0);
                        break;
                    }
                }
            } catch (JSONException e) {
                bzmLog.warn("Failed to get url for JTL report, sessionId="+s,e);
            }

        }
        return jtlUrls;
    }

    public static void downloadJtlReport(String sessionId, String jtlUrl, FilePath filePath, StdErrLog bzmLog) {
        URL url = null;
        try {
            url = new URL(jtlUrl);
            int i = 1;
            boolean jtl = false;
            while (!jtl && i < 4) {
                try {
                    bzmLog.info("Downloading JTLZIP for sessionId = " + sessionId + " attemp # " + i);
                    int conTo = (int) (10000 * Math.pow(3, i - 1));
                    URLConnection connection = url.openConnection();
                    connection.setConnectTimeout(conTo);
                    connection.setReadTimeout(30000);
                    InputStream input = connection.getInputStream();
                    filePath.unzipFrom(input);
                    jtl = true;
                } catch (MalformedURLException e) {
                    bzmLog.warn("It seems like test was terminated on server side...");
                    bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts");
                } catch (Exception e) {
                    bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts" + e);
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
            bzmLog.warn("Unable to get JTLZIP for sessionId=" + sessionId + ":check server for test artifacts " + e);
        } catch (IOException e) {
            bzmLog.warn("Unable to get JTLZIP from " + url, e);
        } catch (InterruptedException e) {
            bzmLog.warn("Unable to get JTLZIP from " + url, e);
        }
    }

    public static void downloadJtlReports(HashMap<String,String> jtlUrls, FilePath filePath, StdErrLog bzmBuildLog) {
        Set<String> sessionsIds=jtlUrls.keySet();
        for (String s : sessionsIds) {
            FilePath jtl = new FilePath(filePath, s + Constants.BM_ARTEFACTS);
            downloadJtlReport(s, jtlUrls.get(s),jtl, bzmBuildLog);
        }
    }


    public static void retrieveJUNITXMLreport(Api api, String masterId,
                                              FilePath junitPath, StdErrLog jenBuildLog) {
        String junitReport = "";
        jenBuildLog.info("Requesting JUNIT report from server, masterId=" + masterId);
        try {
            junitReport = api.retrieveJUNITXML(masterId);
            String junitName = masterId + "-" + Constants.BM_TRESHOLDS;
            jenBuildLog.info("Received Junit report from server.... masterId=" + masterId);
            jenBuildLog.info("Saving it to " + junitPath + " with name=" + junitName);
            saveReport(junitName, junitReport, junitPath, jenBuildLog);
        } catch (Exception e) {
            jenBuildLog.warn("Problems with receiving JUNIT report from server, masterId=" + masterId + ": " + e.getMessage());
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
            StdErrLog bzmLog) throws InterruptedException {
        Thread.sleep(10000); // Wait for the report to generate.
        //get thresholds from server and check if test is success
        Result result;
        CIStatus ciStatus = JobUtility.validateCIStatus(api, masterId, bzmLog);
        if (ciStatus.equals(CIStatus.errors)) {
            result = Result.FAILURE;
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
                bzmLog.warn("JTL report will be saved to workspace");
                junitPath = dfp;
            }
            retrieveJUNITXMLreport(api, masterId, junitPath, bzmLog);
        } else {
            bzmLog.info("JUNIT report won't be requested: check-box is unchecked.");
        }
        Thread.sleep(30000);
        FilePath jtlPath = null;
        HashMap<String,String> jtlUrls=JobUtility.jtlUrls(api,masterId,bzmLog);
        if (isJtl) {
            if (StringUtil.isBlank(jtlPathStr)) {
                jtlPath = dfp;
            } else {
                try {
                    jtlPath = Utils.resolvePath(dfp, jtlPathStr, envVars);
                    bzmLog.info("Will use the following path for JTL: " +  jtlPath.getParent().getName() + "/" + jtlPath.getName());
                } catch (Exception e) {
                    bzmLog.warn("Failed to resolve jtlPath: " + e.getMessage());
                    bzmLog.warn("JTL report will be saved to workspace");
                    jtlPath = dfp;
                }
                bzmLog.info("Will use the following path for JTL: " +
                        jtlPath.getParent().getName() + "/" + jtlPath.getName());
            }
            JobUtility.downloadJtlReports(jtlUrls,jtlPath,bzmLog);
        } else {
            bzmLog.info("JTL report won't be requested: check-box is unchecked.");
        }


        //get testGetArchive information
        JSONObject testReport = requestAggregateReport(api, bzmLog, masterId);


        if (testReport == null || testReport.equals("null")) {
            bzmLog.warn("Aggregate report is not available after 4 attempts.");
            return result;
        }
        TestResult testResult = null;
        try {
            testResult = new TestResult(testReport);
            bzmLog.info(testResult.toString());
        } catch (IOException ioe) {
            bzmLog.info("Failed to get test result. Try to check server for it");
            bzmLog.info("ERROR: Failed to generate TestResult: " + ioe);
        } catch (JSONException je) {
            bzmLog.info("Failed to get test result. Try to check server for it");
            bzmLog.info("ERROR: Failed to generate TestResult: " + je);
        } finally {
            return result;
        }

    }


    public static JSONObject requestAggregateReport(Api api, StdErrLog jenBuildLog, String masterId) {
        JSONObject testReport = null;
        int retries = 1;
        try {
            while (retries < 5 && testReport == null) {
                jenBuildLog.info("Trying to get aggregate test report from server, attempt# " + retries);
                testReport = api.testReport(masterId);
                if (testReport != null) {
                    return testReport;
                }
                Thread.sleep(5000);
                retries++;
            }
        } catch (Exception e) {
            jenBuildLog.info("Failed to get test report from server.");
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

    public static String getVersion() {
        Properties props = new Properties();
        try {
            props.load(JobUtility.class.getResourceAsStream("version.properties"));
        } catch (IOException ex) {
            props.setProperty(Constants.VERSION, "N/A");
        }
        return props.getProperty(Constants.VERSION);
    }

    public static FormValidation validateUserKey(String userKey, String blazeMeterUrl) {
        if (userKey.isEmpty()) {
            logger.warn(Constants.API_KEY_EMPTY);
            return FormValidation.errorWithMarkup(Constants.API_KEY_EMPTY);
        }
        String encryptedKey = userKey.substring(0, 4) + "..." + userKey.substring(17);
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
