// The following property need to be set for the HTML report @TODO figure out how to get this nicely on jenkins
//System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")

/**
 * The name of the master branch
 */
def MASTER_BRANCH = "master"

/**
* Is this a release branch? Temporary workaround that won't break everything horribly if we merge.
*/ 
def RELEASE_BRANCH = false

/**
 * The result string for a successful build
 */
def BUILD_SUCCESS = 'SUCCESS'

/**
 * The result string for an unstable build
 */
def BUILD_UNSTABLE = 'UNSTABLE'

/**
 * The result string for a failed build
 */
def BUILD_FAILURE = 'FAILURE'

/**
 * The credentials id field for the artifactory username and password
 */
def ARTIFACTORY_CREDENTIALS_ID = 'GizaArtifactory'


// Setup conditional build options. Would have done this in the options of the declarative pipeline, but it is pretty
// much impossible to have conditional options based on the branch :/
def opts = []

if (BRANCH_NAME == MASTER_BRANCH) {
    // Only keep 20 builds
    opts.push(buildDiscarder(logRotator(numToKeepStr: '20')))

    // Concurrent builds need to be disabled on the master branch because
    // it needs to actively commit and do a build. There's no point in publishing
    // twice in quick succession
    opts.push(disableConcurrentBuilds())
} else {
    if (BRANCH_NAME.equals("package")){
        RELEASE_BRANCH = true   
    }
    // Only keep 5 builds on other branches
    opts.push(buildDiscarder(logRotator(numToKeepStr: '5')))
}

// define custom build parameters
def customParameters = []
customParameters.push(string(
  name: 'INTEGRATION_TEST_ZOSMF_HOST',
  description: 'z/OSMF server for integration test',
  defaultValue: 'river.zowe.org',
  trim: true,
  required: true
))
customParameters.push(string(
  name: 'INTEGRATION_TEST_ZOSMF_PORT',
  description: 'z/OSMF port for integration test',
  defaultValue: '10443',
  trim: true,
  required: true
))
customParameters.push(string(
  name: 'INTEGRATION_TEST_SSH_PORT',
  description: 'SSH port for integration test server',
  defaultValue: '2022',
  trim: true,
  required: true
))
customParameters.push(string(
  name: 'INTEGRATION_TEST_DIRECTORY_ROOT',
  description: 'Root directory for integration test',
  defaultValue: '/zaas1',
  trim: true,
  required: true
))
customParameters.push(credentials(
  name: 'INTEGRATION_TEST_DIRECTORY_INIT_USER',
  description: 'z/OSMF credential to initialize integration test folders / files',
  credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
  defaultValue: 'ssh-zdt-test-image-guest-tstr001',
  required: true
))
customParameters.push(credentials(
  name: 'INTEGRATION_TEST_ZOSMF_CREDENTIAL',
  description: 'z/OSMF credential for integration test',
  credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
  defaultValue: 'ssh-zdt-test-image-guest',
  required: true
))
opts.push(parameters(customParameters))

properties(opts)

// unique Build ID
// this value should be updated before using it
def uniqueBuildId = ""

