package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Account;
import com.blazemeter.api.explorer.User;
import com.blazemeter.api.explorer.Workspace;
import com.blazemeter.api.explorer.test.AbstractTest;
import com.blazemeter.api.explorer.test.MultiTest;
import com.blazemeter.api.explorer.test.SingleTest;
import com.blazemeter.api.utils.BlazeMeterUtils;
import com.blazemeter.ciworkflow.TestsListFlow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JenkinsTestListFlow extends TestsListFlow {

    public JenkinsTestListFlow(BlazeMeterUtils utils) {
        super(utils);
    }

    private List<SingleTest> getSingleTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getSingleTests();
        } catch (IOException e) {
            getUtils().getNotifier().notifyError("Failed to get single tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<MultiTest> getMultiTestsForWorkspace(Workspace workspace) {
        try {
            return workspace.getMultiTests();
        } catch (IOException e) {
            getUtils().getNotifier().notifyError("Failed to get multi tests for workspace id =" + workspace.getId() + ". Reason is: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public List<AbstractTest> getAllTestsForWorkspace(Workspace workspace) {
        List<AbstractTest> tests = new ArrayList<>();
        tests.addAll(getSingleTestsForWorkspace(workspace));
        tests.addAll(getMultiTestsForWorkspace(workspace));
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
