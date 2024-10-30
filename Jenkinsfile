pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'JDK17'
    }

    environment {
        DOCKER_IMAGE_NAME = 'nivisha/spring-app:latest'
        GITHUB_REPO = 'https://github.com/Nivisha01/myfirstdockerapp.git'
        SONARQUBE_SERVER = 'http://44.196.180.172:9000/'
        SONARQUBE_TOKEN = credentials('sonar-token')
        PROJECT_NAME = 'Web-app'
        SONAR_HOST_URL = "${SONARQUBE_SERVER}"
        DOCKER_CREDENTIALS_ID = 'DockerHub_Cred'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }
        stage('Checkout Code') {
            steps {
                script {
                    git credentialsId: 'GitHub_Credentials', url: "${GITHUB_REPO}", branch: 'master'
                }
            }
        }
        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn sonar:sonar \
                        -Dmaven.test.skip=true \
                        -Dsonar.projectKey=${PROJECT_NAME} \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONARQUBE_TOKEN}
                    """
                }
            }
        }
        stage('Docker Build and Push') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE_NAME} ."

                    withCredentials([usernamePassword(credentialsId: DOCKER_CREDENTIALS_ID, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                        sh "echo ${DOCKER_PASS} | docker login -u ${DOCKER_USER} --password-stdin"
                        sh "docker push ${DOCKER_IMAGE_NAME}"
                    }
                }
            }
        }
        stage('Kubernetes Deployment') {
            steps {
                script {
                    // Start Minikube if not already started
                    sh 'minikube start'

                    // Check Minikube status
                    sh 'minikube status'

                    // Set Kubernetes context to Minikube
                    sh 'kubectl config use-context minikube'

                    // Create deployment
                    sh "kubectl create deployment spring-web-app --image=${DOCKER_IMAGE_NAME}"

                    // Expose the deployment
                    sh "kubectl expose deployment spring-web-app --type=LoadBalancer --port=80 --target-port=8085"

                    // Get the Minikube IP
                    def minikubeIp = sh(script: 'minikube ip', returnStdout: true).trim()
                    echo "Access your application at: http://${minikubeIp}:80"

                    // Optionally, open the Minikube dashboard
                    sh 'minikube dashboard &'
                }
            }
        }
    }
}