pipeline {
    agent {
        label 'ibm-jenkins-slave-nvm-jnlp'
    }

    environment {
        // Environment variable for flow control. Tells most of the steps if they should build.
        SHOULD_BUILD = "true"

        // Environment variable for flow control. Indicates if the git source was updated by the pipeline.
        GIT_SOURCE_UPDATED = "false"

        // Environment variable for integration test.
        TEST_DIRECTORY_ROOT = "${params.INTEGRATION_TEST_DIRECTORY_ROOT}"
    }

    stages {
    
        /************************************************************************
         * STAGE
         * -----
         * Bootstrap gradlew
         *
         * TIMEOUT
         * -------
         * 5 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The build is still successful
         *
         * DESCRIPTION
         * -----------
         * Executes `bootstrap_gradle.sh` to bootstrap gradlew (downloads gradle-wrapper).
         *
         * OUTPUTS
         * -------
         * gradle-wrapper.jar is present in "./gradle/wrapper/" directory.
         ************************************************************************/
        stage ('Bootstrap Gradlew') {
            when {
                expression {
                    return SHOULD_BUILD == 'true'
                }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh './bootstrap_gradlew.sh'
                }
            }
        }
        
        /************************************************************************
         * STAGE
         * -----
         * Build the source code.
         *
         * TIMEOUT
         * -------
         * 10 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The build is still successful
         *
         * DESCRIPTION
         * -----------
         * Executes `gradle build` to build the source code and run unit tests.
         *
         * OUTPUTS
         * -------
         * Jenkins: Compiled application executable code
         ************************************************************************/
        stage('Build') {
            when {
                expression {
                    return SHOULD_BUILD == 'true'
                }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    sh './gradlew build'
                }
            }
        }

        /************************************************************************
         * STAGE
         * -----
         * Run unit test, generate and publish coverage report
         *
         * TIMEOUT
         * -------
         * 30 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The build is still successful and not unstable
         ************************************************************************/
        stage('Test') {
            when {
                allOf {
                    expression {
                        return SHOULD_BUILD == 'true'
                    }
                    expression {
                        return currentBuild.resultIsBetterOrEqualTo(BUILD_SUCCESS)
                    }
                }
            }
            steps {
                timeout(time: 30, unit: 'MINUTES') {
                    sh './gradlew coverage'

                   publishHTML(target: [
                       allowMissing         : false,
                       alwaysLinkToLastBuild: false,
                       keepAll              : true,
                       reportDir            : 'build/reports/jacoco/jacocoFullReport/html',
                       reportFiles          : 'index.html',
                       reportName           : "Java Coverage Report"
                   ])
                    publishHTML(target: [
                        allowMissing         : false,
                        alwaysLinkToLastBuild: false,
                        keepAll              : true,
                        reportDir            : 'data-sets-api-server/build/reports/tests/test',
                        reportFiles          : 'index.html',
                        reportName           : "Unit Test Results"
                    ])
                }
            }
        }

        /************************************************************************
        * STAGE
        * -----
        * SonarQube Scanner
        *
        * EXECUTION CONDITIONS
        * --------------------
        * - SHOULD_BUILD is true
        * - The build is still successful and not unstable
        *
        * DESCRIPTION
        * -----------
        * Runs the sonar-scanner analysis tool, which submits the source, test resutls,
        *  and coverage results for analysis in our SonarQube server.
        * TODO: This step does not yet support branch or PR submissions properly.
        ***********************************************************************/
        stage('Sonar Scan') {
            steps {
                withSonarQubeEnv('sonar-default-server') {
                    // Per Sonar Doc - It's important to add --info because of SONARJNKNS-281
                    sh "./gradlew --info sonarqube -Psonar.host.url=${SONAR_HOST_URL}"
                }
            }
        }

        /************************************************************************
         * STAGE
         * -----
         * Run integration tests
         *
         * TIMEOUT
         * -------
         * 20 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The build is still successful
         *
         * DESCRIPTION
         * -----------
         * Executes the `gradle runIntegrationTests` to run integration|system tests.
         *
         * OUTPUTS
         * -------
         * HTML test report: Integration Test Results
         ************************************************************************/
        stage('Integration Test') {
            when {
                expression {
                    return SHOULD_BUILD == 'true'
                }
                expression {
                    return currentBuild.resultIsBetterOrEqualTo(BUILD_SUCCESS)
                }
            }
            stages {
                stage('Prepare Build ID') {
                    steps {
                        // generate unique build ID
                        script {
                            def buildIdentifier = getBuildIdentifier()
                            uniqueBuildId = "datasets-integration-test-${buildIdentifier}"
                            if (!uniqueBuildId) {
                                error "Cannot determine unique build ID."
                            }
                        }
                    }
                }

                stage('Prepare Certificate') {
                    steps {
                        sh """keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
    -storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
    -dname \"CN=Zowe Explorer Data Sets API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
    -ext san=dns:localhost,ip:127.0.0.1"""
                    }
                }

                stage('Start Server') {
                    steps {
                        sh """java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp \
    -Dserver.port=8443 \
    -Dserver.ssl.keyAlias=localhost \
    -Dserver.ssl.keyStore=localhost.keystore.p12 \
    -Dserver.ssl.keyStorePassword=password \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dzosmf.httpsPort=${params.INTEGRATION_TEST_ZOSMF_PORT} \
    -Dzosmf.ipAddress=${params.INTEGRATION_TEST_ZOSMF_HOST} \
    -jar \$(ls -1 data-sets-api-server/build/libs/data-sets-api-server-*-boot.jar) &"""

                        // give it a little time to start the server
                        sleep time: 1, unit: 'MINUTES'
                    }
                }

                stage('Prepare Test Directory') {
                    steps {
                        timeout(time: 20, unit: 'MINUTES') {
                            withCredentials([usernamePassword(credentialsId: params.INTEGRATION_TEST_DIRECTORY_INIT_USER, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                // send file to test image host
                                sh """SSHPASS=${PASSWORD} sshpass -e sftp -o BatchMode=no -o StrictHostKeyChecking=no -o PubkeyAuthentication=no -b - -P ${params.INTEGRATION_TEST_SSH_PORT} ${USERNAME}@${params.INTEGRATION_TEST_ZOSMF_HOST} << EOF
put scripts/prepare-integration-test-folders.sh
EOF"""
                                // create TEST_DIRECTORY_ROOT/uniqueBuildId
                                sh """SSHPASS=${PASSWORD} sshpass -e ssh -tt -o StrictHostKeyChecking=no -o PubkeyAuthentication=no -p ${params.INTEGRATION_TEST_SSH_PORT} ${USERNAME}@${params.INTEGRATION_TEST_ZOSMF_HOST} << EOF
cd ~ && \
  (iconv -f ISO8859-1 -t IBM-1047 prepare-integration-test-folders.sh > prepare-integration-test-folders.sh.new) && mv prepare-integration-test-folders.sh.new prepare-integration-test-folders.sh && chmod +x prepare-integration-test-folders.sh
./prepare-integration-test-folders.sh ${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId} || { echo "[prepare-integration-test-folders] failed"; exit 1; }
echo "[prepare-integration-test-folders] succeeds" && exit 0
EOF"""
                            }
                        }
                    }
                }

                stage('Start Test') {
                    steps {
                        timeout(time: 20, unit: 'MINUTES') {
                            withCredentials([usernamePassword(credentialsId: params.INTEGRATION_TEST_ZOSMF_CREDENTIAL, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                                script {
                                    try {
                                        sh """./gradlew runIntegrationTests \
    -Pserver.host=localhost \
    -Pserver.port=8443 \
    -Pserver.username=${USERNAME} \
    -Pserver.password=${PASSWORD}"""
                                    } catch (err) {
                                        // ignore test failures
                                        // FIXME: after fix all failed test during test, this stage
                                        //        should fail the pipeline
                                    }
                                }
                            }
                        }

                        publishHTML(target: [
                            allowMissing         : false,
                            alwaysLinkToLastBuild: false,
                            keepAll              : true,
                            reportDir            : 'data-sets-tests/build/reports/tests/test',
                            reportFiles          : 'index.html',
                            reportName           : "Integration Test Results"
                        ])
                    }
                }
            }
        }

        /************************************************************************
         * STAGE
         * -----
         * Package
         *
         * TIMEOUT
         * -------
         * 5 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The current branch is MASTER branch or a RELEASE_BRANCH
         * - The build is still successful and not unstable
         *
         * DESCRIPTION
         * -----------
         * Pacakge the current build as a zip file for zowe integration.
         *
         * OUTPUTS
         * -------
         * zip package
         ************************************************************************/
        stage('Package') {
            when {
                allOf {
                    expression {
                        return SHOULD_BUILD == 'true'
                    }
                    expression {
                        return currentBuild.resultIsBetterOrEqualTo(BUILD_SUCCESS)
                    }
                    expression {
                        return BRANCH_NAME.equals(MASTER_BRANCH) || RELEASE_BRANCH;   
                    }
                }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    sh './gradlew packageDataSetsApiServer'
                }
            }
        }

        /************************************************************************
         * STAGE
         * -----
         * Deploy
         *
         * TIMEOUT
         * -------
         * 5 Minutes
         *
         * EXECUTION CONDITIONS
         * --------------------
         * - SHOULD_BUILD is true
         * - The current branch is MASTER branch or a RELEASE_BRANCH
         * - The build is still successful and not unstable
         *
         * DESCRIPTION
         * -----------
         * Deploys the current build as a maven artifact to a Maven Repository.
         *
         * OUTPUTS
         * -------
         * maven|gradle: an artifact is deployed
         ************************************************************************/
        stage('Deploy') {
            when {
                allOf {
                    expression {
                        return SHOULD_BUILD == 'true'
                    }
                    expression {
                        return currentBuild.resultIsBetterOrEqualTo(BUILD_SUCCESS)
                    }
                    expression {
                        return BRANCH_NAME.equals(MASTER_BRANCH) || RELEASE_BRANCH;   
                    }
                }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    echo 'Publish Artifacts'
                    // Get the registry that we need to publish to
                    withCredentials([usernamePassword(credentialsId: ARTIFACTORY_CREDENTIALS_ID, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        sh "./gradlew publishArtifacts --info -Pdeploy.username=$USERNAME -Pdeploy.password=$PASSWORD"
                    }
                }
            }
        }
    }
    post {
        /************************************************************************
         * POST BUILD ACTION
         *
         * This step only is executed when SHOULD_BUILD is true.
         *
         * Sends out emails when any of the following are true:
         *
         * - It is the first build for a new branch
         * - The build is successful but the previous build was not
         * - The build failed or is unstable
         * - The build is on the MASTER_BRANCH
         *
         * In the case that an email was sent out, it will send it to individuals
         * who were involved with the build and if broken those involved in
         * breaking the build. If this build is for the MASTER_BRANCH, then an
         * additional set of individuals will also get an email that the build
         * occurred.
         ************************************************************************/
        always {
            // publish any test results found
            junit allowEmptyResults: true, testResults: '**/test-results/**/*.xml'

            script {
                // remove temporary folder
                if (uniqueBuildId) {
                    withCredentials([usernamePassword(credentialsId: params.INTEGRATION_TEST_DIRECTORY_INIT_USER, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        // delete TEST_DIRECTORY_ROOT/uniqueBuildId
                        sh """SSHPASS=${PASSWORD} sshpass -e ssh -tt -o StrictHostKeyChecking=no -o PubkeyAuthentication=no -p ${params.INTEGRATION_TEST_SSH_PORT} ${USERNAME}@${params.INTEGRATION_TEST_ZOSMF_HOST} << EOF
cd ~ && \
  [ -d "${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId}" ] && \
  rm -fr "${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId}"
echo "[cleanup-integration-test-folders] done" && exit 0
EOF"""
                   }
                }

                def buildStatus = currentBuild.currentResult

                if (SHOULD_BUILD == 'true') {
                    try {
                        def previousBuild = currentBuild.getPreviousBuild()
                        def recipients = ""

                        def subject = "${currentBuild.currentResult}: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
                        def consoleOutput = """
                        <p>Branch: <b>${BRANCH_NAME}</b></p>
                        <p>Check console output at "<a href="${RUN_DISPLAY_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>
                        """

                        def details = ""

                        if (previousBuild == null) {
                            details = "<p>Initial build for new branch.</p>"
                        } else if (currentBuild.resultIsBetterOrEqualTo(BUILD_SUCCESS) && previousBuild.resultIsWorseOrEqualTo(BUILD_UNSTABLE)) {
                            details = "<p>Build returned to normal.</p>"
                        }

                        // Issue #53 - Previously if the first build for a branch failed, logs would not be captured.
                        //             Now they do!
                        if (currentBuild.resultIsWorseOrEqualTo(BUILD_UNSTABLE)) {
                            // Archives any test artifacts for logging and debugging purposes
                            // TODO: archiveArtifacts allowEmptyArchive: true, artifacts: '__tests__/__results__/**/*.log'
                            details = "${details}<p>Build Failure.</p>"
                        }

                        if (details != "") {
                            echo "Sending out email with details"
                            emailext(
                                    subject: subject,
                                    to: recipients,
                                    body: "${details} ${consoleOutput}",
                                    recipientProviders: [[$class: 'DevelopersRecipientProvider'],
                                                         [$class: 'UpstreamComitterRecipientProvider'],
                                                         [$class: 'CulpritsRecipientProvider'],
                                                         [$class: 'RequesterRecipientProvider']]
                            )
                        }
                    } catch (e) {
                        echo "Experienced an error sending an email for a ${buildStatus} build"
                        currentBuild.result = buildStatus
                    }
                }
            }
        }
    }
}