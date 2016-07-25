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

package hudson.plugins.blazemeter.utils.report;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class BuildReporter {
    private static int LOG_INTERVAL=10;

    private final ScheduledExecutorService exec = Executors.newScheduledThreadPool(100);
    private ScheduledFuture<?> urlTask;
    private ScheduledFuture<?> logTask;

    public BuildReporter(){
    }

    public void run(ReportUrlTask g,LoggerTask l) {
        if ((urlTask == null || urlTask.isDone())) {
            urlTask = exec.scheduleAtFixedRate(g, 120, 120, TimeUnit.SECONDS);
        }
        if ((logTask == null || logTask.isDone())) {
            logTask = exec.scheduleAtFixedRate(l, 5, LOG_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public void stop() {
        if ((this.urlTask != null || !this.urlTask.isDone())) {
            this.urlTask.cancel(false);
        }
        if ((logTask != null || !logTask.isDone())) {
            try {
                Thread.sleep(LOG_INTERVAL*2*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                logTask.cancel(false);
            }
        }
    }
}
