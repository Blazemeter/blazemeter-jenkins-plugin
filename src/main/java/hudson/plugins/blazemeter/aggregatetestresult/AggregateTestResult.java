package hudson.plugins.blazemeter.aggregatetestresult;

/**
 * BlazeMeter
 * User: moshe
 * Date: 5/28/12
 * Time: 12:51 PM
 */
public abstract class AggregateTestResult {
    protected double std              ;
    protected double average          ;
    protected double min              ;
    protected double max              ;
    protected double samples          ;
    protected double median           ;
    protected double percentile90     ;
    protected double percentile99     ;
    protected double errorPercentage  ;
    protected double hits             ;
    protected double kbs              ;
    protected long   n                ;

    public double getErrorPercentage() {
        return errorPercentage;
    }

    public double getAverage() {
        return average;
    }
}
