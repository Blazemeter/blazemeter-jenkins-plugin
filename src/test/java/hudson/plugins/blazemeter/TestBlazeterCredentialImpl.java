package hudson.plugins.blazemeter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by zmicer on 19.12.16.
 */
public class TestBlazeterCredentialImpl {

    @Test
    public void id() {
        String ak = "e7d12123456rpqifveej";
        String description = "e7d12123456rpqifveejdescription";
        String expectedId = "e7d1...veej";
        BlazemeterCredentialImpl c = new BlazemeterCredentialImpl(ak, description);
        Assert.assertEquals(expectedId,c.getId());
    }
}
