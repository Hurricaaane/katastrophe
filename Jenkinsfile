properties([[$class: 'GitLabConnectionProperty', gitLabConnection: 'gitlab']])
pipeline {
    agent any
    stages {
        stage('Build') {
            agent {
                docker 'mzagar/jenkins-slave-jdk-maven-git'
            }
            steps {
                sh 'ls'
                sh '''
                    mkdir -p maven_local_repo
                    mvn -Dmaven.repo.local=./maven_local_repo clean compile test
                '''
            }
        }
    }

    post {
        success {
            updateCommitStatus('SUCCESS')
            updateGitlabCommitStatus name: 'jenkins', state: 'success'
        }
        failure {
            updateCommitStatus('FAILURE')
            updateGitlabCommitStatus name: 'jenkins', state: 'failed'
        }
        unstable {
            updateCommitStatus('FAILURE')
            updateGitlabCommitStatus name: 'jenkins', state: 'failed'
        }
    }
}

def updateCommitStatus(status) {
    step([
        $class: 'GitHubCommitStatusSetter',
        contextSource: [$class: 'ManuallyEnteredCommitContextSource', context: 'jenkins'],
        statusResultSource: [$class: 'ConditionalStatusResultSource', results: [[$class: 'AnyBuildResult', message: 'jenkins-pipeline', state: status]]]
    ])
}
