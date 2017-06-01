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

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.util.FormValidation;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import net.sf.json.JSONException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

@SuppressWarnings("unused") // read resolved by extension plugins
public class BlazemeterCredentialsLegacyImpl extends BaseStandardCredentials implements BlazemeterCredentials {

     public static BlazemeterCredentialsLegacyImpl EMPTY = new BlazemeterCredentialsLegacyImpl(CredentialsScope.GLOBAL,"","","");
    /**
     * The key.
     */
    @NonNull
    private final String key;

    /**
     * Constructor.
     *
     * @param scope       the credentials scope
     * @param id          the ID or {@code null} to generate a new one.
     * @param description the description.
     * @param key    the key.
     */
    @DataBoundConstructor
    @SuppressWarnings("unused") // by stapler
    public BlazemeterCredentialsLegacyImpl(@CheckForNull CredentialsScope scope,
        @CheckForNull String id, @CheckForNull String description,
        @CheckForNull String key) {
        super(scope, id, description);
        this.key = Util.fixNull(key);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public String getKey() {
        return key;
    }

    /**
     * {@inheritDoc}
     */
    @Extension(ordinal = 1)
    public static class DescriptorImpl extends BaseStandardCredentialsDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return Messages.BlazemeterLegacyCredential_DisplayName();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getIconClassName() {
            return "icon-credentials-userpass";
        }

        public FormValidation doTestConnection(@QueryParameter("key") final String username)
            throws MessagingException, IOException, JSONException, ServletException {
            /* TODO
            String plainPass = null;
            Secret decrPassword = Secret.fromString(password);
            try {
                plainPass = decrPassword.getPlainText();
            } catch (NullPointerException npe) {
                return FormValidation.error("Failed to decrypt password to plain text");
            }
            String serverUrl = BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL();
            */
//            return JobUtility.validateCredentials(username, plainPass, serverUrl);
            return FormValidation.error("Not implemented");
        }

    }
}

