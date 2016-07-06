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

import hudson.FilePath;
import hudson.plugins.blazemeter.utils.Constants;
import org.eclipse.jetty.util.log.StdErrLog;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;


public class LoggerTask implements Runnable {
    private static String SPLIT_REG_EX="(channel:)|(#[0-9]{1,}:)";
    private FilePath l = null;
    private PrintStream ps = null;
    private int logged=0;

    public LoggerTask(PrintStream ps,FilePath l) {
        this.ps = ps;
        this.l = l;
    }

    @Override
    public void run() {
        try {
            StdErrLog errLog = new StdErrLog(Constants.BZM_JEN);
            errLog.setPrintLongNames(false);
            errLog.setStdErrStream(this.ps);
            String s=this.l.readToString();
            if (s != null) {
                int currentLogged=0;
                List<String> ls= Arrays.asList(s.split("\\n"));
                int lss=ls.size();
                for (int i = 0 + this.logged; i < lss; i++) {
                    String lstr = ls.get(i);
                    try {
                        errLog.info(lstr.split(SPLIT_REG_EX)[1]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    } finally {
                        currentLogged++;
                    }
                }
                this.logged=this.logged+currentLogged;

            }
        } catch (Exception e) {
            this.logged=0;
            return;
        }
    }
}

