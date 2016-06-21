package hudson.plugins.blazemeter.utils.report;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by zmicer on 20.6.16.
 */
public class ReportUrlGetter{
    private static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> task;
    public static void run(ReportUrlGetterTask g) {
        if ((task == null || task.isDone())) {
            task = exec.scheduleAtFixedRate(g, 120, 120, TimeUnit.SECONDS);
        }
    }

    public static void stop() {
        if ((task != null || !task.isDone())) {
            task.cancel(true);
        }
        exec.shutdownNow();
    }
}
