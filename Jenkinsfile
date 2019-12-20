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
