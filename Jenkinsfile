pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                dir("Plan") {
                    script {
                        try {
                            sh './gradlew clean test --no-daemon' //run a gradle task
                        } finally {
                            junit '**/build/test-results/test/*.xml' //make the junit test results available in any case (success & failure)
                        }
                    }
                }
            }
        }
    }
}
