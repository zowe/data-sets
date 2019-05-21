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

  pipeline.admins.add("jackjia")

  // we have extra parameters for integration test
  pipeline.addBuildParameters(
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
      url                        : lib.Constants.DEFAULT_ARTIFACTORY_URL,
      usernamePasswordCredential : lib.Constants.DEFAULT_ARTIFACTORY_ROBOT_CREDENTIAL,
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
      [dir: "jobs-api-server/build/reports/tests/test", files: "index.html", name: "Report: Unit Test"],
    ],
  )

  pipeline.test(
    name          : 'Integration',
    operation     : {
      echo "Preparing certificates ..."
      sh """keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
-storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
-dname \"CN=Zowe Jobs Explorer API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
-ext san=dns:localhost,ip:127.0.0.1"""

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
-jar \$(ls -1 jobs-api-server/build/libs/jobs-api-server-*-boot.jar) &"""

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
-Pserver.password=${PASSWORD}"""
      }
    },
    junit         : '**/test-results/test/*.xml',
    htmlReports   : [
      [dir: "jobs-tests/build/reports/tests/test", files: "index.html", name: "Report: Integration Test"],
    ],
  )

  pipeline.sonarScan(
    scannerServer   : lib.Constants.DEFAULT_SONARQUBE_SERVER
  )

  // how we packaging jars/zips
  pipeline.packaging(
      name: 'explorer-jobs',
      operation: {
          sh './gradlew packageJobsApiServer'
      }
  )

  // define we need publish stage
  pipeline.publish(
    // NOTE: task publishArtifacts will publish to lib-release-local because we don't have SNAPSHOT in version
    artifacts: [
      'jobs-zowe-server-package/build/distributions/jobs-server-zowe-package.zip'
    ]
  )

  // define we need release stage
  pipeline.release()

  pipeline.end()
}
