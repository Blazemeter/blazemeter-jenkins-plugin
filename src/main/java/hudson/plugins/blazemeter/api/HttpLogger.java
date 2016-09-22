package hudson.plugins.blazemeter.api;

import hudson.plugins.blazemeter.utils.Constants;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by zmicer on 22.9.16.
 */
public class HttpLogger implements HttpLoggingInterceptor.Logger {

    private Logger httpLog=Logger.getLogger(Constants.HTTP_LOG);

    public  HttpLogger(String httpLog_f){
        FileHandler http_lfh= null;
        try {
            http_lfh = new FileHandler(httpLog_f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        http_lfh.setFormatter(new SimpleFormatter());
        httpLog.addHandler(http_lfh);
        httpLog.setUseParentHandlers(false);

    }

    @Override
    public void log(String message) {
         httpLog.info(message);
    }
}
