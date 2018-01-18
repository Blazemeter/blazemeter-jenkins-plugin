package hudson.plugins.blazemeter;

import hudson.plugins.blazemeter.utils.Utils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

public class TestUtils {


    @Test
    public void resolveId_positive() {
        String testId = "dddddddddd(1.d)";
        assertEquals("1.d", Utils.resolveTestId(testId));
    }

    @Test
    public void resolveId_not_changes() {
        String testId = "1.d";
        assertEquals("1.d", Utils.resolveTestId(testId));
    }

    @Test
    public void resolveId_negative() {
        String testId = "no-test-id";
        assertEquals("no-test-id", Utils.resolveTestId(testId));
    }

    @Test
    public void getTestId() {
        String testId = "1.d";
        assertEquals("1", Utils.getTestId(testId));
    }

    @Test
    public void version() {
        assertNotEquals("N/A", Utils.version());
    }
}
