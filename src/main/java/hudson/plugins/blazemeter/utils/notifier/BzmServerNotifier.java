/**
 * Copyright 2018 BlazeMeter Inc.
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
import hudson.plugins.blazemeter.utils.logger.BzmServerLogger;

import java.util.logging.Logger;

public class BzmServerNotifier implements UserNotifier {

    private Logger logger = Logger.getLogger(BzmServerLogger.class.getName());

    @Override
    public void notifyInfo(String s) {
        logger.info("Notification: " + s);
    }

    @Override
    public void notifyWarning(String s) {
        logger.warning("Notification: " + s);
    }

    @Override
    public void notifyError(String s) {
        logger.severe("Notification: " + s);
    }
}
