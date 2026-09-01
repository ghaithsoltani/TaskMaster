// ============================================
// TASKMASTER - JENKINS PIPELINE
// ============================================
// Pipeline stages:
//   1. Checkout         → Pull code from GitHub
//   2. Build Backend    → Maven compile
//   3. Test Backend     → Unit + Integration tests
//   4. Build Frontend   → npm install + build
//   5. Test Frontend    → Karma tests
//   6. Docker Build     → Build images
//   7. Image Validation → Validate containers
//   8. Push Images      → Push to registry (simulated)
//   9. Deploy           → Deploy to environment
// ============================================

pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        nodejs 'NodeJS-20'
    }

    stages {

        // ==========================================
        // STAGE 1: CHECKOUT
        // ==========================================
        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub..."
                checkout scm
            }
        }

        // ==========================================
        // STAGE 2: BUILD BACKEND
        // ==========================================
        stage('Build Backend') {
            steps {
                echo "Building Spring Boot backend..."
                sh '''
                    chmod +x mvnw
                    ./mvnw clean compile -B
                '''
            }
            post {
                success {
                    echo "Backend compiled successfully"
                }
                failure {
                    echo "Backend compilation failed"
                }
            }
        }

        // ==========================================
        // STAGE 3: TEST BACKEND
        // ==========================================
        stage('Test Backend') {
            steps {
                echo "Running backend tests..."
                sh '''
                    chmod +x mvnw
                    ./mvnw test -B
                '''
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
                success {
                    echo "All backend tests passed"
                }
                failure {
                    echo "Backend tests failed"
                }
            }
        }

        // ==========================================
        // STAGE 4: BUILD FRONTEND
        // ==========================================
        stage('Build Frontend') {
            steps {
                echo "Building Angular frontend..."
                dir('frontend') {
                    sh '''
                        node --version
                        npm --version
                        npm ci
                        npm run build
                    '''
                }
            }
            post {
                success {
                    echo "Frontend built successfully"
                }
                failure {
                    echo "Frontend build failed"
                }
            }
        }

        // ==========================================
        // STAGE 5: TEST FRONTEND
        // ==========================================
        stage('Test Frontend') {
            steps {
                dir('frontend') {
                    sh '''
                        npm test -- --watch=false
                    '''
                }
            }
        }

        // ==========================================
        // STAGE 6: DOCKER BUILD
        // ==========================================
        stage('Docker Build') {
            steps {
                script {
                    echo "Building Docker images..."
                    echo "Backend: ${BACKEND_IMAGE}"
                    echo "Frontend: ${FRONTEND_IMAGE}"
                }
                sh "docker build -t ${BACKEND_IMAGE} ."
                dir('frontend') {
                    sh "docker build -t ${FRONTEND_IMAGE} ."
                }
            }
            post {
                success {
                    echo "Docker images built successfully"
                }
                failure {
                    echo "Docker build failed"
                }
            }
        }

        // ==========================================
        // STAGE 7: IMAGE VALIDATION
        // ==========================================
        stage('Image Validation') {
            steps {
                script {
                    echo "Validating Docker images..."
                }
                sh """
                    docker run -d --name ${APP_NAME}-backend-test -p 8081:8080 ${BACKEND_IMAGE}
                    sleep 30
                    curl -f http://localhost:8081/api/v1/actuator/health || exit 1
                    docker stop ${APP_NAME}-backend-test
                    docker rm ${APP_NAME}-backend-test
                """
                sh """
                    docker run -d --name ${APP_NAME}-frontend-test -p 8082:80 ${FRONTEND_IMAGE}
                    sleep 10
                    curl -f http://localhost:8082/health || exit 1
                    docker stop ${APP_NAME}-frontend-test
                    docker rm ${APP_NAME}-frontend-test
                """
            }
            post {
                success {
                    echo "Image validation passed"
                }
                failure {
                    echo "Image validation failed"
                    sh """
                        docker stop ${APP_NAME}-backend-test || true
                        docker rm ${APP_NAME}-backend-test || true
                        docker stop ${APP_NAME}-frontend-test || true
                        docker rm ${APP_NAME}-frontend-test || true
                    """
                }
            }
        }

        // ==========================================
        // STAGE 8: PUSH IMAGES (Simulated)
        // ==========================================
        stage('Push Images') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Pushing images to registry..."
                    echo "In production, this would push to Docker Hub, ECR, or GCR"
                    sh "docker tag ${BACKEND_IMAGE} ${APP_NAME}-backend:latest"
                    sh "docker tag ${FRONTEND_IMAGE} ${APP_NAME}-frontend:latest"
                    echo "Simulated: docker push ${APP_NAME}-backend:latest"
                    echo "Simulated: docker push ${APP_NAME}-frontend:latest"
                }
            }
        }

        // ==========================================
        // STAGE 9: DEPLOY (Simulated)
        // ==========================================
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Deploying to environment..."
                    echo "In production, this would deploy to Kubernetes or Docker Swarm"
                    echo "Simulated: kubectl apply -f kubernetes/"
                    echo "Simulated: docker-compose -f docker-compose.prod.yml up -d"
                }
            }
        }

    }

    // ============================================
    // POST-BUILD ACTIONS
    // ============================================
    post {
        always {
            cleanWs()
            echo "Pipeline completed: ${currentBuild.result}"
        }
        success {
            echo "Pipeline succeeded! All stages passed."
        }
        failure {
            echo "Pipeline failed! Check logs for details."
        }
        unstable {
            echo "Pipeline unstable! Tests may have failed."
        }
    }
}