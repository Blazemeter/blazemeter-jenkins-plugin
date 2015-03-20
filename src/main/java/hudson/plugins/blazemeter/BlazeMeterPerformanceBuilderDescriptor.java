package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.LinkedHashMultimap;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

public class BlazeMeterPerformanceBuilderDescriptor extends BuildStepDescriptor<Builder> {

    private String blazeMeterURL;
    private String name = "My BlazeMeter Account";
    private String apiKey;
    private String testId;



    public BlazeMeterPerformanceBuilderDescriptor() {
        super(PerformanceBuilder.class);
        load();
        APIFactory.getApiFactory().setBlazeMeterUrl(blazeMeterURL!=null&&!blazeMeterURL.isEmpty()?blazeMeterURL:
                Constants.DEFAULT_BLAZEMETER_URL);

    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "BlazeMeter";
    }

    // Used by config.jelly to display the test list.
    public ListBoxModel doFillTestIdItems(@QueryParameter String apiKey) throws FormValidation {
        if (StringUtils.isBlank(apiKey)) {
            apiKey = getApiKey();
        }
        this.apiKey=apiKey;
        String apiSecret = null;
        Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
            if (StringUtils.equals(apiKey, c.getId())) {
                apiSecret = c.getApiKey();
                break;
            }
        }
        ListBoxModel items = new ListBoxModel();
        if (apiSecret == null) {
            items.add("No API Key", "-1");
        } else {
            APIFactory apiFactory=APIFactory.getApiFactory();
            BlazemeterApi bzm = apiFactory.getAPI(apiSecret, APIFactory.ApiVersion.v3);
            try {
                LinkedHashMultimap<String, String> testList = bzm.getTestList();
                items.add(Constants.CREATE_BZM_TEST, Constants.CREATE_BZM_TEST_NOTE);

                if (testList == null){
                    items.add("Invalid API key ", "-1");
                } else if (testList.isEmpty()){
                    items.add("No tests", "-1");
                } else {
                    Set set = testList.entries();
                    for (Object test : set) {
                        Map.Entry me = (Map.Entry) test;
                        items.add(new ListBoxModel.Option(String.valueOf(me.getValue())+"->"+me.getKey(), String.valueOf(me.getValue())));
                    }
                }
            } catch (Exception e) {
                throw FormValidation.error(e.getMessage(), e);
            }
        }
        return items;
    }

    // Used by config.jelly to display the test list.
    public ListBoxModel doFillLocationItems(@QueryParameter String apiKey) throws FormValidation {
        if (StringUtils.isBlank(apiKey)) {
            apiKey = getApiKey();
        }
        this.apiKey=apiKey;
        String apiSecret = null;
        Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
            if (StringUtils.equals(apiKey, c.getId())) {
                apiSecret = c.getApiKey();
                break;
            }
        }
        ListBoxModel items = new ListBoxModel();
        if (apiSecret == null) {
            items.add("No API Key", "-1");
        } else {
            APIFactory apiFactory=APIFactory.getApiFactory();
            BlazemeterApi bzm = apiFactory.getAPI(apiSecret, APIFactory.ApiVersion.v3);
            try{
                LinkedHashMap<String, String> locationList = new LinkedHashMap<String,String>();
                items.add(Constants.USE_TEST_LOCATION, Constants.USE_TEST_LOCATION);
                JSONObject jo = JSONObject.fromObject(bzm.getUser().toString());
                Iterator<JSONObject> locations=jo.getJSONArray("locations").iterator();
                while(locations.hasNext()){
                    JSONObject location=locations.next();
                    locationList.put(location.getString("id"),location.getString("title"));
                }
                Set set = locationList.entrySet();
                for (Object test : set) {
                    Map.Entry me = (Map.Entry) test;
                    items.add(new ListBoxModel.Option(String.valueOf(me.getValue()),String.valueOf(me.getKey()),false));
                }

            } catch (Exception e) {
                throw FormValidation.error(e.getMessage(), e);
            }
        }
        return items;
    }



    public ListBoxModel doFillApiKeyItems() {
        ListBoxModel items = new ListBoxModel();
        Set<String> apiKeys = new HashSet<String>();

        Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                items.add(new ListBoxModel.Option(c.getDescription(),
                        c.getId(),
                        c.getId().contains(apiKey)));
                apiKeys.add(id);
            }
        }
        return items;
    }

    public List<BlazemeterCredential> getCredentials(Object scope) {
        List<BlazemeterCredential> result = new ArrayList<BlazemeterCredential>();
        Set<String> apiKeys = new HashSet<String>();

        Item item = scope instanceof Item ? (Item) scope : null;
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                result.add(c);
                apiKeys.add(id);
            }
        }
        return result;
    }

    // Used by global.jelly to authenticate User key
    public FormValidation doTestConnection(@QueryParameter("apiKey") final String userKey)
            throws MessagingException, IOException, JSONException, ServletException {
        BlazemeterApi bzm = APIFactory.getApiFactory().getAPI(apiKey, APIFactory.ApiVersion.v3);
        int testCount = bzm.getTestCount();
        if (testCount < 0) {
            return FormValidation.errorWithMarkup("An error as occurred, check proxy settings");
        } else if (testCount == 0) {
            return FormValidation.errorWithMarkup("User Key Invalid Or No Available Tests");
        } else {
            return FormValidation.ok("User Key Valid. " + testCount + " Available Tests");
        }
    }

    public FormValidation doCheckTestDuration(@QueryParameter String value) throws IOException, ServletException {
        if(value.equals("0")) {
            return FormValidation.warning("TestDuration should be more than ZERO");
        }if(value.equals("")) {
            return FormValidation.warning("Default value will be fetched from server");
        }
        return FormValidation.ok();
    }


    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        apiKey = formData.optString("apiKey");
        blazeMeterURL = formData.optString("blazeMeterURL");
        APIFactory.getApiFactory().setBlazeMeterUrl(!blazeMeterURL.isEmpty()?blazeMeterURL:
                Constants.DEFAULT_BLAZEMETER_URL);
        save();
        return true;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiKey() {
        List<BlazemeterCredential> credentials = CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, Jenkins.getInstance(), ACL.SYSTEM);
        if (StringUtils.isBlank(apiKey) && !credentials.isEmpty()) {
            return credentials.get(0).getId();
        }
        if (credentials.size() == 1) {
            return credentials.get(0).getId();
        }
        for (BlazemeterCredential c: credentials) {
            if (StringUtils.equals(c.getId(), apiKey)) {
                return apiKey;
            }
        }
        // API key is not valid any more
        return "";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBlazeMeterURL() {
        return blazeMeterURL;
    }

    public void setBlazeMeterURL(String blazeMeterURL) {
        this.blazeMeterURL = blazeMeterURL;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }
}

