pipeline {
    agent any

    stages {
        stage('Can-I-Deploy Consumer') {
            steps {
                dir('../PublishPacts') {
                    script {
                        if (isUnix()) {
                            sh 'npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                        }
                        else {
                            bat 'npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                        }
                    }
                }
            }
        }
    }
}
