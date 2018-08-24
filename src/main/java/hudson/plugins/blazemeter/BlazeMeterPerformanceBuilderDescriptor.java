/**
 * Copyright 2018 BlazeMeter Inc.
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
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.plugins.blazemeter.utils.JenkinsBlazeMeterUtils;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.JenkinsTestListFlow;
import hudson.plugins.blazemeter.utils.logger.BzmServerLogger;
import hudson.plugins.blazemeter.utils.notifier.BzmServerNotifier;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

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
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(BlazeMeterPerformanceBuilderDescriptor.class.getName());

    private String NO_CREDENTIALS = "No Credentials";
    private String NO_WORKSPACE = "No Workspace";
    private String SELECT_WORKSPACE = "Select workspace";
    private String SELECT_TEST = "Select test";
    private String NO_TESTS_IN_WORKSPACE = "No tests in workspace";
    private String NO_WORKSPACES_IN_ACCOUNT = "No workspaces in account";

    private String CHECK_CREDENTIALS_PROXY = "Check credentials, proxy settings";
    private String blazeMeterURL = Constants.A_BLAZEMETER_COM;
    private boolean isUnstableIfNotStarted = false;
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

    public BlazeMeterPerformanceBuilderDescriptor(String blazeMeterURL, boolean isUnstableIfNotStarted) {
        super(PerformanceBuilder.class);
        this.load();
        this.blazeMeterURL = blazeMeterURL;
        this.isUnstableIfNotStarted = isUnstableIfNotStarted;
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

    public FormValidation doCheckMainTestFile(@QueryParameter String value) {
        if (StringUtils.isEmpty(value) || value.endsWith(".jmx") || value.endsWith(".yml") || value.endsWith(".yaml")) {
            return FormValidation.ok();
        } else {
            return FormValidation.warning("Unknown script type. Please, select 'Test type' in BlazeMeter web application");
        }
    }

    public ListBoxModel doFillTestIdItems(@QueryParameter("credentialsId") String credentialsId,
                                          @QueryParameter("workspaceId") String workspaceId,
                                          @QueryParameter("testId") String testId) throws FormValidation {

        ListBoxModel items = new ListBoxModel();

        try {
            List<BlazemeterCredentialsBAImpl> creds = getCredentials();
            BlazemeterCredentialsBAImpl credentials = (StringUtils.isBlank(credentialsId) && !creds.isEmpty()) ?
                    creds.get(0) :
                    findCredentials(creds, credentialsId);
            if (credentials != null) {
                BlazeMeterUtils utils = getBzmUtils(credentials.getUsername(), credentials.getPassword().getPlainText());

                Workspace workspace;
                if (StringUtils.isBlank(workspaceId)) {
                    List<Workspace> workspaces = getWorkspaces(utils);
                    if (workspaces.isEmpty()) {
                        items.add(new ListBoxModel.Option(NO_WORKSPACE, testId, true));
                        return items;
                    }
                    workspace = workspaces.get(0);
                } else {
                    workspace = getWorkspace(utils, workspaceId);
                }

                if (workspace != null) {
                    items = testsList(workspace, testId);
                } else {
                    items.add(new ListBoxModel.Option(NO_WORKSPACE, testId, true));
                }
            } else {
                items.add(new ListBoxModel.Option(NO_CREDENTIALS, testId, true));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot do fill tests ", ex);
            items.clear();
            items.add(new ListBoxModel.Option(CHECK_CREDENTIALS_PROXY, testId, true));
        }

        return items;
    }

    private Workspace getWorkspace(BlazeMeterUtils utils, String workspaceId) throws Exception {
        try {
            return Workspace.getWorkspace(utils, workspaceId);
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot get workspace with id=" + workspaceId, ex);
            if (ex.getMessage().toLowerCase().contains("not found")) {
                return null;
            } else {
                throw ex;
            }
        }
    }

    private ListBoxModel testsList(Workspace workspace, String testId) throws Exception {
        ListBoxModel sortedTests = new ListBoxModel();

        JenkinsTestListFlow jenkinsTestListFlow = new JenkinsTestListFlow(workspace.getUtils(), System.getProperty("bzm.limit", "10000"));

        List<AbstractTest> tests = jenkinsTestListFlow.getAllTestsForWorkspaceWithException(workspace);
        Comparator<AbstractTest> c = new AbstractTestComparator();
        if (tests.isEmpty()) {
            sortedTests.add(new ListBoxModel.Option(NO_TESTS_IN_WORKSPACE, testId, true));
            return sortedTests;
        }

        Collections.sort(tests, c);
        for (AbstractTest t : tests) {
            String testName = t.getName() + "(" + t.getId() + "." + t.getTestType() + ")";
            sortedTests.add(new ListBoxModel.Option(testName, t.getId() + "." + t.getTestType(), false));
        }

        if (StringUtils.isBlank(testId)) {
            sortedTests.get(0).selected = true;
        }

        for (ListBoxModel.Option test : sortedTests) {
            if (test.value.contains(testId)) {
                test.selected = true;
                return sortedTests;
            }
        }

        sortedTests.add(0, new ListBoxModel.Option(SELECT_TEST, testId, true));
        return sortedTests;
    }

    private List<Workspace> getWorkspaces(BlazeMeterUtils utils) throws IOException {
        final List<Workspace> workspaces = new ArrayList<>();

        User user = User.getUser(utils);
        List<Account> accounts = user.getAccounts();

        for (Account acc : accounts) {
            try {
                workspaces.addAll(acc.getWorkspaces());
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Cannot get workspaces for account=" + acc.getId(), ex);
            }
        }

        return workspaces;
    }

    public ListBoxModel doFillWorkspaceIdItems(@QueryParameter("credentialsId") String credentialsId,
                                               @QueryParameter("workspaceId") String workspaceId) throws FormValidation {
        ListBoxModel items = new ListBoxModel();

        try {
            List<BlazemeterCredentialsBAImpl> creds = getCredentials();
            BlazemeterCredentialsBAImpl credentials = (StringUtils.isBlank(credentialsId) && !creds.isEmpty()) ?
                    creds.get(0) :
                    findCredentials(creds, credentialsId);

            if (credentials != null) {
                BlazeMeterUtils utils = getBzmUtils(credentials.getUsername(), credentials.getPassword().getPlainText());
                items = workspacesList(utils, workspaceId);
            } else {
                items.add(new ListBoxModel.Option(NO_CREDENTIALS, workspaceId, true));
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot do fill workspaces ", ex);
            items.clear();
            items.add(new ListBoxModel.Option(CHECK_CREDENTIALS_PROXY, workspaceId, true));
        }
        return items;
    }

    private ListBoxModel workspacesList(BlazeMeterUtils utils, String savedWorkspace) throws Exception {
        ListBoxModel workspacesList = new ListBoxModel();

        List<Workspace> workspaces = getWorkspaces(utils);
        for (Workspace ws : workspaces) {
                workspacesList.add(new ListBoxModel.Option(ws.getName() + "(" + ws.getId() + ")", ws.getId(), false));
        }

        if (workspacesList.isEmpty()) {
            workspacesList.add(new ListBoxModel.Option(NO_WORKSPACES_IN_ACCOUNT, savedWorkspace, false));
            return workspacesList;
        }

        if (StringUtils.isBlank(savedWorkspace)) {
            workspacesList.get(0).selected = true;
            return workspacesList;
        }

        for (ListBoxModel.Option wsp : workspacesList) {
            if (wsp.value.contains(savedWorkspace)) {
                wsp.selected = true;
                return workspacesList;
            }
        }

        workspacesList.add(0, new ListBoxModel.Option(SELECT_WORKSPACE, savedWorkspace, true));
        return workspacesList;
    }


    public ListBoxModel doFillCredentialsIdItems(@QueryParameter("credentialsId") String credentialsId) {
        ListBoxModel items = new ListBoxModel();
        try {

            Item item = Stapler.getCurrentRequest().findAncestorObject(Item.class);
            List<BlazemeterCredentialsBAImpl> credentials =
                    CredentialsProvider.lookupCredentials(
                            BlazemeterCredentialsBAImpl.class,
                            item,
                            ACL.SYSTEM,
                            Collections.<DomainRequirement>emptyList());

            for (BlazemeterCredentials c : credentials) {
                items.add(new ListBoxModel.Option(c.getDescription(), c.getId(), false));
            }

            if (StringUtils.isBlank(credentialsId)) {
                items.get(0).selected = true;
                return items;
            }

            for (ListBoxModel.Option option : items) {
                try {
                    if (credentialsId.equals(option.value)) {
                        option.selected = true;
                        return items;
                    }
                } catch (Exception e) {
                    option.selected = false;
                }
            }

            items.add(0, new ListBoxModel.Option("Credentials not found '" + credentialsId + "'", credentialsId, true));
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Cannot do fill credentials ", ex);
        }
        return items;
    }

    // Used by global.jelly to authenticate User key
    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws Descriptor.FormException {
        String blazeMeterURL = formData.optString("blazeMeterURL");
        this.blazeMeterURL = blazeMeterURL.isEmpty() ? Constants.A_BLAZEMETER_COM : blazeMeterURL;
        this.isUnstableIfNotStarted = formData.optBoolean("isUnstableIfNotStarted");
        this.save();
        return true;
    }

    private List<BlazemeterCredentialsBAImpl> getCredentials() {
        return CredentialsProvider.lookupCredentials(
                BlazemeterCredentialsBAImpl.class,
                Jenkins.getInstance(),
                ACL.SYSTEM,
                Collections.<DomainRequirement>emptyList());
    }

    public BlazemeterCredentialsBAImpl findCredentials(List<BlazemeterCredentialsBAImpl> credentials, String credentialsId) {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }

        for (BlazemeterCredentialsBAImpl cred : credentials) {
            if (cred.getId().equals(credentialsId)) {
                return cred;
            }
        }

        return null;
    }


    public static JenkinsBlazeMeterUtils getBzmUtils(String username, String password) throws Exception {
        UserNotifier serverUserNotifier = new BzmServerNotifier();
        Logger logger = new BzmServerLogger();
        ProxyConfigurator.updateProxySettings(ProxyConfiguration.load(), false);
        return new JenkinsBlazeMeterUtils(username, password,
                BlazeMeterPerformanceBuilderDescriptor.descriptor.blazeMeterURL, serverUserNotifier, logger);
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

    public boolean getIsUnstableIfNotStarted() {
        return isUnstableIfNotStarted;
    }

    public void setIsUnstableIfNotStarted(boolean isUnstableIfNotStarted) {
        this.isUnstableIfNotStarted = isUnstableIfNotStarted;
    }
}

