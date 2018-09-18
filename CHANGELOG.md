#### v4.7 - upcoming

- `ADDED` - upgrade notifications
- `ADDED` - BlazeMeter logo to Report link

#### v4.6 - 27.08.2018

- `ADDED` - Mark build as UNSTABLE if BlazeMeter does not respond with pass or fail (Manage Jenkins -> Configure System -> BlazeMeter Cloud).

_If this setting is disabled then build will be marked as FAILED in case BlazeMeter does not respond with pass or fail. If test was not started and this setting is enabled then build will be marked as UNSTABLE_

- `FIXED`: Retry Interceptor. Use Java property `bzm.request.retries.count` for config retries count (by default it is 3).

_Will retry only `GET` requests in case if response code was not 2** or was throw `SocketTimeoutException`_


#### v4.5 - 15.08.2018

- `ADDED` - Abort job if BlazeMeter has fails
- `CHANGES` - Mark build as unstable if got error: "Not enough available resources"

#### v4.4 - 26.07.2018

- `ADDED` - Validation for test files

#### v4.3 - 23.07.2018

- `FEATURE` - Update test files before running test

_Limitations: For Taurus and FunctionalApi tests will set 'Test type': JMeter if file has '.jmx' extension and Taurus - for '.yml' and '.yaml' files. Other 'Test types', please, set in the BlazeMeter application. Supported test types: Taurus, FunctionalApi, JMeter_

- `FIXED` - Search field in Tests dropdown is broken when you add the second BlazeMeter build step

#### v4.2 - 10.05.2018

- `FEATURE` - Change BlazeMeter report link name. _By default max report link name length = 35, but you can increase this value using Java System property or Jenkins environment variable "bzm.reportLinkName.length"_
- `FEATURE` - Improve set up proxy configuration. _By default all slaves will use proxy configuration from Manage Jenkins> Manage Plugins > Advanced. For override proxy setting for any agents need pass Java System property to this agent: -Dproxy.override=true. And if you want use other proxy add -Dhttp.proxyHost=<HOST> -Dhttp.proxyPort=<PORT> -Dhttp.proxyUser=<USER> -Dhttp.proxyPass=<PASSWORD> or nothing more for work without proxy_


#### v4.1 - 21.03.2018

- `FIXED` - Links to BlazeMeter reports when job has 2 or more `blazeMeterTest` steps. [BUG #49195](https://issues.jenkins-ci.org/browse/JENKINS-49195)
- `FIXED` - `bzm-log` file contains logs only from one Master
- `CHANGES` - update `blazemeter-api-client` dependency version from `1.5` to `1.8` ([changelog](https://github.com/Blazemeter/blazemeter-api-client/wiki/Changelog))

#### v4.0 - 23.02.2018

- `FEATURE`: Search tests in `Tests` dropdown
- `CHANGES`: Migration to [blazemeter-api-client](https://github.com/Blazemeter/blazemeter-api-client) 
  * **complete old keys deprecation;**  
- `CHANGES`: Combining `bzm-log` file with `http-log` file; Resulting file will have name `bzm-log`.  
  See [Logging](https://github.com/jenkinsci/blazemeter-plugin/wiki/Logging)  for the file location.  
- `FIXED`: Snippet generator for Jenkins pipeline.  
- `CHANGES`: JDK 7 back-compatibility.  

Verification environment: Jenkins 2.89.3  

#### v3.1 - 27.11.2017

- `FEATURE`: `workspace` selector to job configuration: users can select BlazeMeter workspace to fetch tests from.
- `FEATURE`: `average throughput` and `90% response time` metrics to aggregate report;
- Aggregate report values are rounded to 2 decimal places;
 
Verification environment: Jenkins 2.73.2 LTS  

#### v3.0 - 30.08.2017

- `FEATURE`: support new [BlazeMeter api-keys](https://guide.blazemeter.com/hc/en-us/articles/115002213289-BlazeMeter-API-keys)  

Verification environment: Jenkins 2.60.2 LTS  
NOTE: _While plugin still can work with old keys, we strictly recommend to migrate to new api-keys._

#### v2.8 - 20.04.2017

- [Pipeline](https://jenkins.io/doc/book/pipeline/) support;

#### v2.7 - 24.11.2016

- `FEATURE`: support Job DSL;
- Migration to [OkHttp](https://square.github.io/okhttp/) client;

#### v2.6 - 25.09.2016

- `FEATURE`: sort test drop-down alphabetically;
- `FEATURE`: do 3 re-tries if received 500 from server;

#### v2.5 - 04.07.2016

- `Added`: full master/slave support - BZM job will be completely executed at JVM of selected node(master or slave).

#### v2.4 - 02.06.2016

- `CHANGES`: Using native jenkins interface for proxy configuration. 
      Go to "Manage Jenkins" -> "Manage Plugins" ->"Advanced".  
      These proxy settings are used both by jenkins & plugin.

- `FEATURE`: support master/slave configuration
- `FEATURE`: doing 3 retries while downloading jtl report: connection timeout - 10,30,90 s.
- `FEATURE`: ability to send jmeter-properties to session;
- `FEATURE`: ability to send report notes to master session;
- `FEATURE`: ability to set path to jtl report(jenkins variables are respected)
- `FEATURE`: ability to set path to junit report(jenkins variables are respected)
_Last four settings are available under "Advanced" button._

#### v2.3

- `FEATURE`: Support proxy server. Proxy setting are available in "Manage Jenkins" -> "Configure System".

#### v2.2.1

- `FIXED`: Set default value for 'serverUrl';
- `FIXED`: Default value will be used if  'serverUrl' was left empty;

#### v2.2

- `FIXED`: Download reports if test finished with failures;
- `FIXED`: Empty userKey validation;
- `FIXED`: Build will be failed if test was terminated on booting stage; 

#### v2.1

- `FIXED`: minor changes;

#### v2.0

- `FEATURE`: Plugin now supports Blazemeter 3.0 tests
- `FEATURE`: Plugin now supports JSON tests

#### v1.08

- `FIXED`: Aggregate results more accurate, and handle errors better.

#### v1.07

- `FIXED`: API calls handle errors better.

#### v1.06

- Release with minor changes.

#### v1.05

- `FEATURE`: Plugin now supports proxy settings defined in: Jenkins/plugins/advanced.

#### v1.04

- `FEATURE`: Multiple UserKeys can be inserted via the "Credentials Plugin".

#### v1.02

- `FIXED`: Main JMX file uploads correctly.

#### v1.01

- `FEATURE`: Binary files now available for upload in data directory.
- `FIXED`: Main JMX file now set correctly.
- `FIXED`: Some graphical tweaks to the report page.
- `CHANGES`: Test duration override has 3 hours set by default, not 20 minutes.
- `CHANGES`: Test list populated by last updated.
- `CHANGES`: Report page has no need for the user to be logged in to BlazeMeter.

#### v1.0(02.06.2016)

The First release.
