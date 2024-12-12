pipeline {
    environment {

        SONARQUBE_ENV = 'SonarQube'
        SONAR_TOKEN = credentials('SonarToken')
        registry = "mazenkababou/devopsevents"
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

        stage('nexus') {
            steps {
                echo 'Deploy to nexus';
                sh 'mvn deploy -D skipTests'

            }
        }
//         stage('Deploy to NEXUS') {
//             steps {
//                 script {
//                     echo "Deploying artifact to Nexus..."
//                     withCredentials([usernamePassword(credentialsId: 'NEXUS', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASS')]) {
//                         sh 'mvn -X deploy -DskipTests=true -Dnexus.username=$NEXUS_USER -Dnexus.password=$NEXUS_PASS'
//                     }
//                 }
//             }
//         }


        stage('Building our image') {
            steps {
                script {
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
            }
        }

        stage('Deploy our image') {
            steps {
                script {
                    docker.withRegistry( '', registryCredential ) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Cleaning up') {
            steps {
                sh "docker rmi $registry:$BUILD_NUMBER"
            }
        }

        stage('Building and deploying using docker-compose') {
            steps {
                sh 'docker-compose up -d'
            }
        }

    }

}