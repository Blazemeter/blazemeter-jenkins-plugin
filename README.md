
***
BlazeMeter Job DSL Example:
***

    job('bza-from-dsl'){
       steps{
         blazeMeterTest{
           jobApiKey '<your-key-that-is-present-in-credentials>'
           testId 'testId-existing-on-server'
           notes ''
           sessionProperties '' 
           jtlPath '' 
           junitPath '' 
           getJtl false 
           getJunit false 
      
         }
       }
    }