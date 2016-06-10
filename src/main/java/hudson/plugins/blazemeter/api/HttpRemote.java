package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;
import hudson.remoting.Callable;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.Serializable;
import java.util.HashMap;


/**
 * Created by zmicer on 9.6.16.
 */
public class HttpRemote implements Callable<String,Exception>, Serializable {
    private CloseableHttpClient httpClient=null;
    private HttpRequestBase r = null;
    private HashMap<String,String> hs = null;
    private HashMap<String,Object> rp = null;
    private static final long serialVersionUID = 1L;
    public HttpRemote(HashMap <String,String> headers) {
        this.hs=headers;
    }

    @Override
    public String call() throws Exception {
        this.httpClient = HttpClients.createDefault();
        HttpRequestBase r=null;
        String url=(String) rp.get(Constants.URL);
        Method m=Method.valueOf((String)rp.get(Constants.METHOD));
        String data = (String)rp.get(Constants.DATA);
        switch (m) {
            case GET:
                r = new HttpGet(url);
                break;
            case POST:
                r = new HttpPost(url);
                if (data != null) {
                    ((HttpPost) r).setEntity(new StringEntity(data.toString()));
                }
                break;
            case PUT:
                r = new HttpPut(url);
                if (data != null) {
                    ((HttpPut) r).setEntity(new StringEntity(data.toString()));
                }
                break;
            case PATCH:
                r = new HttpPatch(url);
                if (data != null) {
                    ((HttpPatch) r).setEntity(new StringEntity(data.toString()));
                }
                break;
            default:
                throw new RuntimeException("Unsupported method: " + m.toString());
        }
        for(String s:hs.keySet()){
            r.setHeader(s,hs.get(s));
        }
        HttpResponse resp=this.httpClient.execute(r);
        return EntityUtils.toString(resp.getEntity());
    }

    public void setRp(HashMap<String, Object> rp) {
        this.rp = rp;
    }
}
