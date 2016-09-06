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

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;


@Extension(optional = true)
public class PerformanceBuilderDSLExtension extends ContextExtensionPoint{
    @DslExtensionMethod(context = StepContext.class)
    public Object blazeMeterTest(Runnable closure){
        PerformanceBuilderDSLContext c = new PerformanceBuilderDSLContext();
        executeInContext(closure,c);
        return new PerformanceBuilder(c.jobApiKey, BlazeMeterPerformanceBuilderDescriptor.getDescriptor().getBlazeMeterURL()
                , c.testId, c.notes, c.sessionProperties,
                c.jtlPath, c.junitPath, c.getJtl, c.getJunit);
    }
}
