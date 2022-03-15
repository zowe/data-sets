# Explorer Data Sets API

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=zowe_data-sets&metric=alert_status)](https://sonarcloud.io/dashboard?id=zowe_data-sets)

## Development

Development requires an API ML instance. [Quick Start here](https://github.com/zowe/api-layer#quick-start).

Alternatively, you can use docker to run the APIML using the [run-containerized-apiml.sh](./scripts/run-containerized-apiml.sh] script.
This script requires a running z/OSMF instance, and the following environment variables to be set:
* ZOSMF_HOST - the host of your z/OSMF instance
* ZOSMF_PORT - the port of your z/OSMF instance

This script has the following optional environment variables:
* HOST_OS - set to `linux` if you are running on linux
* FILES_PORT - port you will run the Files API on
* GATEWAY_PORT - port the Gateway runs on
* DISCOVERY_PORT - port the discovery service runs on

1. Git clone repo
2. Import > Gradle > Existing Gradle Project
3. Generate test certificate

  ```
  keytool -genkeypair -keystore localhost.keystore.p12 -storetype PKCS12 \
      -storepass password -alias localhost -keyalg RSA -keysize 2048 -validity 99999 \
      -dname \"CN=Zowe Explorer Data Sets API Default Certificate, OU=Zowe API Squad, O=Zowe, L=Hursley, ST=Hampshire, C=UK\" \
      -ext san=dns:localhost,ip:127.0.0.1
  ```
4. Create run configuration with main class `org.zowe.DataSetsAndUnixFilesApplication`
5. Add environment variables to run configuration
  ```
  connection.httpsPort : ${GATEWAY_PORT}
  connection.ipAddress : ${GATEWAY_HOST}
  server.port : {DATASETS_API_PORT}
  server.ssl.keyAlias : localhost
  server.ssl.keyStore : localhost.keystore.p12
  server.ssl.keyStorePassword : password
  server.ssl.keyStoreType : PKCS12
  ```
6. Run and navigate to https://localhost:${GATEWAY_PORT}/swagger-ui.html to see endpoints

## Build

```
./gradlew build
```

## Test

### Unit Test

```
./gradlew test
```

### Integration Test

1. Ensure you have generated a test certificate as described above.
2. Ensure you are running the APIML as described above.
3. Start test server:

  ```
  java -Xms16m -Xmx512m -Dibm.serversocket.recover=true -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp \
    -Dserver.port=8443 \
    -Dserver.ssl.keyAlias=localhost \
    -Dserver.ssl.keyStore=localhost.keystore.p12 \
    -Dserver.ssl.keyStorePassword=password \
    -Dserver.ssl.keyStoreType=PKCS12 \
    -Dconnection.httpsPort=${GATEWAY_PORT} \
    -Dconnection.ipAddress=${GATEWAY_HOST} \
    -jar $(ls -1 data-sets-api-server/build/libs/data-sets-api-server-*-boot.jar) &
  ```

  *Note: Replace the `${GATEWAY_PORT}` and `${GATEWAY_HOST}` variable in above with your API Gateway server information.

4. Run integration tests:

  ```
  ./gradlew runIntegrationTests \
    -Pserver.host=${GATEWAY_HOST} \
    -Pserver.port=${GATEWAY_PORT} \
    -Pserver.username=${USERNAME} \
    -Pserver.password=${PASSWORD} \
    -Ptest.version=${VERSION_UNDER_TEST}
  ```
  *Note: Replace the `${GATEWAY_HOST}` and `$GATEWAY_PORT}` variables with your API Gateway server information

  *Note: Replace the `${USERNAME}` and `${PASSWORD}` variable in above with your z/OSMF server information.

  *Note: Replace the `${VERSION_UNDER_TEST}` variable with the api version you wish to test (accepted values or 1 or 2)

## Publishing

The [Publish branch binaries](https://github.com/zowe/data-sets/actions/workflows/binary-publish-branch.yml) action in Github Actions
will automatically publish remote branches to the [Artifactory repository](https://zowe.jfrog.io/ui/repos/tree/General/libs-snapshot-local/org/zowe/explorer/files)
on every push. There is a folder for each module, `data-sets-api-server`, `data-sets-model`, `data-sets-tests`, and `data-sets-zowe-server-package`.

The jar executable will be published to `data-sets-api-server/{gradle.properties version}/{branch_name}`.

## Releasing

When there is a new minor release of Zowe, there should be a new patch release of the Files API from the `master` branch and a new patch snapshot version created.
This is done in two steps:

1. Release the binaries with the [binary specific release workflow](https://github.com/zowe/data-sets/actions/workflows/binary-specific-release.yml).
    * `release_version` is the version that will be released. This should be a new patch version. For example, if `master` is currently on version 1.0.13-SNAPSHOT, `release_version` would be 1.0.14.
    * `new_version` should be a `SNAPSHOT` version with a new patch version. For example, if `master` is currently on version 1.0.13-SNAPSHOT, `new_version` would be 1.0.14-SNAPSHOT.

2. Release the images with the [image specific release workflow](https://github.com/zowe/data-sets/actions/workflows/image-specific-release.yml).
    * `release_version` is the version that will be released. This should be the same value as used in step `1`.

After this release is finished the new version must be added to the [release candidate manifest](https://github.com/zowe/zowe-install-packaging/blob/rc/manifest.json.template).

The following sections of the manifest need to have their version tag updated to the newly released version of the API ML (the value used in `release_version`):
* `org.zowe.explorer.files.data-sets-zowe-server-package`
* `data-sets` repository `tag` version under `sourceDependencies`
* `files-api` `tag` version under `imageDependencies`