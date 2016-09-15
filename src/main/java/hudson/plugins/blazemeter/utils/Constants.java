package hudson.plugins.blazemeter.utils;

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

public interface Constants {
    String BZM_JEN="BZM-JEN";
    String HTTP_LOG="http-log";
    String BZM_LOG ="bzm-log";
    String BM_TRESHOLDS="bm-thresholds.xml";
    String BM_KPIS="bm-kpis.jtl";
    String BM_ARTEFACTS="-bm-artefacts";
    String VERSION="version";
    String CREDENTIALS_KEY="...";
    String NO_API_KEY="No API Key";
    String UNKNOWN_TYPE="unknown_type";
    String API_KEY_EMPTY ="API key is empty: please, enter valid API key";
    String A_BLAZEMETER_COM="https://a.blazemeter.com";
    String API_KEY_VALID="API key is valid: user e-mail=";
    String API_KEY_IS_NOT_VALID="API key is not valid";
    String NO_TESTS_FOR_API_KEY="No tests for api key";
    int ENCRYPT_CHARS_NUM=3;
}
