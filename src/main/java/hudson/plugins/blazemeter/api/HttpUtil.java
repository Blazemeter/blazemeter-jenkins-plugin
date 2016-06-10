package hudson.plugins.blazemeter.api;

import hudson.ProxyConfiguration;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import org.apache.http.HttpHost;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class HttpUtil {
    private StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    private transient CloseableHttpClient httpClient = null;
    private HttpHost proxy = null;
    private HttpRemote r = null;
    private VirtualChannel c = null;
    private HashMap<String, String> headers = new HashMap<String, String>();

    public HttpUtil(ProxyConfiguration proxy, VirtualChannel c) {
        this.headers.put("Accept", "application/json");
        this.headers.put("Content-type", "application/json; charset=UTF-8");
        this.r = new HttpRemote(this.headers);
        this.httpClient = HttpClients.createDefault();
        this.logger.setDebugEnabled(false);
        this.c = c;
        try {
            proxy = proxy == null ? ProxyConfiguration.load() : proxy;
        } catch (IOException ie) {
            logger.warn("Failed to load jenkins proxy configuration: ", ie);
            proxy = null;
        } catch (Exception e) {
            proxy = null;
            logger.warn("Failed to load jenkins proxy configuration: ", e);
        }

        if (proxy != null) {
            this.proxy = new HttpHost(proxy.name, proxy.port);
            String proxyUser = proxy.getUserName();
            String proxyPass = proxy.getPassword();
            if (!proxyUser.isEmpty() && !proxyPass.isEmpty()) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(proxy.name, proxy.port),
                        new UsernamePasswordCredentials(proxyUser, proxyPass));
                this.httpClient = HttpClients.custom()
                        .setDefaultCredentialsProvider(credsProvider).build();
            }
        }
    }

    public <V> String responseRC(String url, V data, Method method) throws Exception {
        HashMap<String, Object> rp = new HashMap<String, Object>();
        String entity = null;
        try {
            rp.put(Constants.URL, url);
            rp.put(Constants.METHOD, method.name());
            if (data != null) {
                rp.put(Constants.DATA, data.toString());
            }
            this.r.setRp(rp);
            entity = c.call(this.r);
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Problems with executing http request at remote channel(slave)=" + this.c.toString(), e);
            throw e;
        } finally {
            return entity;
        }
    }

    public <V> HttpResponse responseLC(String url, V data, Method method) throws IOException {
        if (StringUtils.isBlank(url)) return null;
        if (logger.isDebugEnabled())
            logger.debug("Requesting : " + url.substring(0, url.indexOf("?") + 14));
        HttpResponse response = null;
        HttpRequestBase request = null;

        try {
            switch (method) {
                case GET:
                    request = new HttpGet(url);
                    break;
                case POST:
                    request = new HttpPost(url);
                    if (data != null) {
                        ((HttpPost) request).setEntity(new StringEntity(data.toString()));
                    }
                    break;
                case PUT:
                    request = new HttpPut(url);
                    if (data != null) {
                        ((HttpPut) request).setEntity(new StringEntity(data.toString()));
                    }
                    break;
                case PATCH:
                    request = new HttpPatch(url);
                    if (data != null) {
                        ((HttpPatch) request).setEntity(new StringEntity(data.toString()));
                    }
                    break;
                default:
                    throw new RuntimeException("Unsupported method: " + method.toString());
            }
            for (String s : headers.keySet()) {
                request.setHeader(s, headers.get(s));
            }
            if (proxy != null) {
                RequestConfig conf = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();
                request.setConfig(conf);
            }
            response = this.httpClient.execute(request);


            if (response == null || response.getStatusLine() == null) {
                if (logger.isDebugEnabled())
                    logger.debug("Erroneous response (Probably null) for url: \n", url);
                response = null;
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Problems with creating and sending request: \n", e);
        }
        return response;
    }


    public <T, V> T response(String url, V data, Method method, Class<T> returnType, Class<V> dataType) {
        T returnObj = null;
        JSONObject jo = null;
        String output = null;
        HttpResponse response = null;
        try {
            if ((this.c == null) | (this.c instanceof LocalChannel)) {
                response = responseLC(url, data, method);
                if (response != null) {
                    output = EntityUtils.toString(response.getEntity());
                    if (logger.isDebugEnabled())
                        logger.debug("Received object from server: " + output);

                }
            } else {
                output = responseRC(url, data, method);
            }
            if (output.isEmpty()) {
                throw new IOException();
            }
            jo = new JSONObject(output);
        } catch (IOException ioe) {
            if (logger.isDebugEnabled())
                logger.debug("Received empty response from server: ", ioe);
            return null;
        } catch (JSONException e) {
            if (logger.isDebugEnabled())
                logger.debug("ERROR decoding Json: ", e);
            returnType = (Class<T>) String.class;
            return returnType.cast(output);
        } catch (Exception e) {
            if (logger.isDebugEnabled())
                logger.debug("Failed to do request from slave: channell=" + c.toString(), e);

        }
        try {
            returnObj = returnType.cast(jo);

        } catch (ClassCastException cce) {
            if (logger.isDebugEnabled())
                logger.debug("Failed to parse response from server: ", cce);
            throw new RuntimeException(jo.toString());

        }
        return returnObj;
    }

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public StdErrLog getLogger() {
        return logger;
    }

    public void setLogger(StdErrLog logger) {
        this.logger = logger;
    }
}
