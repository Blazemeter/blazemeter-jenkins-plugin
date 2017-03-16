node('master') {
  stage ('BlazeMeter test'){
    blazeMeterTest(
      jobApiKey:'123fjghn68t63dv',
      serverUrl:'https://a.blazemeter.com',
      testId:'53841',
      notes:'',
      sessionProperties:'',
      jtlPath:'',
      junitPath:'',
      getJtl:false,
      getJunit:false
    )
  }
}
