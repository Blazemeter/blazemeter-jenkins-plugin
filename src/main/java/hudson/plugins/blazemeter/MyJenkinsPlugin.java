package hudson.plugins.blazemeter;

import hudson.Extension;
import hudson.model.RootAction;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Extension
public class MyJenkinsPlugin implements RootAction {
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    // This is the URL path for your endpoint
    public String getUrlName() {
        return "my-plugin";
    }

    // This method handles the GET request to your endpoint
    public void doIndex(StaplerRequest req, StaplerResponse resp) throws IOException {
        try {

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("1", "data1");
            jsonObject.put("2", "data2");
            jsonObject.put("3", "data3");
            jsonObject.put("4", "data4");
            jsonObject.put("5", "data5");


            // Send a simple response
            resp.setContentType("application/json");
            resp.getWriter().println(jsonObject);
        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }
}