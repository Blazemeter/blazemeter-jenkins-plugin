Use the following samples if going to store Job configuration as Groovy DSL.

### Since v.3.0 with new API key
    def jobName = 'bzm-dsl'
        job(jobName) {
        configure{project-> project/'builders'<< builder(class:'hudson.plugins.blazemeter.PerformanceBuilder'){
        credentialsId 'credentials-id-of-new-api-key'
        workspaceId '<workspaceId>'
        testId '<test which is valid with key>'
        notes ''
        sessionProperties ''
        jtlPath ''
        junitPath ''
        getJtl true
        getJunit true
        }
      }
    }

    job('bza-from-dsl'){
       steps{
            blazeMeterTest{
            credentialsId 'credentials-id-of-new-api-key'
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

### Since v.2.7, v.3.0 with old API key

    job('bza-from-dsl'){
       steps{
            blazeMeterTest{
            jobApiKey 'value-of-your-key-that-is-present-in-credentials'
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

### Before v.2.7

    def jobName = 'bzm-dsl'
    job(jobName) {
        configure{project-> project/'builders'<< builder(class:'hudson.plugins.blazemeter.PerformanceBuilder'){
        jobApiKey '<your-actual-key>'
        serverUrl 'https://a.blazemeter.com'
        testId '<test which is valid with key>'
        notes ''
        sessionProperties ''
        jtlPath ''
        junitPath ''
        getJtl true
        getJunit true
        }
      }
    }


