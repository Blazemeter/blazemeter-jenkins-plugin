package hudson.plugins.blazemeter;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by dzmitrykashlach on 8/13/14.
 */
public  final class TextBoxValueValidationDescriptor extends BuildStepDescriptor<Builder> {

//descriptor's code

/**
 * Performs on-the-fly validation of the form field 'value'.
 *
 * @param value
 *            This parameter receives the value that the user has typed.
 * @return Indicates the outcome of the validation. This is sent to the
 *         browser.
 */
public FormValidation checkValue(@QueryParameter String value) throws IOException, ServletException {
        if(value.equals("0")) {
        return FormValidation.warning("Response time should be more than ZERO");
        }
        return FormValidation.ok();
        }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return false;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
