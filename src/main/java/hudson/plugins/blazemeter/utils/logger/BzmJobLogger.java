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

package hudson.plugins.blazemeter.utils.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class BzmJobLogger implements com.blazemeter.api.logging.Logger {

    private Logger logger = Logger.getLogger(BzmJobLogger.class.getName());
    private Logger fileLogger;
    private FileHandler fileHandler;

    public BzmJobLogger(String logFile) {
        File f = new File(logFile);
        this.fileLogger = Logger.getLogger(f.getName());
        try {
            fileHandler = new FileHandler(logFile);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot create file handler for log file", ex);
        }
        fileHandler.setFormatter(new SimpleFormatter());
        fileLogger.addHandler(fileHandler);
        fileLogger.setUseParentHandlers(false);
        fileLogger.setLevel(Level.FINE);
    }

    public void close() {
        fileHandler.close();
    }

    @Override
    public void debug(String s) {
        logger.log(Level.FINE, s);
        fileLogger.log(Level.FINE, s);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        logger.log(Level.FINE, s, throwable);
        fileLogger.log(Level.FINE, s, throwable);
    }

    @Override
    public void info(String s) {
        logger.log(Level.INFO, s);
        fileLogger.log(Level.INFO, s);
    }

    @Override
    public void info(String s, Throwable throwable) {
        logger.log(Level.INFO, s, throwable);
        fileLogger.log(Level.INFO, s, throwable);
    }

    @Override
    public void warn(String s) {
        logger.log(Level.WARNING, s);
        fileLogger.log(Level.WARNING, s);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        logger.log(Level.WARNING, s, throwable);
        fileLogger.log(Level.WARNING, s, throwable);
    }

    @Override
    public void error(String s) {
        logger.log(Level.SEVERE, s);
        fileLogger.log(Level.SEVERE, s);
    }

    @Override
    public void error(String s, Throwable throwable) {
        logger.log(Level.SEVERE, s, throwable);
        fileLogger.log(Level.SEVERE, s, throwable);
    }
}
