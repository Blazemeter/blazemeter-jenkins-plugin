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
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.plugins.blazemeter.api.Api;
import hudson.plugins.blazemeter.api.ApiImpl;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSONObject;
import okhttp3.Credentials;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;



@Extension
public class BlazeMeterPerformanceBuilderDescriptor extends BuildStepDescriptor<Builder> {

    private String blazeMeterURL=Constants.A_BLAZEMETER_COM;
    private String name = "My BlazeMeter Account";
    private static BlazeMeterPerformanceBuilderDescriptor descriptor;

    public BlazeMeterPerformanceBuilderDescriptor() {
        super(PerformanceBuilder.class);
        this.load();
        BlazeMeterPerformanceBuilderDescriptor.descriptor = this;
    }

    public BlazeMeterPerformanceBuilderDescriptor(String blazeMeterURL) {
        super(PerformanceBuilder.class);
        this.load();
        this.blazeMeterURL=blazeMeterURL;
        BlazeMeterPerformanceBuilderDescriptor.descriptor = this;
    }

    public static BlazeMeterPerformanceBuilderDescriptor getDescriptor() {
        return BlazeMeterPerformanceBuilderDescriptor.descriptor;
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "BlazeMeter";
    }

    public ListBoxModel doFillTestIdItems(@QueryParameter("credentialsId")String crid,
        @QueryParameter("testId") String savedTestId) throws FormValidation {
        ListBoxModel items = new ListBoxModel();
        List<BlazemeterCredentials> creds = this.getCredentials(CredentialsScope.GLOBAL);
        BlazemeterCredentials credential = null;
        if (StringUtils.isBlank(crid)) {
            if (creds.size() > 0) {
                crid = creds.get(0).getId();
            } else {
                items.add(Constants.NO_CREDENTIALS, "-1");
                return items;
            }
        }
        for (BlazemeterCredentials c : creds) {
            if (c.getId().equals(crid)) {
                credential = c;
            }
        }
        for (BlazemeterCredentials c : creds) {
            if (c.getId().equals(Utils.calcLegacyId(crid))) {
                credential = c;
            }
        }

        Api api=null;
        if (credential instanceof BlazemeterCredentialsBAImpl) {
            String bc = null;
            String username = ((BlazemeterCredentialsBAImpl) credential).getUsername();
            String password = ((BlazemeterCredentialsBAImpl) credential).getPassword().getPlainText();
            bc = Credentials.basic(username, password);
            api = new ApiImpl(bc, this.blazeMeterURL, false);
        }
        if (credential instanceof BlazemeterCredentialImpl) {
            String apiKey = ((BlazemeterCredentialImpl) credential).getApiKey();
            api = new ApiImpl(apiKey, this.blazeMeterURL, true);
        }
        if (credential == null){
            items.add(Constants.NO_SUCH_CREDENTIALS, "-1");
            return items;
        }
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
                    String testId = (String) me.getValue();
                    items.add(new ListBoxModel.Option(testId, testId, testId.contains(savedTestId)));
                }
            }
        } catch (Exception e) {
            throw FormValidation.error(e.getMessage(), e);
        }
        return items;
    }

    public ListBoxModel doFillCredentialsIdItems(@QueryParameter("credentialsId") String credentialsId) {
        ListBoxModel items = new ListBoxModel();
        try {

            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            for (BlazemeterCredentials c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialsBAImpl.class, item, ACL.SYSTEM)) {
                items.add(new ListBoxModel.Option(c.getDescription(),
                    c.getId(),
                    false));
            }
            for (BlazemeterCredentials c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
                items.add(new ListBoxModel.Option(c.getDescription()+Constants.LEGACY,
                    c.getId(),
                    false));
            }
            Iterator<ListBoxModel.Option> iterator = items.iterator();
            while (iterator.hasNext()) {
                ListBoxModel.Option option = iterator.next();
                try {
                    option.selected = StringUtils.isBlank(credentialsId) || credentialsId.equals(option.value);
                    if (!option.selected) {
                        String lid = Utils.calcLegacyId(credentialsId);
                        option.selected = lid.equals(option.value);
                    }
                } catch (Exception e) {
                    option.selected = false;
                }
            }
        } catch (NullPointerException npe) {

        } finally {
            return items;
        }
    }

    public List<BlazemeterCredentials> getCredentials(Object scope) {
        List<BlazemeterCredentials> result = new ArrayList<BlazemeterCredentials>();
        Set<String> addedCredentials = new HashSet<String>();

        Item item = scope instanceof Item ? (Item) scope : null;
        StringBuilder id = new StringBuilder();
        for (BlazemeterCredentialsBAImpl c : CredentialsProvider
            .lookupCredentials(BlazemeterCredentialsBAImpl.class, item, ACL.SYSTEM)) {
            id.append(c.getId());
            result.add(c);
            addedCredentials.add(id.toString());
            id.setLength(0);
        }
        for (BlazemeterCredentialImpl c : CredentialsProvider
            .lookupCredentials(BlazemeterCredentialImpl.class, item, ACL.SYSTEM)) {
            id.append(c.getId());
            result.add(c);
            addedCredentials.add(id.toString());
            id.setLength(0);
        }
        return result;
    }

    // Used by global.jelly to authenticate User key


    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
        String blazeMeterURL = formData.optString("blazeMeterURL");
        this.blazeMeterURL=blazeMeterURL.isEmpty()?Constants.A_BLAZEMETER_COM:blazeMeterURL;
        this.save();
        return true;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlazeMeterURL() {
        return this.blazeMeterURL;
    }

    public void setBlazeMeterURL(String blazeMeterURL) {
        this.blazeMeterURL = blazeMeterURL;
    }

}

