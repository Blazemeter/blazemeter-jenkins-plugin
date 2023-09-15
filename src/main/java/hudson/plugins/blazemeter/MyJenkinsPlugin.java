package hudson.plugins.blazemeter;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.model.RootAction;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import static hudson.plugins.blazemeter.utils.Utils.getCredentials;

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
    private List<BlazemeterCredentialsBAImpl> getCredentials () {
        return CredentialsProvider.lookupCredentials(
                BlazemeterCredentialsBAImpl.class,
                Jenkins.getInstance(),
                ACL.SYSTEM,
                Collections.<DomainRequirement>emptyList());
    }
    private List<Workspace> getWorkspaces (BlazeMeterUtils utils) throws IOException {
        final List<Workspace> workspaces = new ArrayList<>();

        User user = User.getUser(utils);
        List<Account> accounts = user.getAccounts();

        for (Account acc : accounts) {
            try {
                workspaces.addAll(acc.getWorkspaces());
            } catch (Exception ex) {
                ex.printStackTrace();
                //LOGGER.log(Level.WARNING, "Cannot get workspaces for account=" + acc.getId(), ex);
            }
        }

        return workspaces;
    }

    public void doIndex(StaplerRequest req, StaplerResponse resp) throws IOException {
        try {

//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("1", "data1");
//            jsonObject.put("2", "data2");
//            jsonObject.put("3", "data3");
//            jsonObject.put("4", "data4");
//            jsonObject.put("5", "data5");
            // Send a simple response
//            resp.setContentType("application/json");
//            resp.getWriter().println(jsonObject);

            String searchTestId = req.getParameter("searchTestId");
            List<BlazemeterCredentialsBAImpl> creds = getCredentials();
            BlazeMeterUtils utils = BlazeMeterPerformanceBuilderDescriptor.getBzmUtils(creds.get(0).getUsername(), creds.get(0).getPassword().getPlainText());
            //Workspace workspace = new Workspace(utils, "838258", "name");
          //  Workspace workspace;
            //List<Workspace> workspaces = getWorkspaces(utils);
           // workspace = workspaces.get(0);
            //MultiTest singleTest =  workspace.getTestDetails(searchTestId); //getAllTests("10", "name", paramName);
            SingleTest singleTest =  SingleTest.getSingleTest(utils,searchTestId);
            JSONObject jsonObject = new JSONObject();
//            for (MultiTest singleTest: singleTests){
                jsonObject.put(singleTest.getId(), singleTest.getName() + "("+singleTest.getId()+"."+singleTest.getTestType()+")");
//            }
            resp.setContentType("application/json");
            resp.getWriter().println(jsonObject);

        } catch (Exception e) {
            e.printStackTrace(resp.getWriter());
        }
    }
}