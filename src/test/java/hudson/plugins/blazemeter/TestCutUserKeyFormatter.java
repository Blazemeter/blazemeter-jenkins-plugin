package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.api.CutUserKeyFormatter;
import hudson.plugins.blazemeter.utils.Constants;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zmicer on 15.12.16.
 */
public class TestCutUserKeyFormatter {

    @Test
    public void format_true() {
        CutUserKeyFormatter cuk = new CutUserKeyFormatter();
        TestLogRecord lr = new TestLogRecord(Level.INFO,
            "https://a.blazemeter.com/api/latest/tests/5283127/start?api_key=12345678901234566567");
        Assert.assertTrue(cuk.format(lr).indexOf(Constants.THREE_DOTS)==105);
    }

    @Test
    public void format_false() {
        CutUserKeyFormatter cuk = new CutUserKeyFormatter();
        TestLogRecord lr = new TestLogRecord(Level.INFO,
            "https://a.blazemeter.com/api/latest/tests/5283127/start?");
        Assert.assertFalse(cuk.format(lr).contains(Constants.THREE_DOTS));
    }

    private class TestLogRecord extends LogRecord {
        public TestLogRecord(final Level level, final String msg) {
            super(level, msg);
        }
    }
}
