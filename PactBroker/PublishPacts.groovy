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
                bat 'npm run generate-pact-client'
            }
        }
        stage('Publish Pacts') {
            steps {
                bat 'npm run publish-pacts-Broker'
            }
        }
    }
}
