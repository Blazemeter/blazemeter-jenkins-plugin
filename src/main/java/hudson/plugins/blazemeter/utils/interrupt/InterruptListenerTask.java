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

package hudson.plugins.blazemeter.utils.interrupt;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.remoting.VirtualChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

public class InterruptListenerTask extends Thread {

    protected Logger logger = Logger.getLogger(InterruptListenerTask.class.getName());
    private final int ATTEMPT_COUNT = Integer.parseInt(System.getProperty("bzm.interrupt.attemptCount", "60"));
    private final long RETRY_DELAY = 5000;

    protected String jobName;

    private VirtualChannel channel;
    private Run run;

    protected boolean isDone = false;

    public InterruptListenerTask(Run run, String jobName, VirtualChannel channel) {
        this.run = run;
        this.jobName = jobName;
        this.channel = channel;
    }

    @Override
    public void run() {
        int i = 0;
        while (!isDone && i < ATTEMPT_COUNT) {
            try {
                logger.log(Level.FINE, "Check interrupt status. Attempt #" + i);

                i++;
                EnvVars ev = EnvVars.getRemote(channel);
                String ruId = "isInterrupted-" + jobName + "-" + run.getId();
                if (ev != null && ev.containsKey(ruId)) {
                    String isInterrupted = ev.get(ruId, "false");
                    if ("true".equals(isInterrupted)) {
                        isDone = true;
                        logger.log(Level.FINE, "Interrupt status = true. Finished.");
                        return;
                    }
                }
                Thread.sleep(RETRY_DELAY);
            } catch (InterruptedException e) {
                // It means second interrupt from user side
                logger.log(Level.WARNING, "Caught Interrupted Exception", e);
                return;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to get isInterrupted status", e);
            }
        }
    }

}
