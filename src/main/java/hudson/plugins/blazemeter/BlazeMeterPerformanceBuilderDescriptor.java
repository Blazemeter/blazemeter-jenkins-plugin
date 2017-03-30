/**
 Copyright 2016 BlazeMeter Inc.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.json.JSONException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;



@Extension
public class BlazeMeterPerformanceBuilderDescriptor extends BuildStepDescriptor<Builder> {

    private String blazeMeterURL=Constants.A_BLAZEMETER_COM;
    private String name = "My BlazeMeter Account";
    private static BlazeMeterPerformanceBuilderDescriptor descriptor=null;

    public BlazeMeterPerformanceBuilderDescriptor() {
        super(PerformanceBuilder.class);
        load();
        descriptor=this;
    }

    public BlazeMeterPerformanceBuilderDescriptor(String blazeMeterURL) {
        super(PerformanceBuilder.class);
        load();
        this.blazeMeterURL=blazeMeterURL;
        descriptor=this;
    }

    public static BlazeMeterPerformanceBuilderDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "BlazeMeter";
    }


    /*
    TODO
    public ListBoxModel doFillTestIdItems(@QueryParameter("jobApiKey") String apiKey, @QueryParameter("testId") String savedTestId) throws FormValidation {
        ListBoxModel items = new ListBoxModel();
        List<String> keys = getKeys();
        if (!keys.contains(apiKey) || StringUtils.isBlank(apiKey)) {
            if (keys.size() > 0) {
                apiKey = keys.get(0);
            } else {
                items.add(Constants.NO_API_KEY, "-1");
                return items;
            }
        }
        Api api = new ApiV3Impl(apiKey, this.blazeMeterURL);
        try {
            LinkedHashMultimap<String, String> testList = api.testsMultiMap();
            if (testList == null) {
                items.add(Constants.API_KEY_IS_NOT_VALID, "-1");
            } else if (testList.isEmpty()) {
                items.add(Constants.NO_TESTS_FOR_API_KEY, "-1");
            } else {
                Set set = testList.entries();
                for (Object test : set) {
                    Map.Entry me = (Map.Entry) test;
                    String testId = String.valueOf(me.getKey() + "(" + me.getValue() + ")");
                    items.add(new ListBoxModel.Option(testId, testId, testId.contains(savedTestId)));
                }
            }
        } catch (Exception e) {
            throw FormValidation.error(e.getMessage(), e);
        }
        Comparator c = new Comparator<ListBoxModel.Option>() {
            @Override
            public int compare(ListBoxModel.Option o1, ListBoxModel.Option o2) {
                return o1.name.compareToIgnoreCase(o2.name);
            }
        };
        Collections.sort(items, c);
        return items;
    }
*/
    public ListBoxModel doFillJobApiKeyItems(@QueryParameter String jobApiKey) {
        ListBoxModel items = new ListBoxModel();
        try{

        Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
        for (BlazemeterCredentialImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
            items.add(new ListBoxModel.Option(c.getDescription(),
                    c.getUsername(),
                    false));
        }
        Iterator<ListBoxModel.Option> iterator=items.iterator();
        while(iterator.hasNext()){
            ListBoxModel.Option option=iterator.next();
            try{
                option.selected=jobApiKey.substring(0,4).equals(option.value.substring(0,4))?true:false;
            }catch (Exception e){
                option.selected=false;
            }
        }
        }catch (NullPointerException npe){

        }finally {
            return items;
        }
    }

    public List<BlazemeterCredentialImpl> getCredentials(Object scope) {
        List<BlazemeterCredentialImpl> result = new ArrayList<BlazemeterCredentialImpl>();
        Set<String> apiKeys = new HashSet<String>();

        Item item = scope instanceof Item ? (Item) scope : null;
        for (BlazemeterCredentialImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
            String id = c.getId();
            if (!apiKeys.contains(id)) {
                result.add(c);
                apiKeys.add(id);
            }
        }
        return result;
    }

    public boolean credPresent(String username, Object scope) {
        List<BlazemeterCredentialImpl> cred = getCredentials(scope);
        boolean valid = false;
        for (BlazemeterCredentialImpl c : cred) {
            if (c.getUsername().equals(username)) {
                valid = true;
            }
        }
        return valid;
    }


    // Used by global.jelly to authenticate User key
    public FormValidation doTestConnection(@QueryParameter("apiKey") final String userKey)
            throws MessagingException, IOException, JSONException, ServletException {
        return JobUtility.validateUserKey(userKey,this.blazeMeterURL);
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        String blazeMeterURL = formData.optString("blazeMeterURL");
        this.blazeMeterURL=blazeMeterURL.isEmpty()?Constants.A_BLAZEMETER_COM:blazeMeterURL;
        save();
        return true;
    }

    public List<String> getKeys() {
        List<String> apiKeys = new ArrayList<String>();
        try {
            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            for (BlazemeterCredentialImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
                String key = c.getUsername();
                if (!apiKeys.contains(key)) {
                    apiKeys.add(key);
                }
            }
        } catch (Exception e) {
        } finally {
            return apiKeys;
        }
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

