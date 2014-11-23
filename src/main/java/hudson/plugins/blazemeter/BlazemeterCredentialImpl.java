package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.Extension;
import hudson.plugins.blazemeter.api.APIFactory;
import hudson.plugins.blazemeter.api.BlazemeterApi;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
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

    /* Converted Secret into String
    public Secret getApiKey() {
    */
//        return apiKey;
//  }

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
            BlazemeterApi bzm = APIFactory.getApiFactory().getAPI(userKey);
            int testCount = bzm.getTestCount();
            if (testCount < 0) {
                return FormValidation.errorWithMarkup("An error as occurred, check proxy settings");
            } else if (testCount == 0) {
                return FormValidation.errorWithMarkup("User Key Invalid Or No Available Tests");
            } else {
                return FormValidation.ok("User Key Valid. " + testCount + " Available Tests");
            }
        }

    }
}
