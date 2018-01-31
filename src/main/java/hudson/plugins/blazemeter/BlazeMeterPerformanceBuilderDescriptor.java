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
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JenkinsTestListFlow;
import hudson.plugins.blazemeter.utils.Utils;
import hudson.plugins.blazemeter.utils.logger.BzmServerLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmServerNotifier;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

@Symbol({"blazeMeterTest"})
@Extension
public class BlazeMeterPerformanceBuilderDescriptor extends BuildStepDescriptor<Builder> {

    private String CHECK_CREDENTIALS_PROXY_TESTS = "Check credentials, proxy settings, tests in workspace";
    private String CHECK_CREDENTIALS_PROXY_WORKSPACES = "Check credentials, proxy settings, workspaces in account";
    private String INVALID_CREDENTIALS_ID = "INVALID CREDENTIALS ID";
    private String INVALID_WORKSPACE_ID = "INVALID WORKSPACE ID";
    private String INVALID_TEST_ID = "INVALID TEST ID";
    private String blazeMeterURL = Constants.A_BLAZEMETER_COM;
    private String NOT_DEFINED = "not defined";
    private String name = "My BlazeMeter Account";
    private static BlazeMeterPerformanceBuilderDescriptor descriptor;

    public static String NO_TESTS = "no-tests";

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

        String resolvedTestId = Utils.resolveTestId(savedTestId);
        ListBoxModel items = new ListBoxModel();
        if (StringUtils.isBlank(crid)) {
            items.add(Constants.NO_CREDENTIALS, "");
            return items;
        }
        try {
            BlazemeterCredentialsBAImpl credentials = findCredentials(crid);
            BlazeMeterUtils utils = getBzmUtils(credentials.getUsername(), credentials.getPassword().getPlainText());
            Workspace workspace = new Workspace(utils, wsid, NOT_DEFINED);
            items = testsList(workspace, resolvedTestId);
        } catch (Exception e) {
            items.clear();
            items.add(new ListBoxModel.Option(CHECK_CREDENTIALS_PROXY_TESTS, CHECK_CREDENTIALS_PROXY_TESTS, true));
        } finally {
            return items;
        }
    }

    public ListBoxModel doFillWorkspaceIdItems(@QueryParameter("credentialsId") String crid,
                                               @QueryParameter("workspaceId") String swid) throws FormValidation {
        ListBoxModel items = new ListBoxModel();
        if (StringUtils.isBlank(crid)) {
            items.add(new ListBoxModel.Option(Constants.NO_CREDENTIALS, Constants.NO_CREDENTIALS, true));
            return items;
        }
        try {
            BlazemeterCredentialsBAImpl credentials = findCredentials(crid);
            BlazeMeterUtils utils = getBzmUtils(credentials.getUsername(), credentials.getPassword().getPlainText());
            items = workspacesList(utils, swid);
        } catch (Exception e) {
            items.clear();
            items.add(new ListBoxModel.Option(CHECK_CREDENTIALS_PROXY_WORKSPACES, CHECK_CREDENTIALS_PROXY_WORKSPACES, true));
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
                        return items;
                    }
                    if (credentialsId.equals(option.value)) {
                        option.selected = true;
                        return items;
                    }
                } catch (Exception e) {
                    option.selected = false;
                }
            }
            items.add(0, new ListBoxModel.Option(INVALID_CREDENTIALS_ID, INVALID_CREDENTIALS_ID, true));
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

    public BlazemeterCredentialsBAImpl findCredentials(String credentialsId) {
        BlazemeterCredentialsBAImpl foundCredentials = null;
        for (BlazemeterCredentialsBAImpl c : CredentialsProvider
                .lookupCredentials(BlazemeterCredentialsBAImpl.class, Jenkins.getInstance(), ACL.SYSTEM)) {
            if (c.getId().equals(credentialsId)) {
                foundCredentials = c;
                break;
            }
        }
        return foundCredentials;
    }


    public static JenkinsBlazeMeterUtils getBzmUtils(String username, String password) throws Exception {
        UserNotifier serverUserNotifier = new BzmServerNotifier();
        Logger logger = new BzmServerLogger();
        ProxyConfigurator.updateProxySettings(true);
        JenkinsBlazeMeterUtils utils = new JenkinsBlazeMeterUtils(username, password,
                BlazeMeterPerformanceBuilderDescriptor.descriptor.blazeMeterURL, serverUserNotifier, logger);

        return utils;
    }


    private ListBoxModel testsList(Workspace workspace, String savedTest) throws Exception {
        ListBoxModel sortedTests = new ListBoxModel();
        JenkinsTestListFlow jenkinsTestListFlow = new JenkinsTestListFlow(workspace.getUtils());
        List<AbstractTest> tests = jenkinsTestListFlow.getAllTestsForWorkspace(workspace);
        Comparator<AbstractTest> c = new AbstractTestComparator();
        if (tests.isEmpty()) {
            sortedTests.add(new ListBoxModel.Option("No tests in workspace", NO_TESTS, true));
            return sortedTests;
        }

        Collections.sort(tests, c);
        for (AbstractTest t : tests) {
            String testName = t.getName() + "(" + t.getId() + "." + t.getTestType() + ")";
            sortedTests.add(new ListBoxModel.Option(testName, t.getId() + "." + t.getTestType(), false));
        }
        setSelected(sortedTests, savedTest, INVALID_TEST_ID);
        return sortedTests;
    }

    private ListBoxModel setSelected(ListBoxModel box, String savedValue, String message) {
        int boxSize = box.size();
        boolean valueWasSelected = false;
        for (int i = 0; i < boxSize; i++) {
            ListBoxModel.Option option = box.get(i);
            if (option.value.contains(savedValue)) {
                box.get(i).selected = true;
                return box;
            }
        }
        if (savedValue.equals(CHECK_CREDENTIALS_PROXY_TESTS) | savedValue.equals(CHECK_CREDENTIALS_PROXY_WORKSPACES)) {
            box.get(0).selected = true;
        }
        if (!valueWasSelected) {
            box.add(0, new ListBoxModel.Option(message, message, true));
        }
        return box;
    }


    private ListBoxModel workspacesList(BlazeMeterUtils utils, String savedWorkspace) throws Exception {
        ListBoxModel workspacesList = new ListBoxModel();
        User user = User.getUser(utils);
        List<Account> accounts = user.getAccounts();
        for (Account a : accounts) {
            List<Workspace> workspaces = a.getWorkspaces();
            for (Workspace ws : workspaces) {
                ListBoxModel.Option wso = new ListBoxModel.Option(ws.getName() +
                        "(" + ws.getId() + ")", ws.getId(), false);
                workspacesList.add(wso);
            }
            if (workspacesList.isEmpty()) {
                workspacesList.add(new ListBoxModel.Option("No workspaces in account", "No workspaces in account", true));
                return workspacesList;
            }

            setSelected(workspacesList, savedWorkspace, INVALID_WORKSPACE_ID);
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

