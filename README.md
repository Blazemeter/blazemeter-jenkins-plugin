
BlazeMeter Job DSL Example:
---------------------------

before v.2.7
------------

    job('bza-from-dsl'){
       configure{project-> project/'builders'<< builder(class:'hudson.plugins.blazemeter.PerformanceBuilder'){
           jobApiKey 'your-key-that-is-present-in-credentials'
           serverUrl 'https://a.blazemeter.com'
           testId 'your-key-that-is-present-in-credentials'
           notes 'x\nc\nu\ni'
           /** this note is equal to 
           x
           c
           u
           i
           **/
           sessionProperties ''
           jtlPath ''
           junitPath ''
           getJtl true
           getJunit true
           }
       }
    }

v.2.7
-----

    job('bza-from-dsl'){
       steps{
         blazeMeterTest{
           jobApiKey 'your-key-that-is-present-in-credentials'
           testId 'testId-existing-on-server'
           notes 'x\nc\nu\ni'
            /** this note is equal to 
            x
            c
            u
            i
            **/
           sessionProperties '' 
           jtlPath '' 
           junitPath '' 
           getJtl false 
           getJunit false 
      
         }
       }
    }