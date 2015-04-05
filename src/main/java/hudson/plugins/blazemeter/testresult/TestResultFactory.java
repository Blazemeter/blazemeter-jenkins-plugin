package hudson.plugins.blazemeter.testresult;

import hudson.plugins.blazemeter.api.APIFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by dzmitrykashlach on 13/11/14.
 */
public class TestResultFactory {

    private TestResultFactory() {
    }

    public static TestResult getTestResult(JSONObject json,APIFactory.ApiVersion version) throws IOException, JSONException{
        if(version==null){
            version= APIFactory.ApiVersion.v3;
        }
        TestResult result=null;
        try{
            switch (version) {
                case v2:
                    result=new TestResultV2Impl(json);
                    break;
                case v3:
                    result=new TestResultV3Impl(json);
                    break;
            }


        }catch(IOException ioe){
            throw ioe;
        }catch(JSONException je){
            throw je;
        }

        return result;
    }
}
