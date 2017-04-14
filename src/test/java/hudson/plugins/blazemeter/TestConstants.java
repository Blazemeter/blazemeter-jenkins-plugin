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

package hudson.plugins.blazemeter;

public interface TestConstants {
    String RESOURCES = System.getProperty("user.dir")+"/src/test/java/hudson/plugins/blazemeter/resources";


    // Mocked API constants
    int mockedApiPort=1234;
    String mockedApiHost="http://127.0.0.1:";
    String mockedApiUrl=mockedApiHost+mockedApiPort;

    String TEST_MASTER_ID ="testMasterId";
    String TEST_MASTER_WAIT_FOR_FINISH ="testMasterIdWaitForFinish";
    String TEST_MASTER_15102806 ="15102806";
    String TEST_5039530_ID ="5039530";
    String TEST_5075679_ID ="5075679";
    String TEST_MASTER_NOT_FOUND ="testMaster-not-found";
    String TEST_MASTER_0 ="testMaster-0";
    String TEST_MASTER_25 ="testMaster-25";
    String TEST_MASTER_70 ="testMaster-70";
    String TEST_MASTER_100 ="testMaster-100";
    String TEST_MASTER_140 ="testMaster-140";
    String TEST_MASTER_100_notes ="testMaster-100_notes";
    String TEST_MASTER_SUCCESS ="testMasterSuccess";
    String TEST_MASTER_FAILURE ="testMasterFailure";
    String TEST_MASTER_ERROR_61700 ="testMasterError_61700";
    String TEST_MASTER_ERROR_0 ="testMasterError_0";
    String TEST_MASTER_ERROR_70404 ="testMasterError_70404";

    String MOCKED_SESSION ="r-v3-585114ca535ed";
    String MOCKED_NOTE = "bbbbbbbbbbbbbbbbbbbbb";
    String JTL_URL = mockedApiUrl+"/users/1689/tests/5283127/reports/r-v3-585114ca535ed/jtls_and_more.zip?" +
        "AWSAccessKeyId=AKIAJPZOF6U7I33QK2CQ&Expires=1481718157&Signature=lwSzIQtbopufhiExFwmEUheCah8%3D";

    String MOCK_EMPTY_USER="";
    String MOCK_EMPTY_PASSWORD="";
    String MOCK_EXCEPTION_USER="exceptionUser";
    String MOCK_EXCEPTION_ID="exceptionId";
    String MOCK_EXCEPTION_DESCRIPTION="exceptionDescription";
    String MOCK_EXCEPTION_PASSWORD="exceptionPassword";
    String MOCK_INVALID_ID="invalidId";
    String MOCK_INVALID_DESCRIPTION="invalidDescription";
    String MOCK_INVALID_USER="invalidUser";
    String MOCK_INVALID_PASSWORD="invalidPassword";
    String MOCK_VALID_ID="validId";
    String MOCK_VALID_DESCRIPTION="validDescription";
    String MOCK_VALID_USER="validUser";
    String MOCK_VALID_PASSWORD="validPassword";
    String MOCK_1_TEST_USER ="1-test-user";
    String MOCK_1_TEST_PASSWORD ="1-test-password";
    String MOCK_0_TEST_USER ="0-test-user";
    String MOCK_0_TEST_PASSWORD ="0-test-password";
    String MOCK_5_TEST_USER ="5-test-user";
    String MOCK_5_TEST_PASSWORD ="5-test-password";
}
