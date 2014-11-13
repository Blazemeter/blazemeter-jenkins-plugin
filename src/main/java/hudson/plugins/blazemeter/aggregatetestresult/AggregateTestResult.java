package hudson.plugins.blazemeter.aggregatetestresult;

/**
 * BlazeMeter
 * User: moshe
 * Date: 5/28/12
 * Time: 12:51 PM
 */
public abstract class AggregateTestResult {
    double std              ;
    double average          ;
    double min              ;
    double max              ;
    double samples          ;
    double median           ;
    double percentile90     ;
    double percentile99     ;
    double errorPercentage  ;
    double hits             ;
    double kbs              ;
    long   n                ;

    public double getErrorPercentage() {
        return errorPercentage;
    }

    public double getAverage() {
        return average;
    }
}
