#!groovy

/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2018, 2019
 */


node('ibm-jenkins-slave-nvm') {
  def lib = library("jenkins-library").org.zowe.jenkins_shared_library

  def pipeline = lib.pipelines.gradle.GradlePipeline.new(this)
  def uniqueBuildId

  pipeline.admins.add("jackjia", "jcain", "stevenh")

  // we have extra parameters for integration test
  pipeline.addBuildParameters(
    string(
      name: 'INTEGRATION_TEST_APIML_BUILD',
      description: 'APIML build for integration test',
      defaultValue: 'libs-release-local/com/ca/mfaas/sdk/mfaas-zowe-install/*/mfaas-zowe-install-*.zip',
      trim: true,
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_ZOSMF_HOST',
      description: 'z/OSMF server for integration test',
      defaultValue: 'river.zowe.org',
      trim: true,
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_ZOSMF_PORT',
      description: 'z/OSMF port for integration test',
      defaultValue: '10443',
      trim: true,
      required: true
    ),
    credentials(
      name: 'INTEGRATION_TEST_ZOSMF_CREDENTIAL',
      description: 'z/OSMF credential for integration test',
      credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
      defaultValue: 'ssh-zdt-test-image-guest-fvt',
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_SSH_PORT',
      description: 'SSH port for integration test server',
      defaultValue: '2022',
      trim: true,
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_DIRECTORY_ROOT',
      description: 'Root directory for integration test',
      defaultValue: '/zaas1',
      trim: true,
      required: true
    ),
    credentials(
      name: 'INTEGRATION_TEST_DIRECTORY_INIT_USER',
      description: 'z/OSMF credential to initialize integration test folders / files',
      credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
      defaultValue: 'ssh-zdt-test-image-guest',
      required: true
    )
  )

  pipeline.setup(
    github: [
      email                      : lib.Constants.DEFAULT_GITHUB_ROBOT_EMAIL,
      usernamePasswordCredential : lib.Constants.DEFAULT_GITHUB_ROBOT_CREDENTIAL,
    ],
    artifactory: [
      url                        : lib.Constants.DEFAULT_LFJ_ARTIFACTORY_URL,
      usernamePasswordCredential : lib.Constants.DEFAULT_LFJ_ARTIFACTORY_ROBOT_CREDENTIAL,
    ]
  )

  // we have a custom build command
  pipeline.build()

  pipeline.test(
    name          : 'Unit',
    operation     : {
        sh './gradlew coverage'
    },
    junit         : '**/test-results/test/*.xml',
    htmlReports   : [
      [dir: "build/reports/jacoco/jacocoFullReport/html", files: "index.html", name: "Report: Code Coverage"],
      [dir: "data-sets-api-server/build/reports/tests/test", files: "index.html", name: "Report: Unit Test"],
    ],
  )

  pipeline.test(
    name          : 'Integration',
    operation     : {
      lock("data-sets-integration-test-at-${params.INTEGRATION_TEST_ZOSMF_HOST}-${params.INTEGRATION_TEST_ZOSMF_PORT}") {

      def buildIdentifier = lib.Utils.getBuildIdentifier(env)
      uniqueBuildId = "datasets-integration-test-${buildIdentifier}"
      if (!uniqueBuildId) {
          error "Cannot determine unique build ID."
      }

      echo "Preparing services for test ..."
      withCredentials([
        usernamePassword(
          credentialsId: params.INTEGRATION_TEST_DIRECTORY_INIT_USER,
          usernameVariable: 'USERNAME',
          passwordVariable: 'PASSWORD'
        )
      ]) {
        withEnv([
          "FVT_ZOSMF_HOST=${params.INTEGRATION_TEST_ZOSMF_HOST}",
          "FVT_ZOSMF_PORT=${params.INTEGRATION_TEST_ZOSMF_PORT}",
          "FVT_SERVER_SSH_HOST=${params.INTEGRATION_TEST_ZOSMF_HOST}",
          "FVT_SERVER_SSH_PORT=${params.INTEGRATION_TEST_SSH_PORT}",
          "FVT_SERVER_SSH_USERNAME=${USERNAME}",
          "FVT_SERVER_SSH_PASSWORD=${PASSWORD}",
          "FVT_SERVER_DIRECTORY_ROOT=${params.INTEGRATION_TEST_DIRECTORY_ROOT}",
          "FVT_UID=${uniqueBuildId}"
        ]) {
          sh "scripts/prepare-fvt.sh '${params.INTEGRATION_TEST_APIML_BUILD}'"
        }
      }

      // give it a little time to start the server
      sleep time: 2, unit: 'MINUTES'

      echo "Starting test ..."
      try {
        withCredentials([
          usernamePassword(
            credentialsId: params.INTEGRATION_TEST_ZOSMF_CREDENTIAL,
            usernameVariable: 'USERNAME',
            passwordVariable: 'PASSWORD'
          )
        ]) {
            echo "Testing version 1 - v1 Ltpa"
            sh """./gradlew runIntegrationTests \
                -Pserver.host=localhost \
                -Pserver.port=7554 \
                -Pserver.username=${USERNAME} \
                -Pserver.password=${PASSWORD} \
                -Pserver.test.directory=${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId} \
                -Ptest.version=1"""
            echo "Testing version 2 - v2 JWT"
            sh """./gradlew runIntegrationTests \
                -Pserver.host=localhost \
                -Pserver.port=7554 \
                -Pserver.username=${USERNAME} \
                -Pserver.password=${PASSWORD} \
                -Pserver.test.directory=${params.INTEGRATION_TEST_DIRECTORY_ROOT}_2/${uniqueBuildId} \
                -Ptest.version=2"""
          }
      } catch (e) {
        echo "Error with integration test: ${e}"
        throw e
      } finally {
        // show logs (the folder should match the folder defined in prepare-fvt.sh)
        sh "find .fvt/logs -type f | xargs -i sh -c 'echo \">>>>>>>>>>>>>>>>>>>>>>>> {} >>>>>>>>>>>>>>>>>>>>>>>\" && cat {}'"
      }

      } // end of lock
    },
    junit         : '**/test-results/**/*.xml',
    htmlReports   : [
      [dir: "data-sets-tests/build/reports/tests/test-1", files: "index.html", name: "Report: Integration Test v1"],
      [dir: "data-sets-tests/build/reports/tests/test-2", files: "index.html", name: "Report: Integration Test v2"],
    ],
    timeout: [time: 30, unit: 'MINUTES']
  )

  pipeline.sonarScan(
    scannerTool     : lib.Constants.DEFAULT_LFJ_SONARCLOUD_SCANNER_TOOL,
    scannerServer   : lib.Constants.DEFAULT_LFJ_SONARCLOUD_SERVER,
    allowBranchScan : lib.Constants.DEFAULT_LFJ_SONARCLOUD_ALLOW_BRANCH,
    failBuild       : lib.Constants.DEFAULT_LFJ_SONARCLOUD_FAIL_BUILD
  )

  // how we packaging jars/zips
  pipeline.packaging(
      name: 'explorer-data-sets',
      operation: {
          sh './gradlew packageDataSetsApiServer'
      }
  )

  // define we need publish stage
  pipeline.publish(
    // NOTE: task publishArtifacts will publish to lib-release-local because we don't have SNAPSHOT in version
    artifacts: [
      'data-sets-zowe-server-package/build/distributions/data-sets-server-zowe-package.zip'
    ]
  )

  // define we need release stage
  pipeline.release()

  pipeline.end(
    always: {
      // display fvt test logs
      // this should match FVT_WORKSPACE and FVT_LOGS_DIR defined in scripts/prepare-fvt.sh
      dir('.fvt/logs') {
        sh "find . -type f | xargs -i sh -c 'echo \">>>>>>>>>>>>>>>>>>>>>>>> {} >>>>>>>>>>>>>>>>>>>>>>>\" && cat {}'"
      }
      // clean up integration test folder
      if (uniqueBuildId) {
        withCredentials([
          usernamePassword(
            credentialsId: params.INTEGRATION_TEST_DIRECTORY_INIT_USER, 
            usernameVariable: 'USERNAME',
            passwordVariable: 'PASSWORD'
          )
        ]) {
          // delete TEST_DIRECTORY_ROOT/uniqueBuildId
          sh """SSHPASS=${PASSWORD} sshpass -e ssh -tt -o StrictHostKeyChecking=no -o PubkeyAuthentication=no -p ${params.INTEGRATION_TEST_SSH_PORT} ${USERNAME}@${params.INTEGRATION_TEST_ZOSMF_HOST} << EOF
cd ~ && \
  [ -d "${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId}" ] && \
  rm -fr "${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId}"
echo "[cleanup-integration-test-folders] done" && exit 0
EOF"""
        }
      }
    }
  )
}
