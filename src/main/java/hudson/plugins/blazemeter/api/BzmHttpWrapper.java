package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class BzmHttpWrapper {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    public enum Method {GET, POST, PUT}

    private transient DefaultHttpClient httpClient = null;

    public BzmHttpWrapper() {
        this.httpClient = new DefaultHttpClient();
        this.logger.setDebugEnabled(false);
    }

    public HttpResponse getHttpResponse(String url, JSONObject data, Method method) throws IOException {
        if (StringUtils.isBlank(url)) return null;
        if (logger.isDebugEnabled())
            logger.debug("Requesting : " + url.substring(0,url.indexOf("?")+14));
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
                if(logger.isDebugEnabled())
                    logger.debug("Erroneous response (Probably null) for url: \n", url);
                response = null;
            }
        } catch (Exception e) {
            if(logger.isDebugEnabled())
                logger.debug("Problems with creating and sending request: \n", e);
        }
        return response;
    }


    public JSONObject getResponseAsJson(String url, JSONObject data, Method method) {
        JSONObject jo = null;
        try {
            HttpResponse response = getHttpResponse(url, data, method);
            if (response != null) {
                String output = EntityUtils.toString(response.getEntity());
                if(logger.isDebugEnabled())
                    logger.debug("Received JSON object: "+output);
                jo = new JSONObject(output);
            }
        } catch (IOException e) {
            if(logger.isDebugEnabled())
                logger.debug("ERROR decoding Json ", e);
        } catch (JSONException e) {
            if(logger.isDebugEnabled())
            logger.debug("ERROR decoding Json ", e);
        }
        return jo;
    }

    public String getResponseAsString(String url, JSONObject data, Method method){
        String  str = null;
        try {
            HttpResponse response = getHttpResponse(url, data, method);
            if (response != null) {
                str = EntityUtils.toString(response.getEntity());
                if(logger.isDebugEnabled())
                    logger.debug(str);
            }
        } catch (IOException e) {
            if(logger.isDebugEnabled())
                logger.debug("ERROR decoding Json ", e);
        }
        return str;
    }

    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(DefaultHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public StdErrLog getLogger() {
        return logger;
    }

    public void setLogger(StdErrLog logger) {
        this.logger = logger;
    }
}
