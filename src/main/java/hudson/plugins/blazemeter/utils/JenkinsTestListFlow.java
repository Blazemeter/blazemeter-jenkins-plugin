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
package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.TestsListFlow;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class JenkinsTestListFlow extends TestsListFlow {

    private String limit;

    public JenkinsTestListFlow(BlazeMeterUtils utils, String limit) {
        super(utils);
        this.limit = (!StringUtils.isBlank(limit)&StringUtils.isNumeric(limit)) ? limit : "10000";
    }

    public List<AbstractTest> getAllTestsForWorkspaceWithException(Workspace workspace) throws Exception {
        List<AbstractTest> tests = new ArrayList<>();
        tests.addAll(workspace.getSingleTests(limit, "name"));
        tests.addAll(workspace.getMultiTests(limit, "name"));
        return tests;
    }


    public List<Workspace> getWorkspacesForUser(User user) {
        List<Account> accounts = null;
        try {
            accounts = user.getAccounts();
        } catch (Exception e) {
            getUtils().getNotifier().notifyError("Failed to get accounts for user with id = " + user.getId() + ". Reason is: " + e.getMessage());
        }
        List<Workspace> workspaces = new ArrayList<>();
        for (Account account : accounts) {
            try {
                workspaces.addAll(account.getWorkspaces());
            } catch (Exception e) {
                getUtils().getNotifier().notifyError("Failed to get workspaces for account with id = " + account.getId() + ". Reason is: " + e.getMessage());
            }
        }
        getUtils().getNotifier().notifyInfo("Got " + workspaces.size() + " workspaces from server.");
        return workspaces;
    }

}
