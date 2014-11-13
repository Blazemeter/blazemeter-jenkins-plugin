package hudson.plugins.blazemeter.aggregatetestresult;

import hudson.plugins.blazemeter.api.APIFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 13/11/14.
 */
public class AggregateTestResultFactory {

    public static AggregateTestResultFactory resultFactory = null;

    private AggregateTestResultV2Impl resultV2=null;
    private AggregateTestResultV3Impl resultV3=null;
    private AggregateTestResult result = null;
    private APIFactory.ApiVersion version =null;

    private AggregateTestResultFactory() {
    }


    public static AggregateTestResultFactory getAggregateTestResultFactory() {
        if (resultFactory == null) {
            resultFactory = new AggregateTestResultFactory();
        }
        return resultFactory;
    }

    public AggregateTestResult getAggregateTestResult(JSONObject json) throws IOException, JSONException{
        if(version==null){
            version= APIFactory.ApiVersion.v3;
        }
        try{
            switch (version) {
                case v2:
                    if (result == null || result instanceof AggregateTestResultV3Impl) {
                        if(resultV2==null){
                            resultV2 = new AggregateTestResultV2Impl(json);
                        }
                        result = resultV2;
                    }
                    break;
                case v3:
                    if (result == null || result instanceof AggregateTestResultV2Impl) {
                        if(resultV3==null){
                            resultV3 = new AggregateTestResultV3Impl(json);
                        }
                        result = resultV3;
                    }
                    break;
            }


        }catch(IOException ioe){
            throw ioe;
        }catch(JSONException je){
            throw je;
        }

        return result;
    }

    public APIFactory.ApiVersion getVersion() {
        return version;
    }

    public void setVersion(APIFactory.ApiVersion version) {
        this.version = version;
    }
}
