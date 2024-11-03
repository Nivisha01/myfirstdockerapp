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
        DEPLOYMENT_YAML = 'deployment.yml'  
    }

    stages {
        stage('Configure SSH') {
            steps {
                sshagent(credentials: ['my-ssh-key']) {
                    sh 'ssh-add -L' // Optional: Verifies that the SSH key is added
                    // Optional: Test SSH connection
                    sh 'ssh -o StrictHostKeyChecking=no ec2-user@localhost "echo SSH connection successful"'
                }
            }
        }

        stage('Clean Workspace') {
            steps {
                deleteDir()
            }
        }

        stage('Checkout Code') {
            steps {
                git credentialsId: 'GitHub_Credentials', url: "${GITHUB_REPO}", branch: 'master'
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
                    // Delete existing Minikube cluster if it exists
                    sh 'minikube delete || true'
                    
                    // Start Minikube
                    sh 'minikube start --driver=docker || exit 1'
                    
                    // Configure kubectl
                    sh 'kubectl config use-context minikube'

                    // Apply the deployment configuration from a file
                    sh "kubectl apply -f ${DEPLOYMENT_YAML}"
                    
                    // Expose the deployment using the correct deployment name
                    sh 'kubectl expose deployment myapp-deployment --type=NodePort --port=80 --target-port=8082 --namespace=default || true'
                    
                    // Get Minikube IP for application access
                    def minikubeIp = sh(script: 'minikube ip', returnStdout: true).trim()
                    echo "Access your application at: http://${minikubeIp}:80"
                }
            }
        }
    }
}
