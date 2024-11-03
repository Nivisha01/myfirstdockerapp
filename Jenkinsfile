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
                    // Start Minikube if not already running
                    sh '''
                        export PATH=$PATH:/usr/local/bin
                        if ! minikube status | grep -q "host: Running"; then
                            minikube start --driver=none || exit 1
                        fi
                    '''
                    
                    // Configure kubectl to use Minikube’s context
                    sh 'kubectl config use-context minikube'

                    // Apply the deployment configuration directly
                    sh """
                        kubectl apply -f - <<EOF
                        apiVersion: apps/v1
                        kind: Deployment
                        metadata:
                          name: spring-web-app
                          namespace: default
                        spec:
                          replicas: 2
                          selector:
                            matchLabels:
                              app: spring-web-app
                          template:
                            metadata:
                              labels:
                                app: spring-web-app
                            spec:
                              containers:
                                - name: spring-web-app
                                  image: ${DOCKER_IMAGE_NAME}
                                  ports:
                                    - containerPort: 8082
                        EOF
                    """
                    
                    // Expose deployment
                    sh "kubectl expose deployment spring-web-app --type=NodePort --port=80 --target-port=8082 --namespace=default || true"
                    
                    // Get Minikube IP for application access
                    def minikubeIp = sh(script: 'minikube ip', returnStdout: true).trim()
                    echo "Access your application at: http://${minikubeIp}:80"
                }
            }
        }
    }
}
