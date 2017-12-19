/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hudson.plugins.blazemeter;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.plugins.blazemeter.logging.ServerLogger;
import hudson.plugins.blazemeter.logging.ServerUserNotifier;
import hudson.plugins.blazemeter.utils.BzmUtils;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

@Symbol({"blazeMeterTest"})
@Extension
public class BlazeMeterPerformanceBuilderDescriptor extends BuildStepDescriptor<Builder> {

    private String blazeMeterURL = Constants.A_BLAZEMETER_COM;
    private String NOT_DEFINED = "not defined";
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
        this.blazeMeterURL = blazeMeterURL;
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

    public ListBoxModel doFillTestIdItems(@QueryParameter("credentialsId") String crid,
                                          @QueryParameter("workspaceId") String wsid,
                                          @QueryParameter("testId") String savedTestId) throws FormValidation {

        ListBoxModel items = new ListBoxModel();
        BlazeMeterUtils utils = getBzmUtils(CredentialsScope.GLOBAL, crid);
        if (StringUtils.isBlank(crid)) {
            items.add(Constants.NO_CREDENTIALS, "");
        }
        if (utils == null) {
            items.add(Constants.NO_SUCH_CREDENTIALS, "");
            return items;
        }
        Workspace workspace = new Workspace(utils, wsid, NOT_DEFINED);
        try {
            items = testsList(workspace, savedTestId);
        } catch (Exception e) {
            items.add(Constants.NO_TESTS_FOR_CREDENTIALS, "");
        } finally {
            return items;
        }
    }

    public ListBoxModel doFillWorkspaceIdItems(@QueryParameter("credentialsId") String crid,
                                               @QueryParameter("workspaceId") String swid) throws FormValidation {
        ListBoxModel items = new ListBoxModel();
        BlazeMeterUtils utils = getBzmUtils(CredentialsScope.GLOBAL, crid);
        if (StringUtils.isBlank(crid)) {
            items.add(Constants.NO_CREDENTIALS, "");
        }
        if (utils == null) {
            items.add(Constants.NO_SUCH_CREDENTIALS, "");
            return items;
        }
        try {
            items = workspacesList(utils, swid);
        } catch (Exception e) {
            items.add(Constants.NO_TESTS_FOR_CREDENTIALS, "");
        } finally {
            return items;
        }
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
            Iterator<ListBoxModel.Option> iterator = items.iterator();
            while (iterator.hasNext()) {
                ListBoxModel.Option option = iterator.next();
                try {
                    if (StringUtils.isBlank(credentialsId)) {
                        option.selected = true;
                        break;
                    }
                    if (credentialsId.equals(option.value)) {
                        option.selected = true;
                        break;
                    }
                } catch (Exception e) {
                    option.selected = false;
                }
            }
        } catch (Exception npe) {

        } finally {
            return items;
        }
    }

    // Used by global.jelly to authenticate User key


    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
        String blazeMeterURL = formData.optString("blazeMeterURL");
        this.blazeMeterURL = blazeMeterURL.isEmpty() ? Constants.A_BLAZEMETER_COM : blazeMeterURL;
        this.save();
        return true;
    }

    public BzmUtils getBzmUtils(Object scope, String credentialsId) {
        BzmUtils utils = null;
        Item item = scope instanceof Item ? (Item) scope : null;
        for (BlazemeterCredentialsBAImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialsBAImpl.class, item, ACL.SYSTEM)) {
            if (c.getId().equals(credentialsId)) {
                UserNotifier serverUserNotifier = new ServerUserNotifier();
                Logger logger = new ServerLogger();
                utils = new BzmUtils(c.getUsername(), c.getPassword().getPlainText(),
                        blazeMeterURL, serverUserNotifier, logger);
                try {
                    User.getUser(utils);
                } catch (Exception e) {
                    //TODO
                    //Notify user about invalid credentials in drop-down list
                    logger.error("Failed to find user for provided credentials = " + c.getId(), e);
                }
            }
        }
        return utils;
    }

    private ListBoxModel testsList(Workspace workspace, String savedTest) throws Exception {
        ListBoxModel sortedTests = new ListBoxModel();
        List<AbstractTest> tests = new ArrayList<>();
        tests.addAll(workspace.getMultiTests());
        tests.addAll(workspace.getSingleTests());
        Comparator c = new Comparator<AbstractTest>() {
            @Override
            public int compare(AbstractTest t1, AbstractTest t2) {
                return t1.getName().compareToIgnoreCase(t2.getName());
            }
        };
        tests.sort(c);
        boolean selected = false;
        for (AbstractTest t : tests) {
            String testName = t.getName() + "(" + t.getId() + "." + t.getTestType() + ")";
            sortedTests.add(new ListBoxModel.Option(testName, t.getId()
                    , !selected ? t.getId().contains(savedTest) : false));
            selected = t.getId().contains(savedTest);
        }
        return sortedTests;
    }

    private ListBoxModel workspacesList(BlazeMeterUtils utils, String savedWorkspace) throws Exception {
        ListBoxModel workspacesList = new ListBoxModel();
        User user = User.getUser(utils);
        List<Account> accounts = user.getAccounts();
        for (Account a : accounts) {
            List<Workspace> workspaces = a.getWorkspaces();
            for (Workspace ws : workspaces) {
                ListBoxModel.Option wso = new ListBoxModel.Option(ws.getName() +
                        "(" + ws.getId() + "", ws.getId(), ws.getId().equals(savedWorkspace)
                );
                workspacesList.add(wso);
            }
        }
        return workspacesList;
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

