#!groovy
pipeline {
    agent any

    stages {
        stage('Get project') {
            steps {
                git branch: '6-PactBroker', url: 'https://github.com/morvader/Pact-Workshop'
                bat 'npm install'
            }
        }
        stage('Create Pacts') {
            steps {
                bat 'npm run pact-server'
            }
        }
        stage('Can-I-Deploy Server') {
            steps {
                bat 'npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
            }
        }
    }
}
