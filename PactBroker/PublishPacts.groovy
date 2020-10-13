pipeline {
    agent any

    stages {
        stage('Get project') {
            steps {
                git branch: '6-PactBroker', url: 'https://github.com/morvader/Pact-Workshop'
                script {
                    if (isUnix()) {
                        sh 'npm install'
                    }
                    else {
                        bat 'npm install'
                    }
                }
            }
        }
        stage('Create Pacts') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'npm run generate-pact-client'
                    }
                    else {
                        bat 'npm run generate-pact-client'
                    }
                }
            }
        }
        stage('Publish Pacts') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'npm run publish-pacts-Broker'
                    }
                    else {
                        bat 'npm run publish-pacts-Broker'
                    }
                }
            }
        }
    }
}
