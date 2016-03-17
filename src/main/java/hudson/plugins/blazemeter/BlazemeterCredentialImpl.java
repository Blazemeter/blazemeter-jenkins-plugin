package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.plugins.blazemeter.utils.Constants;
import hudson.plugins.blazemeter.utils.BzmServiceManager;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.eclipse.jetty.util.log.StdErrLog;
import org.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class BlazemeterCredentialImpl extends AbstractBlazemeterCredential {

    /**
     * Ensure consistent serialization.
     */
    private static final long serialVersionUID = 1L;
    private static StdErrLog logger = new StdErrLog(Constants.BZM_JEN);

    //private final Secret apiKey;
    private final String apiKey;
    private final String description;

    @DataBoundConstructor
    public BlazemeterCredentialImpl(String apiKey, String description) {
        this.apiKey = apiKey;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }


    public String getApiKey() {
        return apiKey;
    }

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.BlazemeterCredential_DisplayName();
        }

        @Override
        public ListBoxModel doFillScopeItems() {
            ListBoxModel m = new ListBoxModel();
            m.add(CredentialsScope.GLOBAL.getDisplayName(), CredentialsScope.GLOBAL.toString());
            return m;
        }

        // Used by global.jelly to authenticate User key
        public FormValidation doTestConnection(@QueryParameter("apiKey") final String userKey) throws MessagingException, IOException, JSONException, ServletException {
            BlazeMeterPerformanceBuilderDescriptor descriptor=BlazeMeterPerformanceBuilderDescriptor.getDescriptor();
            return  BzmServiceManager.validateUserKey(userKey,descriptor.getBlazeMeterURL(),
                    descriptor.getProxyHost(),
                    descriptor.getProxyPort(),
                    descriptor.getProxyUser(),
                    descriptor.getProxyPass());
        }

    }
}
