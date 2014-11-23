package hudson.plugins.blazemeter;

import com.cloudbees.plugins.credentials.Credentials;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import org.apache.commons.lang.StringUtils;

/**
 * @author Vivek Pandey
 */
public interface BlazemeterCredential extends Credentials {

    public String getDescription();

    public String getId();

   /*  Converted Secret into String
   public Secret getApiKey();
   */
    public String getApiKey();

}
