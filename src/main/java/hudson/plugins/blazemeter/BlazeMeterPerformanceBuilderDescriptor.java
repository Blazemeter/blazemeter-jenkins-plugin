package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.google.common.collect.LinkedHashMultimap;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
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
    public ListBoxModel doFillTestIdItems(@QueryParameter("jobApiKey") String apiKey) throws FormValidation {
        if(apiKey.contains("...")){
            apiKey=BzmServiceManager.selectUserKeyOnId(this,apiKey);
        }
        ListBoxModel items = new ListBoxModel();
        if (apiKey == null) {
            items.add("No API Key", "-1");
        } else {
            APIFactory apiFactory=APIFactory.getApiFactory();
            BlazemeterApi bzm = apiFactory.getAPI(apiKey, APIFactory.ApiVersion.v3);
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
    public ListBoxModel doFillLocationItems(@QueryParameter("jobApiKey") String apiKey) throws FormValidation {
        if(apiKey.contains("...")){
            apiKey=BzmServiceManager.selectUserKeyOnId(this,apiKey);
        }
        ListBoxModel items = new ListBoxModel();
        if (apiKey == null) {
            items.add("No API Key", "-1");
            return items;
        }
        APIFactory apiFactory = APIFactory.getApiFactory();
        BlazemeterApi bzm = apiFactory.getAPI(apiKey, APIFactory.ApiVersion.v3);
        try {
            LinkedHashMap<String, String> locationList = new LinkedHashMap<String, String>();
            items.add(Constants.USE_TEST_LOCATION, Constants.USE_TEST_LOCATION);
            JSONObject jo = JSONObject.fromObject(bzm.getUser().toString());
            if (!jo.has("locations")) {
                items.add("Invalid API key ", "-1");
                return items;
            }
            Iterator<JSONObject> locations = jo.getJSONArray("locations").iterator();
            while (locations.hasNext()) {
                JSONObject location = locations.next();
                locationList.put(location.getString("id"), location.getString("title"));
            }
            Set set = locationList.entrySet();
            for (Object test : set) {
                Map.Entry me = (Map.Entry) test;
                items.add(new ListBoxModel.Option(String.valueOf(me.getValue()), String.valueOf(me.getKey()), false));
            }

        } catch (Exception e) {
            throw FormValidation.error(e.getMessage(), e);
        } finally {
            return items;
        }
    }

    public ListBoxModel doFillJobApiKeyItems(@QueryParameter String jobApiKey) {
        ListBoxModel items = new ListBoxModel();
        Set<String> apiKeys = new HashSet<String>();

        Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
        for (BlazemeterCredential c : CredentialsProvider
                .lookupCredentials(BlazemeterCredential.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                items.add(new ListBoxModel.Option(c.getDescription(),
                        c.getId(),
                        false));
                apiKeys.add(id);
            }
        }
        Iterator<ListBoxModel.Option> iterator=items.iterator();
        while(iterator.hasNext()){
            ListBoxModel.Option option=iterator.next();
            try{
                option.selected=jobApiKey.substring(jobApiKey.length()-4).equals(option.value.substring(option.value.length()-4))?true:false;
            }catch (Exception e){
                option.selected=false;
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
        return BzmServiceManager.validateUserKey(userKey);
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

    public String getBlazeMeterURL() {
        return blazeMeterURL;
    }

    public void setBlazeMeterURL(String blazeMeterURL) {
        this.blazeMeterURL = blazeMeterURL;
    }

}

