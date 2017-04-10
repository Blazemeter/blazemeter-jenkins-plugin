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
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.google.common.collect.LinkedHashMultimap;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JobUtility;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
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

    public ListBoxModel doFillTestIdItems(@QueryParameter("credentialsId") String credentialsId, @QueryParameter("testId") String savedTestId) throws FormValidation {
        ListBoxModel items = new ListBoxModel();
        BlazemeterCredentialImpl credential = BlazemeterCredentialImpl.EMPTY;
        List<BlazemeterCredentialImpl> creds = getCredentials(CredentialsScope.GLOBAL);
        for (BlazemeterCredentialImpl c : creds) {
            if (c.getId().equals(credentialsId)) {
                credential = c;
            }
        }
        if (credential.equals(BlazemeterCredentialImpl.EMPTY)) {
            items.add(Constants.NO_CREDENTIALS, "-1");
            return items;

        }

        Api api = new ApiImpl(credential, this.blazeMeterURL);
        try {
            LinkedHashMultimap<String, String> testList = api.testsMultiMap();
            if (testList == null) {
                items.add(Constants.CRED_ARE_NOT_VALID, "-1");
            } else if (testList.isEmpty()) {
                items.add(Constants.NO_TESTS_FOR_CREDENTIALS, "-1");
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

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter String credentialsId) {
        ListBoxModel items = new ListBoxModel();
        try {

            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            for (BlazemeterCredentialImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
                items.add(new ListBoxModel.Option(c.getDescription(),
                    c.getId(),
                    false));
            }
            Iterator<ListBoxModel.Option> iterator = items.iterator();
            while (iterator.hasNext()) {
                ListBoxModel.Option option = iterator.next();
                try {
                    option.selected = credentialsId.equals(option.value) ? true : false;
                } catch (Exception e) {
                    option.selected = false;
                }
            }
        } catch (NullPointerException npe) {

        } finally {
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

    // Used by global.jelly to authenticate User key

    public FormValidation doTestConnection(@QueryParameter("id") final String credentialsId)
            throws MessagingException, IOException, JSONException, ServletException {
        BlazemeterCredentialImpl credential = Utils.findCredentials(credentialsId,CredentialsScope.GLOBAL);
        return JobUtility.validateCredentials(credential,this.blazeMeterURL);
    }
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        String blazeMeterURL = formData.optString("blazeMeterURL");
        this.blazeMeterURL=blazeMeterURL.isEmpty()?Constants.A_BLAZEMETER_COM:blazeMeterURL;
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

