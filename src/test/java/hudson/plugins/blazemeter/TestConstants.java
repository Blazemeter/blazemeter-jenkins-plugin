package hudson.plugins.blazemeter;

/**
 * Created by zmicer on 9.7.15.
 */
public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/hudson/plugins/blazemeter/resources";


    // Mocked API constants
    String MOCKED_USER_KEY_VALID ="mockedAPIKeyValid";
    String MOCKED_USER_KEY_INVALID ="mockedAPIKeyInValid";
    String MOCKED_USER_KEY_EXCEPTION ="mockedAPIKeyException";
    int mockedApiPort=1234;
    String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;

    String TEST_ID="testId";
    String TEST_SESSION_25="testSession-25";
    String TEST_SESSION_70="testSession-70";
    String TEST_SESSION_100="testSession-100";
    String TEST_SESSION_140="testSession-140";
}
