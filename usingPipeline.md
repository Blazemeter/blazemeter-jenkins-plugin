## since v4.0:

You can use [Snippet Generator](https://jenkins.io/blog/2016/05/31/pipeline-snippetizer/) for automatically generate your pipeline script.

## since v3.0 with new API key:

```
 node('master') {
     stage ('BlazeMeter test'){
     blazeMeterTest credentialsId:'jenkins_Id_Of_New_API_key',
     serverUrl:'https://a.blazemeter.com',
     testId:'53',
     notes:'',
     sessionProperties:'',
     jtlPath:'',
     junitPath:'',
     getJtl:false,
     getJunit:false
     }
 }

```

Parameters:

- `credentialsId` is Jenkins internal unique ID by which credentials (New API keys) are identified from jobs and other configuration.

- `serverUrl` is an optional parameter beginning from v3.0 of the plugin. But if `serverUrl` is present in the script - it is required and should be equal to the value from `Manage Jenkins`->`Configure system`(`https://a.blazemeter.com` by default).


## before v3.0 or v3.0 with old API key:

```
 node('master') {
     stage ('BlazeMeter test'){
     blazeMeterTest jobApiKey:'12z',
     serverUrl:'https://a.blazemeter.com',
     testId:'53',
     notes:'',
     sessionProperties:'',
     jtlPath:'',
     junitPath:'',
     getJtl:false,
     getJunit:false
     }
 }
```

- `jobApiKey` can be obtained from [BlazeMeter account](https://a.blazemeter.com/app/#/settings/personal)
`jobApiKey` should present in Credential list.

- `serverUrl` is required and should be equal to the value from `Manage Jenkins`->`Configure system`(`https://a.blazemeter.com` by default). It is an optional parameter since v3.0 of the plugin.

- `testId` - testId of desired test.

- `notes` - put here any text that you want add as a mark to test report.

- `sessionProperties` - it's possible to add jmeter properties to test session. Add them in 
`<key1=value1,key2=value2>` format, Jenkins variables will be resolved into actual values.

- `jtlPath` - path for JTL artifacts to be saved to.

- `junitPath` - path for junit report to be saved to.

- `getJtl` - true/false.

- `getJunit` - true/false.
