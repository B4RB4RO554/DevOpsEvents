pipeline {
    environment {

        registry = "B4RB4RO554/DevOpsEvents"

        registryCredential = 'dockerhub_id'

        dockerImage = ''

    }

    agent any

    stages {

        stage('git') {
            steps {
                echo 'pulling from github';
                git branch : 'main',
                url : 'https://github.com/B4RB4RO554/DevOpsEvents.git'
            }
        }

         stage('maven build ') {
            steps {
                echo 'maven build';
                sh """mvn clean install """
            }
        }

        stage('testing with mockito') {
            steps {
                echo 'maven testing';
                sh "mvn test"

            }
        }

        stage('SONARQUBE Analysis') {
             steps {
                  script {
                       echo 'Running SonarQube analysis...'
                       withSonarQubeEnv("${SONARQUBE_ENV}") {
                            sh """
                            mvn sonar:sonar \
                            -Dsonar.login=${SONAR_TOKEN} \
                            -Dsonar.coverage.jacoco.xmlReportPaths=/target/site/jacoco/jacoco.xml
                            """
                       }
                  }
             }
        }



    }

}