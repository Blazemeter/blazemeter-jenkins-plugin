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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BzmJobNotifier implements UserNotifier {

    private final TaskListener listener;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public BzmJobNotifier(TaskListener listener) {
        this.listener = listener;
    }

    public static String formatMessage(String msg) {
        Date date = new Date();
        return "[" + dateFormat.format(date) + "]: " + msg;
    }

    @Override
    public void notifyInfo(String s) {
        listener.getLogger().println(formatMessage(s));
    }

    @Override
    public void notifyWarning(String s) {
        listener.getLogger().println(formatMessage("WARN: " + s));
    }

    @Override
    public void notifyError(String s) {
        listener.error(formatMessage(s));
    }
}
