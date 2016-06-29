package hudson.plugins.blazemeter.utils.report;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by zmicer on 20.6.16.
 */
public class BuildReporter {
    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(2);
    private static ScheduledFuture<?> urlTask;
    private static ScheduledFuture<?> logTask;
    public static void run(ReportUrlTask g,LoggerTask l) {
        if ((urlTask == null || urlTask.isDone())) {
            urlTask = exec.scheduleAtFixedRate(g, 120, 120, TimeUnit.SECONDS);
        }
        if ((logTask == null || logTask.isDone())) {
            logTask = exec.scheduleAtFixedRate(l, 120, 10, TimeUnit.SECONDS);
        }
    }

    public static void stop() {
        if ((urlTask != null || !urlTask.isDone())) {
            urlTask.cancel(false);
        }
        if ((logTask != null || !logTask.isDone())) {
            logTask.cancel(false);
        }
    }
}
