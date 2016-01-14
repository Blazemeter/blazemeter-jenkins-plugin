package hudson.plugins.blazemeter;

/**
 * Created by zmicer on 9.7.15.
 */
public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/hudson/plugins/blazemeter/resources";


    // Mocked API constants
    String MOCKED_USER_KEY_VALID ="mockedAPIKeyValid";
    String MOCKED_USER_KEY_RETRIES ="mockedAPIKeyRetries";
    String MOCKED_USER_KEY_TEST_TYPE ="mockedAPIKeyTestType";
    String MOCKED_USER_KEY_V2 ="mockedAPIKeyV2";
    String MOCKED_USER_KEY_6_TESTS ="mockedAPIKeyValid-1-tests";
    String MOCKED_USER_KEY_1_TEST ="mockedAPIKeyValid-1-test";
    String MOCKED_USER_KEY_0_TESTS ="mockedAPIKeyValid-0-tests";
    String MOCKED_USER_KEY_INVALID ="mockedAPIKeyInValid";
    String MOCKED_USER_KEY_EXCEPTION ="mockedAPIKeyException";
    int mockedApiPort=1234;
    String mockedApiUrl="http://127.0.0.1:"+mockedApiPort;

    String TEST_MASTER_ID ="testMasterId";
    String TEST_MASTER_NOT_FOUND ="testMaster-not-found";
    String TEST_MASTER_0 ="testMaster-0";
    String TEST_MASTER_25 ="testMaster-25";
    String TEST_MASTER_70 ="testMaster-70";
    String TEST_MASTER_100 ="testMaster-100";
    String TEST_MASTER_140 ="testMaster-140";
    String TEST_MASTER_SUCCESS ="testMasterSuccess";
    String TEST_MASTER_FAILURE ="testMasterFailure";
    String TEST_MASTER_ERROR ="testMasterError";

    String YAHOO="yahoo";
}
