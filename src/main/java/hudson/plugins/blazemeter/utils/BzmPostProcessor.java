/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
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

package hudson.plugins.blazemeter.utils;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import com.blazemeter.ciworkflow.CiPostProcess;
import hudson.FilePath;

public class BzmPostProcessor extends CiPostProcess {

    private final FilePath workspace;

    public BzmPostProcessor(boolean isDownloadJtl, boolean isDownloadJunit,
                            String jtlPath, String junitPath,
                            FilePath workspace,
                            UserNotifier notifier, Logger logger) {
        super(isDownloadJtl, isDownloadJunit, jtlPath, junitPath, workspace.getRemote(), notifier, logger);
        this.workspace = workspace;
    }

    @Override
    public void saveJunit(Master master) {
        super.saveJunit(master);
    }

    @Override
    public void saveJTL(Master master) {
        super.saveJTL(master);
    }

}
