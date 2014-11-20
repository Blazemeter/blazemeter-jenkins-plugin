package hudson.plugins.blazemeter.testresult;

import hudson.plugins.blazemeter.api.APIFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 13/11/14.
 */
public class TestResultFactory {

    public static TestResultFactory resultFactory = null;

    private TestResultV2Impl resultV2=null;
    private TestResultV3Impl resultV3=null;
    private TestResult result = null;
    private APIFactory.ApiVersion version =null;

    private TestResultFactory() {
    }


    public static TestResultFactory getTestResultFactory() {
        if (resultFactory == null) {
            resultFactory = new TestResultFactory();
        }
        return resultFactory;
    }

    public TestResult getTestResult(JSONObject json) throws IOException, JSONException{
        if(version==null){
            version= APIFactory.ApiVersion.v3;
        }
        try{
            switch (version) {
                case v2:
                    if (result == null || result instanceof TestResultV3Impl) {
                        if(resultV2==null){
                            resultV2 = new TestResultV2Impl(json);
                        }
                        result = resultV2;
                    }
                    break;
                case v3:
                    if (result == null || result instanceof TestResultV2Impl) {
                        if(resultV3==null){
                            resultV3 = new TestResultV3Impl(json);
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
