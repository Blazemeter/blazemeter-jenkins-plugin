package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.BaseCredentials;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.plugins.blazemeter.utils.Constants;
import org.apache.commons.lang.StringUtils;

/**
 * @author Vivek Pandey
 */
public abstract  class AbstractBlazemeterCredential extends BaseCredentials implements BlazemeterCredential{

    protected AbstractBlazemeterCredential() {
        super(CredentialsScope.GLOBAL);
    }

    protected AbstractBlazemeterCredential(CredentialsScope scope) {
        super(scope);
    }

    public String getId() {
        final String apiKey = getApiKey();
        return StringUtils.left(apiKey,4) + Constants.CREDENTIALS_KEY + StringUtils.right(apiKey, 4);
    }
}
