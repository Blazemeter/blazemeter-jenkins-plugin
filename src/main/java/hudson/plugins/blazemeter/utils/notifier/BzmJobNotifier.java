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

package hudson.plugins.blazemeter.utils.notifier;

import com.blazemeter.api.logging.UserNotifier;
import hudson.model.TaskListener;

public class BzmJobNotifier implements UserNotifier {

    private final TaskListener listener;

    public BzmJobNotifier(TaskListener listener) {
        this.listener = listener;
    }

    @Override
    public void notifyInfo(String s) {
        listener.getLogger().println(s);
    }

    @Override
    public void notifyWarning(String s) {
        listener.getLogger().println("WARN: " + s);
    }

    @Override
    public void notifyError(String s) {
        listener.error(s);
    }
}
