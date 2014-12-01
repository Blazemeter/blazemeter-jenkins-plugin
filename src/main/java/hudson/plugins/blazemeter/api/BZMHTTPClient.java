package hudson.plugins.blazemeter.api;

import hudson.ProxyConfiguration;
import hudson.model.Hudson;
import hudson.plugins.blazemeter.utils.Constants;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class BZMHTTPClient {
    JavaUtilLog logger = new JavaUtilLog(Constants.BZM_JEN);

    public enum Method {GET, POST, PUT}

    private transient DefaultHttpClient httpClient = null;

    public BZMHTTPClient() {
        this.httpClient = new DefaultHttpClient();
    }


    HttpResponse getResponse(String url, JSONObject data, Method method) throws IOException {

        logger.warn("Requesting : " + url);
        HttpResponse response = null;
        HttpRequestBase request = null;

        try {
            if (method == Method.GET) {
                request = new HttpGet(url);
            } else if (method == Method.POST) {
                request = new HttpPost(url);
                if (data != null) {
                    ((HttpPost) request).setEntity(new StringEntity(data.toString()));
                }
            } else if (method == Method.PUT) {
                request = new HttpPut(url);
                if (data != null) {
                    ((HttpPut) request).setEntity(new StringEntity(data.toString()));
                }
            }
            else {
                throw new Exception("Unsupported method: " + method.toString());
            }
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-type", "application/json; charset=UTF-8");

            response = this.httpClient.execute(request);


            if (response == null || response.getStatusLine() == null) {
                logger.warn("Erroneous response (Probably null) for url: \n", url);
                response = null;
            }
        } catch (Exception e) {
            logger.warn("Problems with creating and sending request: \n", e);
        }
        return response;
    }

    HttpResponse getResponseForFileUpload(String url, File file) throws IOException {

        logger.warn("Requesting : " + url);
        HttpResponse response = null;

        try {
            HttpPost postRequest = new HttpPost(url);
            postRequest.setHeader("Accept", "application/json");
            postRequest.setHeader("Content-type", "application/json; charset=UTF-8");

            if (file != null) {
                postRequest.setEntity(new FileEntity(file, "text/plain; charset=\"UTF-8\""));
            }

            response = this.httpClient.execute(postRequest);

            if (response != null && response.getStatusLine() != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                String error = response.getStatusLine().getReasonPhrase();
                if ((statusCode >= 300) || (statusCode < 200)) {
                    throw new RuntimeException(String.format("Failed : %d %s", statusCode, error));
                }
            } else {
                logger.warn("Erroneous response (Probably null) for url: "+ url);
                response = null;
            }
        } catch (Exception e) {
            logger.warn("Wrong response: \n", e);
        }
        return response;
    }

    JSONObject getJsonForFileUpload(String url, File file) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponseForFileUpload(url, file);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.warn(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.warn("ERROR decoding Json ", e);
        } catch (JSONException e) {
            logger.warn("ERROR decoding Json ", e);
        }
        return jo;
    }

    JSONObject getJson(String url, JSONObject data, Method method) {
        JSONObject jo = null;
        try {
            HttpResponse response = getResponse(url, data, method);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                logger.warn(output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            logger.warn("ERROR decoding Json ", e);
        } catch (JSONException e) {
            logger.warn("ERROR decoding Json ", e);
        }
        return jo;
    }

    String getString(String url, JSONObject data, Method method){
        String  str = null;
        try {
            HttpResponse response = getResponse(url, data, method);
            if (response != null) {
                str = EntityUtils.toString(response.getEntity());
                logger.warn(str);
            }
        } catch (IOException e) {
            logger.warn("ERROR decoding Json ", e);
        }
        return str;
    }

    void configureProxy() {
        if (Hudson.getInstance() != null && Hudson.getInstance().proxy != null) {
            ProxyConfiguration proxy = Hudson.getInstance().proxy;
            // Configure the proxy if necessary
            if (proxy.name != null && !proxy.name.isEmpty() && proxy.port > 0) {
                if (proxy.getUserName() != null && !proxy.getUserName().isEmpty()) {
                    Credentials cred = new UsernamePasswordCredentials(proxy.getUserName(), proxy.getPassword());
                    httpClient.getCredentialsProvider().setCredentials(new AuthScope(proxy.name, proxy.port), cred);
                }
                HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxyHost);
            }
        }
    }


}
