pipeline {
    environment {

        registry = "Zoubeir20/devops"

        registryCredential = 'dockerhub_id'

        dockerImage = ''

    }

    agent any

    stages {

        stage('git') {
            steps {
                echo 'pulling from github';
                git branch : 'master',
                url : 'https://github.com/Zoubeir20/Kaddem-DEVOPS'
            }
        }

         stage('maven build ') {
            steps {
                echo 'maven build';
                sh """mvn clean install """
            }
        }

    }

}