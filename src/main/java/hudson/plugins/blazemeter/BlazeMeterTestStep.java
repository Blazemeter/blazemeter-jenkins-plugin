/**
 * Copyright 2016 BlazeMeter Inc.
 *
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

import com.google.inject.Inject;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class BlazeMeterTestStep extends Step {

    private String jobApiKey = "";

    private String serverUrl = "";

    private String testId = "";

    private String notes = "";

    private String sessionProperties = "";

    private String jtlPath = "";

    private String junitPath = "";

    private boolean getJtl = false;

    private boolean getJunit = false;

    @DataBoundConstructor
    public BlazeMeterTestStep(String jobApiKey,
        String serverUrl,
        String testId,
        String notes,
        String sessionProperties,
        String jtlPath,
        String junitPath,
        boolean getJtl,
        boolean getJunit

    ) {
        this.jobApiKey = jobApiKey;
        this.serverUrl = serverUrl;
        this.testId = testId;
        this.jtlPath = jtlPath;
        this.junitPath = junitPath;
        this.getJtl = getJtl;
        this.getJunit = getJunit;
        this.notes = notes;
        this.sessionProperties = sessionProperties;

    }

    @Override
    public StepExecution start(final StepContext stepContext) throws Exception {
        return new BlazeMeterTestExecution(stepContext,this);
    }

    public String getJobApiKey() {
        return this.jobApiKey;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public String getTestId() {
        return this.testId;
    }

    public String getNotes() {
        return this.notes;
    }

    public String getSessionProperties() {
        return this.sessionProperties;
    }

    public String getJtlPath() {
        return this.jtlPath;
    }

    public String getJunitPath() {
        return this.junitPath;
    }

    public boolean isGetJtl() {
        return this.getJtl;
    }

    public boolean isGetJunit() {
        return this.getJunit;
    }

    public static class BlazeMeterTestExecution extends SynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        @Inject(optional = true)
        private transient BlazeMeterTestStep step;
        @SuppressWarnings("rawtypes")
        private transient StepContext context;

        protected BlazeMeterTestExecution(@Nonnull final StepContext context,@Nonnull final BlazeMeterTestStep step) {
            super(context);
            this.context = context;
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {
            PerformanceBuilder pb = new PerformanceBuilder(step.getJobApiKey(),
                step.getServerUrl(),
                step.getTestId(),
                step.getNotes(),
                step.getSessionProperties(),
                step.getJtlPath(),
                step.getJunitPath(),
                step.isGetJtl(),
                step.isGetJunit()
            );
            Run r = this.context.get(Run.class);
            FilePath fp = this.context.get(FilePath.class);
            Launcher l = this.context.get(Launcher.class);
            TaskListener tl = this.context.get(TaskListener.class);
            pb.perform(r, fp, l, tl);
            return null;
        }
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            Set<Class<?>> r = new HashSet<Class<?>>();
            r.add(Run.class);
            r.add(FilePath.class);
            r.add(Launcher.class);
            r.add(TaskListener.class);
            return r;
        }

        @Override
        public String getFunctionName() {
            return "blazeMeterTest";
        }

        @Override
        public String getDisplayName() {
            return "Runs test in BlazeMeter Cloud";
        }
    }
}
