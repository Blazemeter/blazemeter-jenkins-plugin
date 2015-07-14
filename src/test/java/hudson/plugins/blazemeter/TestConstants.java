package hudson.plugins.blazemeter;

/**
 * Created by zmicer on 9.7.15.
 */
public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/hudson/plugins/blazemeter/resources";


    // Mocked API constants
    String MOCKED_USER_KEY_VALID ="mockedAPIKeyValid";
    String MOCKED_USER_KEY_V2 ="mockedAPIKeyV2";
    String MOCKED_USER_KEY_6_TESTS ="mockedAPIKeyValid-1-tests";
    String MOCKED_USER_KEY_1_TEST ="mockedAPIKeyValid-1-test";
    String MOCKED_USER_KEY_0_TESTS ="mockedAPIKeyValid-0-tests";
    String MOCKED_USER_KEY_INVALID ="mockedAPIKeyInValid";
    String MOCKED_USER_KEY_EXCEPTION ="mockedAPIKeyException";
    int mockedApiPort=1234;
    String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;

    String TEST_SESSION_ID ="testSessionId";
    String TEST_SESSION_NOT_FOUND="testSession-not-found";
    String TEST_SESSION_0="testSession-0";
    String TEST_SESSION_25="testSession-25";
    String TEST_SESSION_70="testSession-70";
    String TEST_SESSION_100="testSession-100";
    String TEST_SESSION_140="testSession-140";
    String TEST_SESSION_SUCCESS="testSessionSuccess";
    String TEST_SESSION_FAILURE="testSessionFailure";

    String YAHOO="yahoo";
}
