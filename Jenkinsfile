// ============================================
// TASKMASTER - JENKINS PIPELINE
// ============================================
// Pipeline stages:
//   1. Checkout      → Pull code from GitHub
//   2. Build Backend → Maven compile
//   3. Test Backend  → Unit + Integration tests
//   4. Build Frontend → npm install + build
//   5. Test Frontend → Karma tests
//   6. Docker Build  → Build images
//   7. Push Images   → Push to registry (simulated)
//   8. Deploy        → Deploy to environment
// ============================================

pipeline {
    agent any

    environment {
        // Application metadata
        APP_NAME = 'taskmaster'
        VERSION = "${env.BUILD_NUMBER}"

        // Docker image tags
        BACKEND_IMAGE = "${APP_NAME}-backend:${VERSION}"
        FRONTEND_IMAGE = "${APP_NAME}-frontend:${VERSION}"

        // Database credentials for tests
        DB_URL = 'jdbc:postgresql://localhost:5432/taskmaster_test'
        DB_USERNAME = 'test_user'
        DB_PASSWORD = 'test_pass'
    }

    options {
        // Pipeline options
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    triggers {
        // Poll GitHub every 5 minutes (for demo; webhooks preferred)
        pollSCM('H/5 * * * *')
    }

    stages {
        // ==========================================
        // STAGE 1: CHECKOUT
        // ==========================================
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from GitHub..."
                    echo "Branch: ${env.BRANCH_NAME}"
                    echo "Commit: ${env.GIT_COMMIT}"
                }

                // Clean workspace before build
                cleanWs()

                // Checkout code
                checkout scm
            }
        }

        // ==========================================
        // STAGE 2: BUILD BACKEND
        // ==========================================
        stage('Build Backend') {
            steps {
                script {
                    echo "Building Spring Boot backend..."
                }


                // Maven build (compile only, no tests yet)
                sh 'chmod +x mvnw && ./mvnw clean compile -B'

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
                script {
                    echo "Running backend tests..."
                }


                // Run all tests with coverage
                sh 'chmod +x mvnw && ./mvnw test -B'

            }
            post {
                always {
                    // Publish test results
                    junit 'target/surefire-reports/*.xml'

                    // Publish coverage report
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'Backend Coverage Report'
                    ])
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
                script {
                    echo "Building Angular frontend..."
                }

                dir('frontend') {
                    // Install dependencies
                    sh 'npm ci'

                    // Build for production
                    sh 'npm run build -- --configuration production'
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
                script {
                    echo "Running frontend tests..."
                }

                dir('frontend') {
                    // Run tests with headless browser
                    sh 'ng test --watch=false --browsers=FirefoxHeadless --code-coverage'
                }
            }
            post {
                always {
                    // Publish coverage report
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'frontend/coverage/frontend',
                        reportFiles: 'index.html',
                        reportName: 'Frontend Coverage Report'
                    ])
                }
                success {
                    echo "All frontend tests passed"
                }
                failure {
                    echo "Frontend tests failed"
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

                // Build backend image

                sh "docker build -t ${BACKEND_IMAGE} ."


                // Build frontend image
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
                    echo "🔍 Validating Docker images..."
                }

                // Run backend container and test health
                sh """
                    docker run -d --name ${APP_NAME}-backend-test -p 8081:8080 ${BACKEND_IMAGE}
                    sleep 30
                    curl -f http://localhost:8081/api/v1/actuator/health || exit 1
                    docker stop ${APP_NAME}-backend-test
                    docker rm ${APP_NAME}-backend-test
                """

                // Run frontend container and test health
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
                    // Cleanup on failure
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

                    // Tag for registry
                    sh "docker tag ${BACKEND_IMAGE} ${APP_NAME}-backend:latest"
                    sh "docker tag ${FRONTEND_IMAGE} ${APP_NAME}-frontend:latest"

                    // Simulate push (no actual registry in local setup)
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

                    // Simulate deployment
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
            // Cleanup workspace
            cleanWs()

            // Send notification (simplified)
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
