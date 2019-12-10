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
      name: 'INTEGRATION_TEST_ZOSMF_HOST',
      description: 'z/OSMF server for integration test',
      defaultValue: 'zzow01.zowe.marist.cloud',
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
      defaultValue: 'ssh-marist-server-zzow01-zowel',
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_SSH_PORT',
      description: 'SSH port for integration test server',
      defaultValue: '22',
      trim: true,
      required: true
    ),
    string(
      name: 'INTEGRATION_TEST_DIRECTORY_ROOT',
      description: 'Root directory for integration test',
      defaultValue: '/ZOWE/tmp',
      trim: true,
      required: true
    ),
    credentials(
      name: 'INTEGRATION_TEST_DIRECTORY_INIT_USER',
      description: 'z/OSMF credential to initialize integration test folders / files',
      credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
      defaultValue: 'ssh-marist-server-zzow01-zowep',
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

      echo "Preparing certificates ..."
      sh """keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
-storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
-dname \"CN=Zowe Data Sets Explorer API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
-ext san=dns:localhost,ip:127.0.0.1"""

      def buildIdentifier = lib.Utils.getBuildIdentifier(env)
      uniqueBuildId = "datasets-integration-test-${buildIdentifier}"
      if (!uniqueBuildId) {
          error "Cannot determine unique build ID."
      }

      echo "Preparing test folder ..."
      withCredentials([
        usernamePassword(
          credentialsId: params.INTEGRATION_TEST_DIRECTORY_INIT_USER,
          usernameVariable: 'USERNAME',
          passwordVariable: 'PASSWORD'
        )
      ]) {
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

      echo "Starting test server ..."
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

      echo "Starting test ..."
      withCredentials([
        usernamePassword(
          credentialsId: params.INTEGRATION_TEST_ZOSMF_CREDENTIAL,
          usernameVariable: 'USERNAME',
          passwordVariable: 'PASSWORD'
        )
      ]) {
        sh """./gradlew runIntegrationTests \
-Pserver.host=localhost \
-Pserver.port=8443 \
-Pserver.username=${USERNAME} \
-Pserver.password=${PASSWORD} \
-Pserver.test.directory=${params.INTEGRATION_TEST_DIRECTORY_ROOT}/${uniqueBuildId}"""
      }
      
      } // end of lock
    },
    junit         : '**/test-results/test/*.xml',
    htmlReports   : [
      [dir: "data-sets-tests/build/reports/tests/test", files: "index.html", name: "Report: Integration Test"],
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
