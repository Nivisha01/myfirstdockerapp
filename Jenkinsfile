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
        KUBECONFIG_CREDENTIAL_ID  = 'mykubeconfig'
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
                    // Correcting the parameters to use the right ones
                    kubernetesDeploy(configs: DEPLOYMENT_YAML, kubeconfigId: KUBECONFIG_CREDENTIAL_ID)
                    
                    // Get Minikube IP for application access (if applicable)
                    // This may need to be adjusted based on your deployment environment
                    def minikubeIp = sh(script: 'minikube ip', returnStdout: true).trim()
                    echo "Access your application at: http://${minikubeIp}:80"

                    // Get status of services and pods
                    sh "kubectl get services"
                    sh "kubectl get pods"
                    sh "kubectl get all"

                    // Optionally check for errors in pods
                    def podStatus = sh(script: "kubectl get pods --field-selector=status.phase!=Running", returnStdout: true)
                    if (podStatus) {
                        echo "Some pods are not running: ${podStatus}"
                    }
                }
            }
        }
    }
}
