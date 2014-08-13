package hudson.plugins.blazemeter;

import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 8/13/14.
 */
public class ResponseTimeValidation {

//descriptor's code

/**
 * Performs on-the-fly validation of the form field 'value'.
 *
 * @param value
 *            This parameter receives the value that the user has typed.
 * @return Indicates the outcome of the validation. This is sent to the
 *         browser.
 */
public FormValidation doCheckValue(@QueryParameter String value) throws IOException, ServletException {
        if(value.equals("0")) {
        return FormValidation.warning("Response time should be more than ZERO");
        }
        return FormValidation.ok();
        }
        }
