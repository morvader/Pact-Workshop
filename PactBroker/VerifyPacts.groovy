#!groovy
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
                        sh 'npm run pact-server'
                    }
                    else {
                        bat 'npm run pact-server'
                    }
                }
            }
        }
        stage('Can-I-Deploy Server') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                    else {
                        bat 'npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                }
            }
        }
    }
}
