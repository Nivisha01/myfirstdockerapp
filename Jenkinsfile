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
        KUBECONFIG_CREDENTIAL_ID = 'mykubeconfig'
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
                    // Set the KUBECONFIG environment variable
                    withCredentials([file(credentialsId: KUBECONFIG_CREDENTIAL_ID, variable: 'KUBECONFIG')]) {
                        
                        // Optionally delete the existing deployment if needed
                        sh "/usr/local/bin/kubectl delete deployment spring-app || true"

                        // Create the deployment
                        sh "/usr/local/bin/kubectl create deployment spring-app --image='${DOCKER_IMAGE_NAME}'"

                        // Check if the service already exists and delete it if it does
                        sh """
                            if /usr/local/bin/kubectl get service spring-app; then
                                /usr/local/bin/kubectl delete service spring-app
                            fi
                        """

                        // Expose the deployment with NodePort
                        sh "/usr/local/bin/kubectl expose deployment spring-app --type=NodePort --port=8082 --target-port=8081"

                        // Check the status of services, pods, and resources
                        sh "/usr/local/bin/kubectl get services"
                        sh "/usr/local/bin/kubectl get pods"
                        sh "/usr/local/bin/kubectl get all"

                        // Check for errors in pods
                        def podStatus = sh(script: "/usr/local/bin/kubectl get pods --field-selector=status.phase!=Running", returnStdout: true).trim()
                        if (podStatus) {
                            echo "Some pods are not running: ${podStatus}"
                        } else {
                            echo "All pods are running successfully."
                        }
                    }
                }
            }
        }
    }
}
